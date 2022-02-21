package app;

import app.Utils.FXUtil;
import app.Utils.HttpUtil;
import app.engine.TargetEngine;
import com.google.gson.reflect.TypeToken;
import dataTransferObjects.WorkHistoryDto;
import dataTransferObjects.WorkerTaskInfoDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import resources.Constants;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DataCenterController {

    @FXML
    private Button backToDashboardBtn;

    @FXML
    private Label threadCapacityLabel;

    @FXML
    private TableView<WorkHistoryDto> allTargetsDataTable;

    @FXML
    private TableColumn<WorkHistoryDto, String> targetNameCol;

    @FXML
    private TableColumn<WorkHistoryDto, String> taskNameCol;

    @FXML
    private TableColumn<WorkHistoryDto, String> taskTypeCol;

    @FXML
    private TableColumn<WorkHistoryDto, String> targetStatusCol;

    @FXML
    private TableColumn<WorkHistoryDto, Integer> targetValueCol;

    @FXML
    private TableColumn<WorkHistoryDto, String> targetLogsCol;

    @FXML
    private TableView<WorkerTaskInfoDto> workerTaskInfoTable;

    @FXML
    private TableColumn<WorkerTaskInfoDto, String> liveTaskNameCol;

    @FXML
    private TableColumn<WorkerTaskInfoDto, Integer> liveEnrolledWorkersCol;

    @FXML
    private TableColumn<WorkerTaskInfoDto, ProgressBar> liveProgressCol;

    @FXML
    private TableColumn<WorkerTaskInfoDto, Integer> liveDoneByMeCol;

    @FXML
    private TableColumn<WorkerTaskInfoDto, Integer> liveCreditsCol;


    private Stage primaryStage;
    private Scene dashboardStage;
    private TargetEngine targetEngine;
    private Timer timerForTaskInfo = new Timer();
    private Timer timerForEngienInfo = new Timer();
    private WorkerDashboardController dashboardController;

    @FXML
    void onBackToDashboardBtnClicked(ActionEvent event) {
        primaryStage.setScene(dashboardStage);
    }

    @FXML
    public void initialize() {
    }

    private void initAllTargetsDataTable(List<WorkHistoryDto> workHistoryDto) {

        ObservableList<WorkHistoryDto> allTargetsData = FXCollections.observableArrayList(workHistoryDto);

        targetNameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTargetName())
        );
        taskNameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTaskName())
        );
        taskTypeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTaskType())
        );
        targetStatusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTargetState())
        );
        targetValueCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getTargetValue()).asObject()
        );
        targetLogsCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTargetLogs())
        );

        allTargetsDataTable.setItems(allTargetsData);
    }

    public void setDataCenter(Stage primaryStage, WorkerDashboardController dashboardController, TargetEngine targetEngine) {
        this.primaryStage = primaryStage;
        this.dashboardStage = primaryStage.getScene();
        this.targetEngine = targetEngine;
        this.dashboardController = dashboardController;

        backToDashboardBtn.setGraphic(FXUtil.getIcon("/backHome.png", 35));
        targetEngine.setDataCenterController(this);

        scheduleEngineInfo(targetEngine);
        scheduleWorkerTasksInfo();
    }

    public void updateNumberOfThreads(int numberOfThreads) {
        Platform.runLater(() -> {
            threadCapacityLabel.setText(
                    "# of threads: " +
                            targetEngine.getActiveThreads() + "/" +
                            dashboardController.getNumberOfThreads());
        });
    }

    private void scheduleWorkerTasksInfo() {
        timerForTaskInfo.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchWorkerTaskInfo();
            }
        }, 0, Constants.REFRESH_RATE);
    }

    private void fetchWorkerTaskInfo() {
        String finalUrl = HttpUrl.parse(Constants.FULL_SERVER_PATH + "/worker-task-info")
                .newBuilder()
                .addQueryParameter("username", dashboardController.getWorkerName())
                .build()
                .toString();


        HttpUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                FXUtil.handleErrors(e, "", "Failed to fetch worker task info");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    List<WorkerTaskInfoDto> workerTaskInfoDtoList =
                            HttpUtil.GSON.fromJson(responseBody, new TypeToken<List<WorkerTaskInfoDto>>() {
                            }.getType());

                    updateTaskInfoTale(workerTaskInfoDtoList);
                } else {
                    FXUtil.handleErrors(null, responseBody, "Failed to fetch worker task info");
                }
            }
        });
    }

    private void updateTaskInfoTale(List<WorkerTaskInfoDto> workerTaskInfoDtoList) {
        Platform.runLater(() -> {
            ObservableList<WorkerTaskInfoDto> obsList = FXCollections.observableArrayList(workerTaskInfoDtoList);

            liveTaskNameCol.setCellValueFactory(cellData ->
                    new SimpleStringProperty(cellData.getValue().getTaskName())
            );
            liveEnrolledWorkersCol.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(cellData.getValue().getEnrolledUsers()).asObject()
            );
            liveProgressCol.setCellValueFactory(cellData ->
                    new SimpleObjectProperty<>(cellData.getValue().getTaskProgressBar())
            );
            liveDoneByMeCol.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(targetEngine.getCountTargetDoneByMe(cellData.getValue().taskName)).asObject()
            );
            liveCreditsCol.setCellValueFactory(cellData ->
                    new SimpleIntegerProperty(
                            targetEngine.getCountTargetDoneByMe(
                                    cellData.getValue().taskName) * cellData.getValue().targetPrice)
                            .asObject()
            );
            workerTaskInfoTable.setItems(obsList);
        });
    }

    private void scheduleEngineInfo(TargetEngine targetEngine) {
        timerForEngienInfo.schedule(new TimerTask() {
            @Override
            public void run() {
                initAllTargetsDataTable(targetEngine.getWorkHistoryDto());
            }
        }, 0, 1000);
    }
}
