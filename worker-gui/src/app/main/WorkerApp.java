package app.main;

import app.WorkerLoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.net.URL;

public class WorkerApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(javafx.stage.Stage primaryStage) {

        try {
            primaryStage.setTitle("Gpup-worker");

            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/resources/fxml/workerLogin.fxml");
            fxmlLoader.setLocation(url);
            Parent loginRoot = fxmlLoader.load(url.openStream());
            Scene loginScene = new Scene(loginRoot);

            WorkerLoginController loginController = fxmlLoader.getController();
            loginController.setMainScreen(primaryStage);

            primaryStage.setScene(loginScene);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
