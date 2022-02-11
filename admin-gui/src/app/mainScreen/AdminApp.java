package app.mainScreen;

import app.logIn.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class AdminApp extends Application {
    //private final String APP_FXML_INCLUDE_RESOURCE = "/resources/fxml/mainScreen.fxml";
    private final String LOGIN_SCREEN_FXML = "/resources/fxml/adminLogin.fxml";

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Gpup-Admin");

        FXMLLoader fxmlLoader = new FXMLLoader();
        URL url =  getClass().getResource(LOGIN_SCREEN_FXML);
        fxmlLoader.setLocation(url);
        Parent loginRoot = fxmlLoader.load(url.openStream());
        Scene loginScene = new Scene(loginRoot);

        LoginController loginController = fxmlLoader.getController();
        loginController.setMainScreen(primaryStage);

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
