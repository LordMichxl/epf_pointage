package sn.epf.pointage.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.enums.StatutSeance;
import sn.epf.pointage.model.enums.TypeContrat;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

public class DashboardController {

    @FXML private Label labelSeancesJour;
    @FXML private Label labelPresents;
    @FXML private Label labelAbsents;
    @FXML private Label labelTaux;
    @FXML private LineChart<String, Number> lineChartSeances;
    @FXML private PieChart pieChartContrats;
    @FXML private TableView<SeancePlanifiee> tableAlertes;
    @FXML private TableColumn<SeancePlanifiee, String> colCoursAlerte;
    @FXML private TableColumn<SeancePlanifiee, String> colHeureAlerte;
    @FXML private TableColumn<SeancePlanifiee, String> colProfAlerte;

    private final SeanceDAO seanceDAO = new SeanceDAO();
    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();

    @FXML
    public void initialize() {
        configurerColonnesAlertes();
        chargerDonnees();

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(60), e -> chargerDonnees())
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void configurerColonnesAlertes() {
        colCoursAlerte.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getAssignation().getCours().getIntitule()));
        colHeureAlerte.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDateHeure().format(DateTimeFormatter.ofPattern("HH:mm"))));
        colProfAlerte.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getAssignation().getProfesseur().getNom()));
    }

    private void chargerDonnees() {
        new Thread(() -> {
            List<SeancePlanifiee> seancesJour = seanceDAO.findSeancesDuJour();
            int total = seancesJour.size();
            long realisees = seancesJour.stream()
                    .filter(s -> s.getStatut() == StatutSeance.REALISEE).count();
            long absents = total - realisees;
            double taux = total == 0 ? 0 : (realisees * 100.0 / total);

            List<SeancePlanifiee> sansPointage = seanceDAO.findSansPointageDebut();

            List<Professeur> actifs = professeurDAO.findAllActifs();
            long vacataires = actifs.stream().filter(p -> p.getTypeContrat() == TypeContrat.VACATAIRE).count();
            long permanents = actifs.stream().filter(p -> p.getTypeContrat() == TypeContrat.PERMANENT).count();

            Platform.runLater(() -> {
                labelSeancesJour.setText(String.valueOf(total));
                labelPresents.setText(String.valueOf(realisees));
                labelAbsents.setText(String.valueOf(absents));
                labelTaux.setText(String.format("%.0f%%", taux));
                tableAlertes.setItems(FXCollections.observableArrayList(sansPointage));
                pieChartContrats.setData(FXCollections.observableArrayList(
                        new PieChart.Data("Vacataires", vacataires),
                        new PieChart.Data("Permanents", permanents)
                ));
                remplirLineChart();
            });
        }).start();
    }

    private void remplirLineChart() {
        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Séances réalisées");

        for (int i = 5; i >= 0; i--) {
            LocalDate moisCible = LocalDate.now().minusMonths(i);
            String label = moisCible.getMonth().getDisplayName(TextStyle.SHORT, Locale.FRENCH);
            long count = compterSeancesRealiseesDuMois(moisCible.getMonthValue(), moisCible.getYear());
            serie.getData().add(new XYChart.Data<>(label, count));
        }
        lineChartSeances.getData().setAll(serie);
    }

    private long compterSeancesRealiseesDuMois(int mois, int annee) {
        return professeurDAO.findAllActifs().stream()
                .flatMap(p -> seanceDAO.findByProfesseurEtMois(p.getId(), mois, annee).stream())
                .filter(s -> s.getStatut() == StatutSeance.REALISEE)
                .count();
    }
}