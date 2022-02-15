package app.dashboard;

import app.mainScreen.ControlPanelController;
import app.util.http.HttpClientUtil;
import com.google.gson.reflect.TypeToken;
import dataTransferObjects.GraphInfoDTO;
import dataTransferObjects.UserDto;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import resources.Constants;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DashBoardController {

    @FXML
    private ListView<String> graphListView;

    @FXML
    private ListView<String> pendingWorkListView;

    @FXML
    private TableView<GraphInfoDTO> graphPeekTable;

    @FXML
    private TableColumn<GraphInfoDTO, String> graphNameCol;

    @FXML
    private TableColumn<GraphInfoDTO, String> adminCol;

    @FXML
    private TableColumn<GraphInfoDTO, Integer> independentCol;

    @FXML
    private TableColumn<GraphInfoDTO, Integer> leafCol;

    @FXML
    private TableColumn<GraphInfoDTO, Integer> middleCol;

    @FXML
    private TableColumn<GraphInfoDTO, Integer> rootCol;

    @FXML
    private TableColumn<GraphInfoDTO, Integer> totalCol;

    @FXML
    private TableColumn<GraphInfoDTO, Integer> pricingCol;

    @FXML
    private TableColumn<GraphInfoDTO, Integer> simulationPriceCol;

    @FXML
    private TableColumn<GraphInfoDTO, Integer> compilationPriceCol;

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
    private ScrollPane dashboardScrollPane;

    private final Timer timer = new Timer();
    private final ObservableList<UserDto> userHistoryObsList = FXCollections.observableArrayList();
    private String userName;

    // ---------------------------------------------------- init ---------------------------------------------------- //
    public void setDashBoard(String username) {
        loggedInAs.setText("Hello " + username + "!");
        this.userName = username;
        initUserTable();
        scheduleUsersFetching();

        scheduleGraphNameFetching();
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
        });

        scheduleTasksFetching();
    }

    private void initUserTable() {
        userNameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUserName()));
        userRoleCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getRole()));
        usersTable.setItems(userHistoryObsList);
    }

    private void initGraphPeekTable(GraphInfoDTO graphInfoDTO) {
        ObservableList<GraphInfoDTO> graphInfoObs = FXCollections.observableArrayList(graphInfoDTO);

        adminCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getOwnerName()));
        graphNameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getGraphName()));
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

        this.graphPeekTable.setItems(graphInfoObs);
    }
    // ---------------------------------------------------- init ---------------------------------------------------- //


    // ---------------------------------------------------- init ---------------------------------------------------- //
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
        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleErrors(e, "", "Failed to fetch tasks names");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String s = response.body().string();
                if (response.isSuccessful()) {
                    List<String> taskNames =
                            HttpClientUtil.GSON.fromJson(s, new TypeToken<List<String>>() {
                            }.getType());
                    if (taskNames.size() != pendingWorkListView.getItems().size())
                        Platform.runLater(() -> pendingWorkListView.getItems().setAll(taskNames));
                }
            }
        });
    }
    // ---------------------------------------------------- init ---------------------------------------------------- //


    // ------------------------------------------------- peek graph ------------------------------------------------- //
    private void fetchGraphPeekFromServer(String selectedGraphName) {
        String url = Constants.FULL_SERVER_PATH + "/graph-peek" + "?graph-name=" + selectedGraphName;

        Request request = new Request.Builder()
                .url(url)
                .build();

        Callback cb = new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleErrors(
                        e,
                        "",
                        "Can't get graph peek data"
                );
            }

            @Override
            public void onResponse(@NotNull Call call, Response response) throws IOException {
                String responseString = response.body().string();
                if (response.code() != 200) {
                    handleErrors(
                            null,
                            responseString,
                            "Can't get graph peek data"
                    );
                } else {
                    GraphInfoDTO graphInfoDTO = HttpClientUtil.GSON.fromJson(responseString, GraphInfoDTO.class);
                    Platform.runLater(() -> initGraphPeekTable(graphInfoDTO));
                }
            }
        };

        HttpClientUtil.runAsync(request, cb);
    }
    // ------------------------------------------------- peek graph ------------------------------------------------- //


    // ----------------------------------------------- graph name table -----------------------------------------------//
    private void scheduleGraphNameFetching() {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchGraphNamesFromServer();
            }
        }, 0, Constants.REFRESH_RATE);
    }

    private void fetchGraphNamesFromServer() {
        String finalUrl = Constants.FULL_SERVER_PATH + "/get-graphs-names";
        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Error: " + e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String s = response.body().string();
                if (response.isSuccessful()) {
                    List<String> graphNames =
                            HttpClientUtil.GSON.fromJson(s, new TypeToken<List<String>>() {
                            }.getType());
                    if (graphNames.size() != graphListView.getItems().size())
                        Platform.runLater(() -> graphListView.getItems().setAll(graphNames));
                }
            }
        });
    }
    // ----------------------------------------------- graph name table -----------------------------------------------//


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
        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                System.out.println("Error: " + e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String s = response.body().string();
                if (response.code() == 200) {
                    updateUsersList(
                            HttpClientUtil.GSON.fromJson(s, new TypeToken<List<UserDto>>() {
                                    }.getType()
                            )
                    );
                }
            }
        });
    }

    private void updateUsersList(List<UserDto> usersNames) {
        if (usersNames.size() == userHistoryObsList.size())
            return;
        Platform.runLater(() -> {
            //userHistoryObsList = usersTable.getItems();
            userHistoryObsList.clear();
            userHistoryObsList.addAll(usersNames);
        });
    }
    // ------------------------------------------------- users update ------------------------------------------------//


    // ------------------------------------------------- graph loading -----------------------------------------------//
    @FXML
    void onLoadGraphBtnClicked(ActionEvent event) {
        File selectedFile = getXMLFile();

        if (selectedFile != null) {
            Request request = buildRequest(selectedFile);
            HttpClientUtil.runAsync(request, new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    System.out.println(call.request().body().toString());
                    handleErrors(e, e.getMessage(), "Error loading graph");
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    if (response.code() != 200) {
                        String errorMessage = response.body().string();
                        handleErrors(null, errorMessage, "Error loading graph");
                    } else {
                        System.out.println("Success");
                    }
                }
            });
        }
    }

    @NotNull
    private Request buildRequest(File selectedFile) {
        RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart(
                        "xml-file",
                        selectedFile.getName(),
                        RequestBody.create(
                                selectedFile,
                                MediaType.parse("application/octet-stream"))
                ).build();

        return new Request.Builder()
                .url(HttpUrl.parse(Constants.FULL_SERVER_PATH + "/loadXml")
                        .newBuilder()
                        .build()
                        .toString())
                .method("POST", body)
                .build();
    }

    private File getXMLFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load XML File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("XML Files", "*.XML")
        );
        return fileChooser.showOpenDialog(null);
    }
    // ------------------------------------------------- graph loading -----------------------------------------------//


    // ---------------------------------------------- control panel --------------------------------------------------//
    public void goToGraph(String graphName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/mainScreen.fxml"));
            Parent root = loader.load();
            ControlPanelController graphController = loader.getController();
            graphController.setControlPanel(graphName, dashboardScrollPane, userName);
            ((Stage) dashboardScrollPane.getScene().getWindow()).setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // ---------------------------------------------- control panel --------------------------------------------------//


    public void handleErrors(Exception e, String bodyMessage, String headerMessage) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error occurred");
            alert.setHeaderText(headerMessage);
            if (e != null) {
                alert.setContentText(e.getMessage());
            } else {
                alert.setContentText(bodyMessage);
            }
            alert.showAndWait();
        });
    }

}
