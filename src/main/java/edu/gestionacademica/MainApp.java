package edu.gestionacademica;

import edu.gestionacademica.controller.EstudianteController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        URL recurso = MainApp.class.getResource(
                "/edu/gestionacademica/estudiante-view.fxml"
        );

        if (recurso == null) {
            throw new IOException(
                    "No se encontró estudiante-view.fxml en "
                            + "src/main/resources/edu/gestionacademica/"
            );
        }

        FXMLLoader loader = new FXMLLoader(recurso);
        Parent root = loader.load();

        EstudianteController controller = loader.getController();

        Scene scene = new Scene(root, 1000, 680);

        stage.setTitle("Gestión Académica | Estudiantes");
        stage.setScene(scene);
        stage.setMinWidth(850);
        stage.setMinHeight(620);

        stage.setOnCloseRequest(evento -> {
            if (controller.estaOcupado()) {
                evento.consume();

                Alert alerta = new Alert(
                        Alert.AlertType.INFORMATION
                );

                alerta.initOwner(stage);
                alerta.setTitle("Operación en curso");
                alerta.setHeaderText(null);
                alerta.setContentText(
                        "Espere a que termine la operación "
                                + "antes de cerrar la aplicación."
                );
                alerta.showAndWait();
            }
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}