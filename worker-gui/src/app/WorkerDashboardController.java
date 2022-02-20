package app;

import app.Utils.FXUtil;
import app.Utils.HttpUtil;
import app.engine.TargetEngine;
import com.google.gson.reflect.TypeToken;
import dataTransferObjects.TaskInfoDTO;
import dataTransferObjects.UserDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import resources.Constants;
import argumentsDTO.CommonEnums.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static app.Utils.FXUtil.handleErrors;

public class WorkerDashboardController {

    public TableColumn<TaskInfoDTO, Boolean> taskEnrolledCol;
    public HBox regHBox;
    @FXML
    private ScrollPane workerDashboardScrollPane;

    @FXML
    private TableView<TaskInfoDTO> taskPeekTable;

    @FXML
    private TableColumn<TaskInfoDTO, String> taskStatusCol;

    @FXML
    private TableColumn<TaskInfoDTO, Integer> taskListedCol;

    @FXML
    private TableColumn<TaskInfoDTO, Integer> independentCol;

    @FXML
    private TableColumn<TaskInfoDTO, Integer> leafCol;

    @FXML
    private TableColumn<TaskInfoDTO, Integer> middleCol;

    @FXML
    private TableColumn<TaskInfoDTO, Integer> rootCol;

    @FXML
    private TableColumn<TaskInfoDTO, Integer> totalCol;

    @FXML
    private TableColumn<TaskInfoDTO, Integer> pricingCol;

    @FXML
    private TableView<UserDto> usersTable; // UserHistoryDto

    @FXML
    private TableColumn<UserDto, String> userNameCol;

    @FXML
    private TableColumn<UserDto, String> userRoleCol;

    @FXML
    private Label loggedInAs;

    @FXML
    private ListView<String> taskHeaderTable;

    @FXML
    private TableColumn<TaskInfoDTO, String> taskOwnerCol;

    @FXML
    private TableColumn<TaskInfoDTO, String> taskTypeCol;

    @FXML
    private Label resLogLabel;

    @FXML
    private Label walletLabel;

    @FXML
    private Button leaveTaskBtn;

    @FXML
    private Button joinTaskBtn;

    private int cash = 0;
    private String workerName;
    private Stage primaryStage;
    private int numberOfThreads;
    private TargetEngine engine;
    private final Timer timer = new Timer();
    private final List<String> tasksImIn = new ArrayList<>();
    private final ObservableList<UserDto> userHistoryObsList = FXCollections.observableArrayList();


    @FXML
    void onLoadGraphBtnClicked(ActionEvent event) {

    }

    // init
    public void setDashBoard(String username, Stage primaryStage, int numberOfThreads) {
        initUserTable();
        scheduleUsersFetching();
        this.workerName = username;
        this.primaryStage = primaryStage;
        this.numberOfThreads = numberOfThreads;
        loggedInAs.setText("Hello " + username + "!");
        engine = new TargetEngine(numberOfThreads, workerName, this);

        joinTaskBtn.setGraphic(FXUtil.getIcon("/plusIcon.png", 24));
        leaveTaskBtn.setGraphic(FXUtil.getIcon("/minusIcon.png", 24));

        scheduleTasksFetching();
        taskHeaderTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                fetchGraphPeekFromServer(newValue);
            }
        });
    }

    private void fetchGraphPeekFromServer(String taskName) {
        String finalUrl = HttpUrl.parse(Constants.FULL_SERVER_PATH + "/get-task-peek")
                .newBuilder()
                .addQueryParameter("task-name", taskName)
                .addQueryParameter("user-name", workerName)
                .build()
                .toString();

        HttpUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleErrors(e, " ", "Error, couldn't fetch task data from server");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    TaskInfoDTO taskInfoDTOList = HttpUtil.GSON.fromJson(responseBody, new TypeToken<TaskInfoDTO>() {
                    }.getType());
                    Platform.runLater(() -> updateTaskPeekTable(taskInfoDTOList));
                } else {
                    handleErrors(null,
                            responseBody,
                            "Error, couldn't fetch task data from server"
                    );
                }
            }
        });
    }

    private void initUserTable() {
        userNameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUserName()));
        userRoleCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRole()));
        usersTable.setItems(userHistoryObsList);
    }

    private void updateTaskPeekTable(TaskInfoDTO taskInfoDTO) {
        ObservableList<TaskInfoDTO> taskInfoObs = FXCollections.observableArrayList(taskInfoDTO);

        if (taskInfoDTO.getTaskStatus() == TaskStatus.CANCELED || taskInfoDTO.getTaskStatus().equals(TaskStatus.FINISHED))
            engine.removeTask(taskInfoDTO.getTaskName());

        taskStatusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTaskStatus().toString())
        );
        taskListedCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getNumberOfListedWorker()).asObject()
        );
        independentCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getIndependentsCount()).asObject()
        );
        leafCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getLeafsCount()).asObject()
        );
        middleCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getMiddlesCount()).asObject()
        );
        rootCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getRootsCount()).asObject()
        );
        totalCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getTotalCount()).asObject()
        );
        pricingCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getPricePerTarget()).asObject()
        );
        taskTypeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTaskType().toString())
        );
        taskOwnerCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTaskOwner())
        );
        taskEnrolledCol.setCellValueFactory(cellData ->
                new SimpleBooleanProperty(cellData.getValue().isEnrolled())
        );

        taskPeekTable.setItems(taskInfoObs);
    }
    // init


    // ------------------------------------------------- task ------------------------------------------------//
    private void taskRegistration(String taskName, boolean joinOrLeave) {

        if (assertTaskCredentials(taskName, joinOrLeave)) {
            resLogLabel.setText(joinOrLeave ?
                    "You are already in " + taskName + " task" :
                    "You aren't part of " + taskName + " task");
            return;
        }

        String finalUrl = HttpUrl.parse(Constants.FULL_SERVER_PATH + "/task-registration")
                .newBuilder()
                .addQueryParameter("task-name", taskName)
                .addQueryParameter("user-name", workerName)
                .addQueryParameter("join", joinOrLeave ? "join" : "leave")
                .build()
                .toString();

        HttpUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleErrors(e, " ", "Error, couldn't " + (joinOrLeave ? "join" : "leave") + " task");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    Platform.runLater(() -> resLogLabel.setText("Successfully " + (joinOrLeave ? "joined" : "left") + " task " + taskName));
                    updateWorkFetching(joinOrLeave, taskName);
                } else {
                    handleErrors(null,
                            responseBody,
                            "Error, couldn't " + (joinOrLeave ? "join" : "leave") + " task"
                    );
                }
            }
        });
    }

    private void updateWorkFetching(boolean joinOrLeave, String taskName) {
        if (joinOrLeave) {
            engine.addTask(taskName);
        } else {
            engine.removeTask(taskName);
        }
    }

    private void scheduleTargetsFetching() {

    }

    private boolean assertTaskCredentials(String taskName, boolean joinOrLeave) {
        if (joinOrLeave) {
            return taskName != null && taskName.isEmpty() && !tasksImIn.contains(taskName) &&
                    !taskStatusCol.getText().equals("FINISHED") && !taskStatusCol.getText().equals("CANCELLED");
        } else {
            return taskName != null && taskName.isEmpty() && tasksImIn.contains(taskName);
        }
    }

    @FXML
    void onJoinTaskBtnClicked(ActionEvent event) {
        taskRegistration(taskHeaderTable.getSelectionModel().getSelectedItem(), true);
    }

    @FXML
    void onLeaveTaskBtnClicked(ActionEvent event) {
        taskRegistration(taskHeaderTable.getSelectionModel().getSelectedItem(), false);
    }

    public synchronized void updateWallet(int earned) {
        cash += earned;
        Platform.runLater(() -> walletLabel.setText("Wallet: " + cash));
    }
    // ------------------------------------------------- task ------------------------------------------------//


    // ------------------------------------------------- task update ------------------------------------------------//
    private void scheduleTasksFetching() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchTasksFromServer();
            }
        }, 0, Constants.REFRESH_RATE);

    }

    private void fetchTasksFromServer() {
        String finalUrl = Constants.FULL_SERVER_PATH + "/get-task-names";
        HttpUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleErrors(e, "", "Failed to fetch tasks names");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String s = response.body().string();
                if (response.isSuccessful()) {
                    List<String> taskNames =
                            HttpUtil.GSON.fromJson(s, new TypeToken<List<String>>() {
                            }.getType());
                    if (taskNames.size() != taskHeaderTable.getItems().size())
                        Platform.runLater(() -> taskHeaderTable.getItems().setAll(taskNames));
                }
            }
        });
    }
    // ------------------------------------------------- task update ------------------------------------------------//


    // ------------------------------------------------- users update ------------------------------------------------//
    private void scheduleUsersFetching() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchUsersFromServer();
            }
        }, 0, Constants.REFRESH_RATE);
    }

    private void fetchUsersFromServer() {
        String finalUrl = Constants.FULL_SERVER_PATH + "/usersHistory";
        HttpUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleErrors(e, "", "Error updating user list");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String s = response.body().string();
                if (response.code() == 200) {
                    updateUsersList(
                            HttpUtil.GSON.fromJson(
                                    s, new TypeToken<java.util.List<UserDto>>() {
                                    }.getType()
                            ));
                } else {
                    handleErrors(null, s, "Error updating user list");
                }
            }
        });
    }

    private void updateUsersList(List<UserDto> usersNames) {
        if (usersNames.size() == userHistoryObsList.size())
            return;
        Platform.runLater(() -> {
            userHistoryObsList.clear();
            userHistoryObsList.addAll(usersNames);
        });
    }
    // ------------------------------------------------- users update ------------------------------------------------//

}
