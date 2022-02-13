
package app.circleDisplay;

import app.mainScreen.ControlPanelController;
import app.util.http.HttpClientUtil;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import resources.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CircleDisplayController {
    @FXML
    private HBox targetChooserBox;

    @FXML
    private HBox circleDisplayBox;

    private ControlPanelController appController;
    private String engineName;


    public void setAppController(ControlPanelController appController, String engineName) {
        this.appController = appController;
        this.engineName = engineName;
        //getAllTargets(appController, engineName);
    }

   /* private void getAllTargets(ControlPanelController appController, String engineName) {
        String finalUrl = HttpUrl.parse(
                        Constants.FULL_SERVER_PATH + "/get-all-targets")
                .newBuilder()
                .addQueryParameter("engine-name", engineName)
                .toString();


        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                appController.handleErrors(e, "", "Error fetching targets");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    appController.handleErrors(null,
                            "Could not acquire targets, please try again",
                            "Error fetching targets");
                } else {
                    String s = response.body().string();
                    displayCircles(
                            HttpClientUtil.GSON.fromJson(s, new TypeToken<List<String>>() {
                            }.getType())
                    );
                }
            }
        });
    }*/

    public void displayCircles(List<String> allTargetNames) {
        //targetChooserBox.getChildren().addAll(createTextFields(allTargetNames, execution));
        Platform.runLater(() -> targetChooserBox.getChildren().addAll(createCircleNode(allTargetNames)));
    }

    private List<StackPane> createCircleNode(List<String> allTargetNames) {
        List<StackPane> circleNodes = new ArrayList<>();
        for (String targetName : allTargetNames) {
            Text text = new Text(targetName);
            text.setId("txt");
            Circle circle = new Circle(20, Paint.valueOf("#FF0000"));
            text.setBoundsType(TextBoundsType.LOGICAL_VERTICAL_CENTER);
            StackPane stack = new StackPane();
            stack.setId(targetName);
            stack.getChildren().addAll(circle, text);
            circleNodes.add(stack);
        }

        circleNodes.forEach(pane -> pane.setOnMouseClicked(event -> fetchCircle(pane.getId())));

        return circleNodes;
    }

    private void updateCircleView(String selectedTarget, List<String> selectedTargets) {
        Platform.runLater(() -> {
            circleDisplayBox.getChildren().setAll(
                    selectedTargets.stream()
                            .map(Text::new)
                            .collect(Collectors.toList()));
            if (selectedTargets.isEmpty())
                circleDisplayBox.getChildren().setAll(new Text(selectedTarget + " is not in a circle"));
        });
    }

    private void fetchCircle(String targetName) {
        String finalUrl = HttpUrl.parse(Constants.FULL_SERVER_PATH + "/find-circle")
                .newBuilder()
                .addQueryParameter("engine-name", engineName)
                .addQueryParameter("target-name", targetName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                appController.handleErrors(e, "", "Couldn't fetch circle data");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    appController.handleErrors(null, response.message(), "Couldn't fetch circle data");
                } else {
                    String[] s = response.body().string().split("~~~");
                    response.body().close();

                    updateCircleView(s[0],
                            HttpClientUtil.GSON.fromJson(s[1], new TypeToken<List<String>>() {
                            }.getType())
                    );
                }
            }
        });
    }
/*    private List<Text> createTextFields(List<String> allTargetNames, Engine execution) {
        List<Text> textFields = new ArrayList<>();
        for (String targetName : allTargetNames) {
            Text textField = new Text(targetName);
            textField.setId("txt");
            textFields.add(textField);
        }

        textFields.forEach(textField -> textField.setOnMouseClicked(event -> {
            List<String> selectedTargets = execution.findIfTargetIsInACircle(textField.getText());
            circleDisplayBox.getChildren().setAll(
                    selectedTargets.stream()
                            .map(Text::new)
                            .collect(Collectors.toList()));
            if (selectedTargets.isEmpty())
                circleDisplayBox.getChildren().setAll(new Text(textField.getText() + " is not in a circle"));
        }));

        return textFields;
    }*/
}
