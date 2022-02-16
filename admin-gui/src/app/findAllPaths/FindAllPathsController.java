
package app.findAllPaths;

import app.mainScreen.ControlPanelController;
import app.util.http.HttpClientUtil;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import resources.Constants;

import java.io.IOException;
import java.util.List;

import static app.util.FXUtils.handleErrors;

public class FindAllPathsController {
    @FXML
    private ComboBox<String> srcComboBox;

    @FXML
    private ComboBox<String> dstComboBox;

    @FXML
    private Button swapDstSrcBtn;

    @FXML
    private ListView<String> pathsListView;

    private ControlPanelController appController;
    private String engineName;

    @FXML
    void OnDstCBDrop(ActionEvent event) {
    }

    @FXML
    void OnSrcCBDrop(ActionEvent event) {
    }

    public void setAppController(ControlPanelController appController) {
        this.engineName = appController.getEngineName();
        this.appController = appController;
    }

    @FXML
    public void OnSwapSrcToDestBtn(ActionEvent event) {
        String src = srcComboBox.getValue();
        String dst = dstComboBox.getValue();
        srcComboBox.setValue("");
        dstComboBox.setValue(src);
        srcComboBox.setValue(dst);
    }

    public void loadComboBoxes(List<String> allTargetNames) {
        srcComboBox.getItems().addAll(allTargetNames);
        dstComboBox.getItems().addAll(allTargetNames);

        setSrcComboBoxListener();
        setDstComboBoxListener();

        pathsListView.setPlaceholder(new Text("No paths found"));
    }

    private void setSrcComboBoxListener() {
        srcComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue) && !newValue.isEmpty()
                    && dstComboBox.getValue() != null && !dstComboBox.getValue().isEmpty()) {
                fetchAllPaths(newValue, dstComboBox.getValue());
            }
        });
    }

    private void setDstComboBoxListener() {
        dstComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue) && !newValue.isEmpty()
                    && srcComboBox.getValue() != null && !srcComboBox.getValue().isEmpty()) {
                fetchAllPaths(srcComboBox.getValue(), newValue);
            }
        });
    }

    private void updateAllPathsView(List<String> allPaths) {
        Platform.runLater(() -> pathsListView.getItems().setAll(allPaths));
    }

    private void fetchAllPaths(String src, String dst) {

        String finalUrl = HttpUrl.parse(Constants.FULL_SERVER_PATH + "/get-all-paths")
                .newBuilder()
                .addQueryParameter("engine-name", engineName)
                .addQueryParameter("src-target", src)
                .addQueryParameter("dst-target", dst)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleErrors(e, "", "Couldn't fetch data from server");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String s = response.body().string();
                    handleErrors(null, s, "Error fetching all Paths");
                } else {
                    String s = response.body().string();
                    updateAllPathsView(
                            HttpClientUtil.GSON.fromJson(s, new TypeToken<List<String>>() {
                            }.getType())
                    );
                }
            }
        });
    }
}
