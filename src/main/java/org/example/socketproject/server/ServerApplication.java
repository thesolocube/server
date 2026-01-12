package org.example.socketproject.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Application JavaFX pour le serveur de chat
 */
public class ServerApplication extends Application {

    private ServerController serverController;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(ServerApplication.class.getResource("/org/example/socketproject/server/server.fxml"));
        Parent root = loader.load();
        this.serverController = loader.getController();

        Scene scene = new Scene(root, 700, 500);
        stage.setTitle("Chat LAN - Serveur");
        stage.setScene(scene);

        // Quand on ferme la fenêtre, on arrête proprement le serveur
        stage.setOnCloseRequest(event -> {
            if (serverController != null) {
                serverController.shutdown();
            }
        });

        stage.show();
    }

    @Override
    public void stop() {
        if (serverController != null) {
            serverController.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}


