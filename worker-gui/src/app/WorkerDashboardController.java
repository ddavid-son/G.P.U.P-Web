package app;

import app.Utils.HttpUtil;
import com.google.gson.reflect.TypeToken;
import dataTransferObjects.TaskInfoDTO;
import dataTransferObjects.UserDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import resources.Constants;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WorkerDashboardController {

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
    private TableColumn<TaskInfoDTO, ?> pricingCol;

    @FXML
    private TableColumn<TaskInfoDTO, Integer> simulationPriceCol;

    @FXML
    private TableColumn<TaskInfoDTO, Integer> compilationPriceCol;

    @FXML
    private TableView<UserDto> usersTable; // UserHistoryDto

    @FXML
    private TableColumn<UserDto, String> userNameCol;

    @FXML
    private TableColumn<UserDto, String> userRoleCol;

    @FXML
    private Button loadGraphBtn;

    @FXML
    private Label loggedInAs;

    @FXML
    private TableView<TaskInfoDTO> taskHeaderTable;

    @FXML
    private TableColumn<TaskInfoDTO, String> taskNameCol;

    @FXML
    private TableColumn<TaskInfoDTO, String> taskOwnerCol;

    @FXML
    private TableColumn<TaskInfoDTO, String> taskTypeCol;

    @FXML
    private TableColumn<TaskInfoDTO, Rectangle> TaskEnrolledCol;
    //private TableColumn<TaskInfoDTO, String> TaskEnrolledCol;

    private final Timer timer = new Timer();
    private final ObservableList<UserDto> userHistoryObsList = FXCollections.observableArrayList();

    @FXML
    void onLoadGraphBtnClicked(ActionEvent event) {

    }

    public void setDashBoard(String username) {
        initUserTable();
        scheduleUsersFetching();
        this.loggedInAs.setText("Hello " + username + "!");

        /*scheduleGraphNameFetching();
        graphListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.isEmpty()) {
                fetchGraphPeekFromServer(newValue);
            }
        });

        graphListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String s = graphListView.getSelectionModel().getSelectedItem();
                if (s != null && !s.isEmpty()) {
                    goToGraph(s);
                }
            }
        });*/
    }

    private void initUserTable() {
        userNameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUserName()));
        userRoleCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRole()));
        usersTable.setItems(userHistoryObsList);
    }

    private void initTaskPeekTable(TaskInfoDTO taskInfoDTO) {
        ObservableList<TaskInfoDTO> taskInfoObs = FXCollections.observableArrayList(taskInfoDTO);

        taskStatusCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getOwnerName()));
        taskListedCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getEnlistedWorkers()).asObject());
        independentCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getTotalNumberOfIndependents()).asObject());
        rootCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getTotalNumberOfRoots()).asObject());
        leafCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getTotalNumberOfLeaves()).asObject());
        middleCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getTotalNumberOfMiddles()).asObject());
        totalCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getTotalNumberOfTargets()).asObject());
        simulationPriceCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getSimulationPrice()).asObject());
        compilationPriceCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getCompilationPrice()).asObject());

        this.taskPeekTable.setItems(taskInfoObs);
    }

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
                System.out.println("Error: " + e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() == 200) {
                    updateUsersList(
                            HttpUtil.GSON.fromJson(
                                    response.body().string(),
                                    new TypeToken<java.util.List<UserDto>>() {
                                    }.getType()
                            ));
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
