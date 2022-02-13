package app;

import app.Utils.FXUtil;
import app.Utils.HttpUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import resources.Constants;

import java.io.IOException;
import java.net.URL;

public class WorkerLoginController {

    public HBox threadsHBox;
    public Spinner<Integer> threadsSpinner;
    public TextField userNameTF;

    @FXML
    private Button loginBtn;
    private Stage primaryStage;

    @FXML
    public void initialize() {
        threadsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1)
        );
    }

    public void onLoginBtnClicked(ActionEvent actionEvent) {

        int threads = threadsSpinner.getValue();
        String userName = userNameTF.getText();
        System.out.println(userName);
        userName = userName.replaceAll("[^a-zA-Z0-9]", "");
        System.out.println(userName);

        if (!assertParameters(threads, userName)) {
            FXUtil.handleErrors(null,
                    "Legal values are:\n" +
                            "threads number between 1 - 5\n" +
                            "username at least 1 character ",
                    "Error, Invalid parameter(s)!");
            return;
        }

        String finalUrl = HttpUrl.parse(
                        Constants.FULL_SERVER_PATH + "/client-login")
                .newBuilder()
                .addQueryParameter("threads", String.valueOf(threads))
                .addQueryParameter("username", userName)
                .addQueryParameter("role", "worker")
                .build()
                .toString();

        HttpUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                FXUtil.handleErrors(e, "", "Error while logging in");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.code() != 200) {
                    System.out.println(response.code());
                    try {
                        System.out.println(response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println(call);
                    System.out.println(call.request().toString());
                    FXUtil.handleErrors(
                            null,
                            "couldn't log in due to authentication errors pleas try again later",
                            "Error while logging in");
                } else {
                    Platform.runLater(() -> switchToDashBoard());
                }
            }
        });
    }

    private boolean assertParameters(int threadsNumber, String userName) {
        return threadsNumber < 6 && threadsNumber > 0 &&
                userName != null && !userName.isEmpty();
    }

    private void switchToDashBoard() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource(Constants.DASHBOARD_FXML);
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            Scene scene = new Scene(root);
            WorkerDashboardController controller = fxmlLoader.getController();
            controller.setDashBoard(userNameTF.getText().replaceAll("[^a-zA-Z0-9]", ""));
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setMainScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
