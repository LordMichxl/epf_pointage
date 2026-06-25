package sn.epf.pointage.ui;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import sn.epf.pointage.context.SessionContext;
import sn.epf.pointage.dao.ProfesseurDAO;
import sn.epf.pointage.dao.SeanceDAO;
import sn.epf.pointage.model.Professeur;
import sn.epf.pointage.model.RapportMensuel;
import sn.epf.pointage.model.SeancePlanifiee;
import sn.epf.pointage.model.Utilisateur;
import sn.epf.pointage.model.enums.Role;
import sn.epf.pointage.model.enums.StatutRapport;
import sn.epf.pointage.model.enums.StatutSeance;
import sn.epf.pointage.service.RapportService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RapportsController {

    @FXML private ComboBox<Professeur> comboProfesseur;
    @FXML private ComboBox<String> comboMois;
    @FXML private ComboBox<Integer> comboAnnee;
    @FXML private Label labelStatutRapport;
    @FXML private Button btnValider;
    @FXML private Button btnGenererPDF;
    @FXML private Label labelMessage;

    @FXML private TableView<SeancePlanifiee> tableSeances;
    @FXML private TableColumn<SeancePlanifiee, String> colDate;
    @FXML private TableColumn<SeancePlanifiee, String> colCours;
    @FXML private TableColumn<SeancePlanifiee, String> colDebut;
    @FXML private TableColumn<SeancePlanifiee, String> colFin;
    @FXML private TableColumn<SeancePlanifiee, String> colDuree;
    @FXML private TableColumn<SeancePlanifiee, String> colStatut;

    @FXML private Label labelNbSeances;
    @FXML private Label labelHeures;
    @FXML private Label labelMontant;
    @FXML private Label labelRetards;
    @FXML private Label labelProfNom;
    @FXML private Label labelProfMatricule;
    @FXML private Label labelProfContrat;
    @FXML private Label labelProfTaux;
    private Utilisateur utilisateur;
    private final ProfesseurDAO professeurDAO = new ProfesseurDAO();
    private final SeanceDAO     seanceDAO     = new SeanceDAO();
    private final RapportService rapportService = new RapportService();

    private RapportMensuel rapportCourant;

    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HM   = DateTimeFormatter.ofPattern("HH:mm");

    private static final String[] MOIS_NOMS = {
        "Janvier","Février","Mars","Avril","Mai","Juin",
        "Juillet","Août","Septembre","Octobre","Novembre","Décembre"
    };

    @FXML
    public void initialize() {
        configurerColonnes();
        peuplerComboMois();
        peuplerComboAnnee();
        peuplerComboProfesseur();
        btnValider.setDisable(true);
        btnGenererPDF.setDisable(true);
    }

    private void peuplerComboMois() {
        comboMois.setItems(FXCollections.observableArrayList(MOIS_NOMS));
        comboMois.getSelectionModel().select(LocalDateTime.now().getMonthValue() - 1);
    }

    private void peuplerComboAnnee() {
        int anneeActuelle = LocalDateTime.now().getYear();
        comboAnnee.setItems(FXCollections.observableArrayList(
            anneeActuelle - 2, anneeActuelle - 1, anneeActuelle));
        comboAnnee.getSelectionModel().selectLast();
    }

    private void peuplerComboProfesseur() {
        Utilisateur user = SessionContext.getInstance().getUtilisateurConnecte();

        if (user != null && user.getRole() == Role.PROFESSEUR) {
            comboProfesseur.setItems(FXCollections.observableArrayList(user.getProfesseurLie()));
            comboProfesseur.getSelectionModel().selectFirst();
            comboProfesseur.setDisable(true);
            afficherInfoProf(user.getProfesseurLie());
        } else {
            List<Professeur> profs = professeurDAO.findAllActifs();
            comboProfesseur.setItems(FXCollections.observableArrayList(profs));

            // Afficher nom + matricule dans la ComboBox
            comboProfesseur.setCellFactory(lv -> new ListCell<>() {
                @Override protected void updateItem(Professeur p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty || p == null ? null : p.getNom() + " " + p.getPrenom()
                        + " (" + p.getMatricule() + ")");
                }
            });
            comboProfesseur.setButtonCell(new ListCell<>() {
                @Override protected void updateItem(Professeur p, boolean empty) {
                    super.updateItem(p, empty);
                    setText(empty || p == null ? null : p.getNom() + " " + p.getPrenom());
                }
            });

            comboProfesseur.valueProperty().addListener((obs, old, prof) -> {
                if (prof != null) afficherInfoProf(prof);
            });
        }
    }

    @FXML
    private void genererRapport() {
        Professeur prof  = comboProfesseur.getValue();
        int moisIndex    = comboMois.getSelectionModel().getSelectedIndex();
        Integer annee    = comboAnnee.getValue();

        if (prof == null || moisIndex < 0 || annee == null) {
            afficherMessage(" Sélectionne un professeur, un mois et une année.", "#D35400");
            return;
        }

        int mois = moisIndex + 1;

        try {
            // Appel au service — qui vérifie RG-06
            rapportCourant = rapportService.genererRapportMensuel(prof.getId(), mois, annee);

            if (rapportCourant == null) {
                afficherMessage(" Impossible de générer : des séances du mois sont encore " +
                    "en statut PLANIFIEE (RG-06).", "#D35400");
                return;
            }

            List<SeancePlanifiee> seances = seanceDAO.findByProfesseurEtMois(prof.getId(), mois, annee)
                .stream().filter(s -> s.getStatut() == StatutSeance.REALISEE).toList();

            tableSeances.setItems(FXCollections.observableArrayList(seances));
            labelNbSeances.setText(String.valueOf(seances.size()));
            labelHeures.setText(String.format("%.2f h", rapportCourant.getHeuresRealisees()));
            labelMontant.setText(String.format("%,d XOF", rapportCourant.getMontantXOF()));
            afficherStatutRapport(rapportCourant.getStatut());

            Utilisateur user = SessionContext.getInstance().getUtilisateurConnecte();
            boolean peutValider = user != null
                && (user.getRole() == Role.ADMIN || user.getRole() == Role.SCOLARITE)
                && rapportCourant.getStatut() == StatutRapport.EN_ATTENTE;
            btnValider.setDisable(!peutValider);
            btnGenererPDF.setDisable(false);

            afficherMessage(" Rapport généré pour " + MOIS_NOMS[moisIndex] + " " + annee, "#1E8449");

        } catch (Exception e) {
            afficherMessage(" Erreur : " + e.getMessage(), "#C0392B");
        }
    }

    @FXML
    private void validerRapport() {
        if (rapportCourant == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Valider le rapport");
        alert.setHeaderText("Confirmer la validation ?");
        alert.setContentText("Cette action passe le rapport au statut VALIDE. " +
            "Elle ne peut pas être annulée.");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    rapportService.validerRapport(rapportCourant.getId());
                    rapportCourant.setStatut(StatutRapport.VALIDE);
                    afficherStatutRapport(StatutRapport.VALIDE);
                    btnValider.setDisable(true);
                    afficherMessage(" Rapport validé avec succès !", "#1E8449");
                } catch (Exception e) {
                    afficherMessage(" Erreur validation : " + e.getMessage(), "#C0392B");
                }
            }
        });
    }

    @FXML
    private void exporterPDF() {
        if (rapportCourant == null) return;
        try {
            rapportService.exporterPDF(rapportCourant.getId());
            afficherMessage(" PDF généré et ouvert !", "#1E8449");
        } catch (Exception e) {
            afficherMessage(" Erreur PDF : " + e.getMessage(), "#C0392B");
        }
    }

    private void configurerColonnes() {
        colDate.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                SeancePlanifiee s = getTableView().getItems().get(getIndex());
                setText(s.getDateHeure().format(FMT_DATE));
            }
        });

        colCours.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                SeancePlanifiee s = getTableView().getItems().get(getIndex());
                try { setText(s.getAssignation().getCours().getIntitule()); }
                catch (Exception e) { setText("—"); }
            }
        });

        colDebut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                SeancePlanifiee s = getTableView().getItems().get(getIndex());
                setText(s.getDateHeure().format(FMT_HM));
            }
        });

        colFin.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                SeancePlanifiee s = getTableView().getItems().get(getIndex());
                if (s.getDureeMinutes() != null) {
                    setText(s.getDateHeure().plusMinutes(s.getDureeMinutes()).format(FMT_HM));
                } else setText("—");
            }
        });

        colDuree.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                SeancePlanifiee s = getTableView().getItems().get(getIndex());
                if (s.getDureeMinutes() != null) {
                    double h = s.getDureeMinutes() / 60.0;
                    setText(String.format("%.2f", h));
                } else setText("—");
            }
        });

        colStatut.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setStyle(""); return; }
                setText(" Réalisée");
                setStyle("-fx-text-fill: #1E8449; -fx-font-weight: bold;");
            }
        });
    }

    private void afficherInfoProf(Professeur prof) {
        if (prof == null) return;
        labelProfNom.setText(prof.getNom() + " " + prof.getPrenom());
        labelProfMatricule.setText("Matricule : " + prof.getMatricule());
        labelProfContrat.setText("Contrat : " + prof.getTypeContrat());
        labelProfTaux.setText("Taux : " + (prof.getTauxHoraireXOF() != null
            ? String.format("%,.0f XOF/h", prof.getTauxHoraireXOF()) : "—"));
    }

    private void afficherStatutRapport(StatutRapport statut) {
        switch (statut) {
            case EN_ATTENTE -> {
                labelStatutRapport.setText(" En attente de validation");
                labelStatutRapport.setStyle("-fx-text-fill: #D35400;");
            }
            case VALIDE -> {
                labelStatutRapport.setText(" Validé");
                labelStatutRapport.setStyle("-fx-text-fill: #1E8449; -fx-font-weight: bold;");
            }
            case PAYE -> {
                labelStatutRapport.setText(" Payé");
                labelStatutRapport.setStyle("-fx-text-fill: #241654; -fx-font-weight: bold;");
            }
        }
    }

    private void afficherMessage(String msg, String couleur) {
        labelMessage.setText(msg);
        labelMessage.setStyle("-fx-text-fill: " + couleur + ";");
    }
}
