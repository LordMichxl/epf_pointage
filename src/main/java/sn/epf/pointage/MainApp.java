package sn.epf.pointage;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sn.epf.pointage.config.HibernateConfig;

public class MainApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        Scene scene = new Scene(loader.load(), 400, 500);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setTitle("EPF Africa — Système de Pointage");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        HibernateConfig.shutdown();
    }

    public static void main(String[] args) {
        launch(args);
    }
}