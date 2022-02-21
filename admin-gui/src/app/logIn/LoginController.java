package app.logIn;

import app.dashboard.DashBoardController;
import app.util.FXUtils;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import app.util.http.HttpClientUtil;
import resources.Constants;

import java.io.IOException;
import java.net.URL;

public class LoginController {

    @FXML
    public TextField userNameTextField;

    @FXML
    public Label errorMessageLabel;


    private final StringProperty errorMessageProperty = new SimpleStringProperty();
    private Stage primaryStage;

    @FXML
    public void initialize() {
        errorMessageLabel.textProperty().bind(errorMessageProperty);
    }

    @FXML
    private void loginButtonClicked(ActionEvent event) {

        String userName = userNameTextField.getText().trim();
/*        if (userName.matches(".[0-9]$")) { // todo: need to be tested
            FXUtils.handleErrors(null, "Please enter name that doesnt end with a digit", "Invalid user name!");
            return;
        }*/

        if (userName.isEmpty()) {
            errorMessageProperty.set("User name is empty. You can't login with empty user name");
            return;
        }

        String finalUrl = HttpUrl
                .parse(Constants.FULL_SERVER_PATH + "/client-login")
                .newBuilder()
                .addQueryParameter("username", userName)
                .addQueryParameter("role", "admin")
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                Platform.runLater(() ->
                        errorMessageProperty.set("Something went wrong: " + e.getMessage())
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();//11
                if (response.code() != 200) {
                    response.body().close();
                    Platform.runLater(() ->
                            errorMessageProperty.set("Something went wrong: " + responseBody)
                    );
                } else {
                    Platform.runLater(() -> switchToDashBoard());
                }
            }
        });
    }

    private void switchToDashBoard() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource(Constants.DASHBOARD_FXML);
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            Scene scene = new Scene(root);
            DashBoardController controller = fxmlLoader.getController();
            controller.setDashBoard(userNameTextField.getText().replaceAll("[^a-zA-Z0-9]", ""));
            controller.setDashBoardController(primaryStage);
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void userNameKeyTyped(KeyEvent event) {
        errorMessageProperty.set("");
    }

    @FXML
    private void quitButtonClicked(ActionEvent e) {
        Platform.exit();
    }

    public void setMainScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}

