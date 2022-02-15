
package app.relatedView;

import app.mainScreen.ControlPanelController;
import app.util.http.HttpClientUtil;
import argumentsDTO.CommonEnums.*;
import com.google.gson.reflect.TypeToken;
import dataTransferObjects.WhatIfDTO;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import resources.Constants;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

public class RelatedViewController {

    @FXML
    private ListView<String> targetListView;

    @FXML
    private GridPane relatedTableView;

    @FXML
    private ListView<String> whatIfList;

    @FXML
    private Button relationIndicatorBtn;

    @FXML
    private CheckBox filterNonImmediateCB;

    RelationType relationType = RelationType.DEPENDS_ON;

    private ControlPanelController appController;

    private WhatIfDTO whatIfSelection = new WhatIfDTO();

    @FXML
    void switchRelationTypeClicked(ActionEvent event) {
        relationType = relationType == RelationType.DEPENDS_ON ?
                RelationType.REQUIRED_FOR :
                RelationType.DEPENDS_ON;
        relationIndicatorBtn.setText(relationType.toString().replace("_", " "));
        fetchWhatIf(whatIfSelection.getTargetName(), relationType, this::updateWhatIfView);
    }

    private void updateWhatIfView(WhatIfDTO whatIfRes) {
        whatIfSelection = whatIfRes;
        whatIfList.getItems().setAll(filterNonImmediateCB.isSelected() ?
                whatIfSelection.getAllImmediate() :
                whatIfSelection.getAllRelated());
    }

    public void fetchWhatIf(String targetName, RelationType relationType, Consumer<WhatIfDTO> consumer) {
        String finalUrl = HttpUrl.parse(Constants.FULL_SERVER_PATH + "/get-related")
                .newBuilder()
                .addQueryParameter("engine-name", appController.getEngineName())
                .addQueryParameter("target-name", targetName)
                .addQueryParameter("relation-type", relationType.toString())
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                appController.handleErrors(e, "", "Couldn't fetch data from server");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String s = response.body().string();
                if (response.code() != 200) {
                    appController.handleErrors(null, s, "Error fetching all Paths");
                } else {
                    Platform.runLater(() -> consumer.accept(
                            HttpClientUtil.GSON.fromJson(s, new TypeToken<WhatIfDTO>() {
                            }.getType())
                    ));
                }
            }
        });
    }


    @FXML
    void filterBtn(ActionEvent event) {

    }

    public void setAppController(ControlPanelController appController) {
        this.appController = appController;
    }

    public void loadTargetList(List<String> allTargets) {
        targetListView.getItems().addAll(allTargets);
        targetListView.setPlaceholder(new Text("No targets found"));

        targetListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                fetchWhatIf(newValue, relationType, this::updateWhatIfView));

        filterNonImmediateCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                whatIfList.getItems().setAll(whatIfSelection.getAllImmediate());
            } else {
                whatIfList.getItems().setAll(whatIfSelection.getAllRelated());
            }
        });
    }

}
