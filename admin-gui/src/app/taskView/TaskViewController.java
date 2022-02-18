package app.taskView;

import app.taskView.summaryWindow.SummaryController;
import app.util.http.HttpClientUtil;
import argumentsDTO.CommonEnums.*;
import argumentsDTO.*;
import com.google.gson.reflect.TypeToken;
import dataTransferObjects.UpdateListsDTO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import resources.Constants;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static app.util.FXUtils.handleErrors;

public class TaskViewController {

    @FXML
    private ListView<StackPane> frozenList;

    @FXML
    private GridPane userInputGridPane;

    @FXML
    private ListView<StackPane> waitingList;

    @FXML
    private ListView<StackPane> failedList;

    @FXML
    private ListView<StackPane> skippedList;

    @FXML
    private ListView<StackPane> finishedList;

    @FXML
    private ListView<StackPane> inProcessList;

    @FXML
    private TextArea logListViw;

    @FXML
    private Button goHomeBtn;

    @FXML
    private Button playPauseBtn;

    @FXML
    private Spinner<Integer> numberOfThreadsSpinner;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label taskTypeHeaderLabel;

    @FXML
    private Label numberOfThreadsLabel;

    @FXML
    private Label isIncrementalLabel;

    @FXML
    private Button setNewThreadsBtn;

    @FXML
    private Button abortTaskBtn;

    @FXML
    private Label taskTerminatedLabel;

    private Label isRandomLabel;

    private int totalNumberOfTargets;
    private int finishedNumberTargets = 0;

    List<StackPane> frozenListItems = new ArrayList<>();
    List<StackPane> waitingListItems = new ArrayList<>();
    List<StackPane> failedListItems = new ArrayList<>();
    List<StackPane> skippedListItems = new ArrayList<>();
    List<StackPane> finishedListItems = new ArrayList<>();
    List<StackPane> inProcessListItems = new ArrayList<>();
    List<String> allFinishedTasks = new ArrayList<>(); // dedicated to the progress bar
    List<StackPane> allTargets = new ArrayList<>();

    ObservableList<StackPane> obsWaitingList = FXCollections.observableArrayList();
    ObservableList<StackPane> obsFailedList = FXCollections.observableArrayList();
    ObservableList<StackPane> obsSkippedList = FXCollections.observableArrayList();
    ObservableList<StackPane> obsFinishedList = FXCollections.observableArrayList();
    ObservableList<StackPane> obsInProcessList = FXCollections.observableArrayList();
    ObservableList<StackPane> obsFrozenList = FXCollections.observableArrayList();
    ObservableList<StackPane> obsAllTargets = FXCollections.observableArrayList();

/*    ObservableList<StackPane> obsWaitingList;
    ObservableList<StackPane> obsFailedList;
    ObservableList<StackPane> obsSkippedList;
    ObservableList<StackPane> obsFinishedList;
    ObservableList<StackPane> obsInProcessList;
    ObservableList<StackPane> obsFrozenList;
    ObservableList<StackPane> obsAllTargets;*/

    private boolean paused = false;
    private final Random r = new Random();
    List<String> summery = new ArrayList<>();
    private String taskName;
    private Stage primaryStage;
    private Scene dashboardScene;
    private Timer timerForListUpdates = new Timer();

    @FXML
    void onGoHomeBtnClicked(ActionEvent event) {
        primaryStage.setScene(dashboardScene);
    }

    @FXML
    void onAbortTaskBtnClicked(ActionEvent event) {
        changeTaskStatuse(TaskStatus.CANCELED);
    }

    @FXML
    void onPlayPauseBtnClicked(ActionEvent event) {
        playPauseBtn.setDisable(true);
        if (paused) {
            changeTaskStatuse(TaskStatus.PAUSED);
        } else {
            changeTaskStatuse(TaskStatus.ACTIVE);
        }
    }

    private void setPlayPauseBtnStatuse(String statuse) {
        Platform.runLater(() -> {
            if ("PAUSED".equals(statuse) && !abortTaskBtn.disabledProperty().get()) {
                playPauseBtn.setGraphic(getIcon("/playBtnIcon.png", 35));
                paused = !paused;
                playPauseBtn.setDisable(false);

            } else if ("ACTIVE".equals(statuse) && !abortTaskBtn.disabledProperty().get()) {
                playPauseBtn.setGraphic(getIcon("/pauseBtnIcon.png", 35));
                paused = !paused;
                playPauseBtn.setDisable(false);
            } else {
                playPauseBtn.setDisable(true);
                abortTaskBtn.setDisable(true);
                timerForListUpdates.cancel();
                taskTerminatedLabel.setVisible(true);
            }
        });
    }

    private void changeTaskStatuse(TaskStatus taskStatus) {
        String finalUrl = HttpUrl.parse(Constants.FULL_SERVER_PATH + "/change-task-statuse")
                .newBuilder()
                .addQueryParameter("task-statuse", taskStatus.toString())
                .addQueryParameter("task-name", taskName)
                .build()
                .toString();

        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleErrors(e, " ", "Error! could not set the statuse");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String s = response.body().string();
                if (response.isSuccessful()) {
                    setPlayPauseBtnStatuse(taskStatus.toString());
                } else {
                    handleErrors(null, s, "Error! could not set the requested statuse");
                }
            }
        });

    }

    @FXML
    void onSetNewThreadsBtnClicked(ActionEvent event) {
        //appController.setNumberOfThreads(numberOfThreadsSpinner.getValue());
    }

    public void setTaskView(TaskArgs taskArgs) {
        totalNumberOfTargets = taskArgs.getTargetsSelectedForGraph().size();
        setLabelsAccordingToUserInput(taskArgs);
        handleListAndObservables(taskArgs);
        taskName = taskArgs.getTaskName();

        numberOfThreadsSpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1)
        );

        playPauseBtn.setGraphic(getIcon("/playBtnIcon.png", 35));
        progressBar.setProgress(0F);
        scheduleTargetsUpdate();
    }

    public Node getIcon(String resourceName, int size) {
        Image image = new Image("/resources/icons" + resourceName);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        return imageView;
    }

    // ---------------------------------  lists updating ----------------------------------------
    private void scheduleTargetsUpdate() {
        timerForListUpdates.schedule(new TimerTask() {
            @Override
            public void run() {
                fetchListsFromServer();
            }
        }, Constants.REFRESH_RATE, Constants.REFRESH_RATE);
    }

    public void fetchListsFromServer() {
        System.out.println("fetching list from server");
        String url = HttpUrl.parse(Constants.FULL_SERVER_PATH + "/get-state-lists")
                .newBuilder()
                .addQueryParameter("task-name", taskName)
                .build()
                .toString();

        HttpClientUtil.runAsync(url, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleErrors(e, "", "Error! couldn't fetch lists from server");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                if (response.isSuccessful()) {
                    updateLists(
                            HttpClientUtil.GSON.fromJson(responseBody, new TypeToken<UpdateListsDTO>() {
                            }.getType())
                    );
                }
            }
        });
    }

    private void updateLists(UpdateListsDTO updateListsDTO) {
/*  in case the jat is not the one updating the lists
        frozenListItems = listToCircles(updateListsDTO.getFrozenAsList(), "FROZEN");
        waitingListItems = listToCircles(updateListsDTO.getWaitingAsList(), "WAITING");
        failedListItems = listToCircles(updateListsDTO.getFailedAsList(), "FAILURE");
        skippedListItems = listToCircles(updateListsDTO.getSkippedAsList(), "SKIPPED");
        inProcessListItems = listToCircles(updateListsDTO.getInProcessAsList(), "IN_PROCESS");
        finishedListItems = listToCircles(updateListsDTO.getWarningAsList(), "WARNING");
        finishedListItems.addAll(listToCircles(updateListsDTO.getSuccessAsList(), "SUCCESS"));
*/
        Platform.runLater(() -> {
            obsFrozenList.setAll(listToCircles(updateListsDTO.getFrozenAsList(), "FROZEN"));
            obsWaitingList.setAll(listToCircles(updateListsDTO.getWaitingAsList(), "WAITING"));
            obsInProcessList.setAll(listToCircles(updateListsDTO.getInProcessAsList(), "IN_PROCESS"));
            obsSkippedList.setAll(listToCircles(updateListsDTO.getSkippedAsList(), "SKIPPED"));
            obsFailedList.setAll(listToCircles(updateListsDTO.getFailedAsList(), "FAILURE"));
            obsFinishedList.setAll(listToCircles(updateListsDTO.getWarningAsList(), "WARNING"));

            obsFinishedList.addAll(listToCircles(updateListsDTO.getSuccessAsList(), "SUCCESS"));
        });
    }

    private List<StackPane> listToCircles(List<String> list, String state) {

        List<StackPane> circles = new ArrayList<>();
        for (String name : list) {
            StackPane circle = createCircle(name, stateToColor(state));
            circles.add(circle);
        }
        return circles;
    }

    private StackPane createCircle(String name, Color color) {
        StackPane stackPane = new StackPane();
        Label targetName = new Label(name);
        targetName.resize(90, 30);
        Circle circle = new Circle(50, color);
        Button transparentBtn = new Button();
        transparentBtn.setMinHeight(80);
        transparentBtn.setPrefHeight(80);
        transparentBtn.setMaxHeight(80);

        transparentBtn.setMinWidth(80);
        transparentBtn.setPrefWidth(80);
        transparentBtn.setMaxWidth(80);

        transparentBtn.opacityProperty().setValue(0);
        stackPane.getChildren().add(circle);
        stackPane.getChildren().add(targetName);
        stackPane.getChildren().add(transparentBtn);
        stackPane.setId(name);

        return stackPane;
    }

    private Color stateToColor(String state) {
        switch (state) {
            case "IN_PROGRESS":
                return Color.ORANGE;
            case "SKIPPED":
                return Color.GRAY;
            case "FAILURE":
                return Color.RED;
            case "FROZEN":
                return Color.BLUE;
            case "WAITING":
                return Color.PINK;
            case "WARNING":
                return Color.YELLOW;
            case "SUCCESS":
                return Color.LIMEGREEN;
            default:
                return Color.valueOf("#FF0000");
        }
    }
// ---------------------------------  lists updating ----------------------------------------

    private void setLabelsAccordingToUserInput(TaskArgs taskArgs) {
        taskTypeHeaderLabel.setText(taskArgs.getTaskType().toString());
        numberOfThreadsLabel.setText("Number of threads: " + taskArgs.getNumOfThreads());
        isIncrementalLabel.setText("Task will be performed: " + (taskArgs.isIncremental() ?
                "Incrementally" :
                "From scratch")
        );

        if (taskArgs.getTaskType() == TaskType.COMPILATION) {
            CompilationArgs compilationArgs = (CompilationArgs) taskArgs;
            Label srcFolderLabel = new Label("Files Will Be Taken From: " + compilationArgs.getSrcPath());
            Label destFolderLabel = new Label("Compiled Files Will Be Saved In: " + compilationArgs.getDstPath());

            userInputGridPane.add(srcFolderLabel, 4, 0);
            userInputGridPane.add(destFolderLabel, 4, 1);

            GridPane.setValignment(srcFolderLabel, VPos.TOP);
            GridPane.setValignment(destFolderLabel, VPos.TOP);
            GridPane.setColumnSpan(srcFolderLabel, 6);
            GridPane.setColumnSpan(destFolderLabel, 6);

            GridPane.setHalignment(srcFolderLabel, HPos.LEFT);
            GridPane.setHalignment(destFolderLabel, HPos.LEFT);
        } else {
            SimulationArgs simulationArgs = (SimulationArgs) taskArgs;
            Label successRateLabel = new Label("Success Rate: " + simulationArgs.getSuccessRate());
            Label warningRateLabel = new Label("Warning Rate: " + simulationArgs.getWarningRate());
            Label sleepTimeLabel = new Label("Sleep Time" +
                    (simulationArgs.isRandom() ? " <= " : ": ") +
                    simulationArgs.getSleepTime()
            );

            userInputGridPane.add(successRateLabel, 4, 0);
            userInputGridPane.add(warningRateLabel, 4, 1);
            userInputGridPane.add(sleepTimeLabel, 4, 2);

            GridPane.setValignment(successRateLabel, VPos.TOP);
            GridPane.setValignment(warningRateLabel, VPos.TOP);
            GridPane.setValignment(sleepTimeLabel, VPos.TOP);
            GridPane.setColumnSpan(successRateLabel, 6);
            GridPane.setColumnSpan(warningRateLabel, 6);
            GridPane.setColumnSpan(sleepTimeLabel, 6);

            GridPane.setHalignment(successRateLabel, HPos.LEFT);
            GridPane.setHalignment(warningRateLabel, HPos.LEFT);
            GridPane.setHalignment(sleepTimeLabel, HPos.LEFT);
        }
    }


    private void handleListAndObservables(TaskArgs taskArgs) {
        inProcessList.setItems(obsInProcessList);
        finishedList.setItems(obsFinishedList);
        skippedList.setItems(obsSkippedList);
        failedList.setItems(obsFailedList);
        waitingList.setItems(obsWaitingList);
        frozenList.setItems(obsFrozenList);

/*        for (StackPane target : obsAllTargets) {
            ((Button) target.getChildren().get(2)).onActionProperty().setValue(event -> {
                getInfoAboutTargetInExecution(target);
            });
        }*/
        obsFrozenList.addAll(obsAllTargets);
    }

    private void getInfoAboutTargetInExecution(StackPane target) {
        new Thread(() -> {
            //prints the log to the user screen
            /*publishToUser(
                    execution.getInfoAboutTargetInExecution(
                            target.getId(),
                            cts(((Circle) target.getChildren().get(0)).getFill())
                    ));*/
            System.out.println("Target " + target.getId() + " was clicked");
        }).start();
    }

    private void publishToUser(List<String> message) {

        Platform.runLater(() -> {
            logListViw.appendText("\n************* info about target " + message.get(0) + " *************\n");
            logListViw.appendText("Target Type is: " + message.get(1) + "\n");
            logListViw.appendText("Target participates in serial sets: " + message.get(2) + "\n");
            logListViw.appendText("Target is in state: " + message.get(3) + "\n");
            getExtraDataAccordingToState(message.get(3), message.get(4));
        });
    }

    private void getExtraDataAccordingToState(String targetState, String message) {

        switch (targetState) {
            case "WAITING":
                logListViw.appendText("Target is waiting for: " + message + "ms\n");
                break;
            case "IN_PROCESS":
                logListViw.appendText("Target is in process for: " + message + "\n");
                break;
            case "FAILURE":
            case "SUCCESS":
            case "WARNING":
                logListViw.appendText("Target result: " + targetState + "\n");
                break;
            case "SKIPPED":
                logListViw.appendText("Target skipped because of: " +
                        Arrays.stream(message.split(",")).filter(target ->
                                checkIfTargetIsInList(target, obsSkippedList) ||
                                        checkIfTargetIsInList(target, obsFailedList)).collect(Collectors.toList()) + "\n");
                break;
            case "FROZEN":
                logListViw.appendText("Target is waiting for: " +
                        Arrays.stream(message.split(","))
                                .filter(target ->
                                        checkIfTargetIsInList(target, obsWaitingList) ||
                                                checkIfTargetIsInList(target, obsInProcessList) ||
                                                checkIfTargetIsInList(target, obsFrozenList))
                                .collect(Collectors.toList()) + "\n");
                break;

        }
        logListViw.appendText("****************************************************\n\n");
    }

    private boolean checkIfTargetIsInList(String target, ObservableList<StackPane> list) {
        return list.stream().anyMatch(t -> t.getId().equals(target));
    }

    private TargetState cts(Paint paint) {

        if (paint.equals(Color.RED)) {
            return TargetState.FAILURE;
        } else if (paint.equals(Color.BLUE)) {
            return TargetState.FROZEN;
        } else if (paint.equals(Color.GREEN)) {
            return TargetState.SUCCESS;
        } else if (paint.equals(Color.YELLOW)) {
            return TargetState.WARNING;
        } else if (paint.equals(Color.PINK)) {
            return TargetState.WAITING;
        } else if (paint.equals(Color.ORANGE)) {
            return TargetState.IN_PROCESS;
        } else {
            return TargetState.SKIPPED;
        }
    }

    private void handelLogOfTask(accumulatorForWritingToFile targetLog) {

        targetLog.outPutData.forEach(s -> {
            logListViw.appendText("\n" + TimeUtil.ltn(System.currentTimeMillis()) + " " + s);
            logListViw.positionCaret(0);
        });
        logListViw.appendText("\n");
    }

    private void handelFinishedTask(ProgressDto progressDto) {
        updateProgressBar(progressDto);
        if (playPauseBtn.isDisable())
            playPauseBtn.setDisable(false);
        if (progressDto.getTargetState() == TargetState.FAILURE)
            handelFailedAndSkipped(progressDto.getTargetName());
        else if (progressDto.getTargetState() == TargetState.SUCCESS ||
                progressDto.getTargetState() == TargetState.WARNING)
            handelSuccessAndOpening(progressDto.getTargetName(), progressDto.getTargetState());
        else
            manageListsMovement(progressDto);
    }

    private void handelSuccessAndOpening(String nameOfSuccessAndOpening, TargetState targetState) {
        String[] failedAndSkipped = nameOfSuccessAndOpening.split(",");
        manageListsMovement(new ProgressDto(failedAndSkipped[0], targetState));

        for (int i = 1; i < failedAndSkipped.length; i++) {
            manageListsMovement(new ProgressDto(failedAndSkipped[i], TargetState.WAITING));
        }
    }

    private void handelFailedAndSkipped(String namesOfFailedAndSkipped) {
        String[] failedAndSkipped = namesOfFailedAndSkipped.split(",");
        manageListsMovement(new ProgressDto(failedAndSkipped[0], TargetState.FAILURE));

        for (int i = 1; i < failedAndSkipped.length; i++) {
            manageListsMovement(new ProgressDto(failedAndSkipped[i], TargetState.SKIPPED));
        }
    }

    private void manageListsMovement(ProgressDto targetLog) {
        // switch on the state of the task
        StackPane temp = new TaskCircle().getStackPane();
        this.summery.add(targetLog.getTargetName() + " " + targetLog.getTargetState() + " visited switch");
        switch (targetLog.getTargetState()) {
            case WAITING:
            case SKIPPED:
                handelListInCaseOfStart(targetLog, temp);// the common ground here is -[frozen list -> else]
                break;
            case FAILURE:
            case WARNING:
            case SUCCESS:
                handelListInCaseOfFinished(targetLog, temp); //the common ground here is -[inProcess list -> else]
                break;
            case IN_PROCESS:
                handleListInCasOfInProcess(targetLog, temp);
                break;
        }
    }

    private void handleListInCasOfInProcess(ProgressDto targetLog, StackPane temp) {
        for (StackPane stackPane : obsWaitingList) {
            if (stackPane.getId().equals(targetLog.getTargetName())) {
                temp = stackPane;
                ((Circle) temp.getChildren().get(0)).fillProperty().setValue(Color.ORANGE);
                if (!inProcessListItems.contains(temp)) {
                    obsInProcessList.add(temp);
                }
                break;
            }
        }
        obsWaitingList.remove(temp);
    }

    private void handelListInCaseOfStart(ProgressDto targetLog, StackPane temp) {
        for (StackPane stackPane : obsFrozenList) {
            if (stackPane.getId().equals(targetLog.getTargetName())) {
                temp = stackPane;
                switch (targetLog.getTargetState()) {
                    case WAITING:
                        if (!waitingListItems.contains(temp)) {
                            ((Circle) temp.getChildren().get(0)).fillProperty().setValue(Color.PINK);
                            obsWaitingList.add(temp);
                        }
                        break;
                    case SKIPPED:
                        if (!skippedListItems.contains(temp)) {
                            ((Circle) temp.getChildren().get(0)).fillProperty().setValue(Color.GRAY);
                            obsSkippedList.add(temp);
                        }
                        break;
                }
                break;
            }
        }
        obsFrozenList.remove(temp);
    }

    private void handelListInCaseOfFinished(ProgressDto targetLog, StackPane temp) {
        for (StackPane stackPane : obsInProcessList) {
            if (stackPane.getId().equals(targetLog.getTargetName())) {
                temp = stackPane;
                switch (targetLog.getTargetState()) {
                    case FAILURE:
                        if (!failedListItems.contains(temp)) {
                            ((Circle) temp.getChildren().get(0)).fillProperty().setValue(Color.RED);
                            obsFailedList.add(temp);
                        }
                        break;
                    case WARNING:
                    case SUCCESS:
                        if (!finishedListItems.contains(temp)) {
                            ((Circle) temp.getChildren().get(0)).fillProperty().setValue(
                                    targetLog.getTargetState() == TargetState.WARNING ?
                                            Color.YELLOW :
                                            Color.GREEN);
                            obsFinishedList.add(temp);
                        }
                        break;
                }
                break;
            }
        }
        obsInProcessList.remove(temp);
    }

    private void updateProgressBar(ProgressDto progressDto) {
        if (progressDto.getTargetState() == TargetState.FAILURE ||
                progressDto.getTargetState() == TargetState.WARNING ||
                progressDto.getTargetState() == TargetState.SUCCESS) {
            allFinishedTasks.addAll(Arrays.asList(progressDto.getTargetName().split(",")));
        }
        finishedNumberTargets = allFinishedTasks.size();
        progressBar.setProgress((double) finishedNumberTargets / (double) totalNumberOfTargets);
    }

    private void showSummaryWindow(long time) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/resources/fxml/summayWindow.fxml");
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            SummaryController summayController = fxmlLoader.getController();

            summayController.setSummaryWindow(
                    obsFailedList,
                    obsSkippedList,
                    obsFinishedList,
                    taskTypeHeaderLabel.getText(),
                    time
            );

            Stage stage = new Stage();
            stage.setTitle("Summary");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            //
        }
    }

    public void resetAllLists(boolean isIncremental) {
        if (isIncremental) {
            obsFrozenList.clear();
            obsWaitingList.clear();

            obsFrozenList.addAll(obsSkippedList);
            obsSkippedList.clear();

            obsFrozenList.addAll(obsFailedList);
            obsFailedList.clear();

            obsFinishedList.clear();
        } else {
            obsFrozenList.clear();

            obsFrozenList.addAll(obsSkippedList);
            obsSkippedList.clear();

            obsFrozenList.addAll(obsFinishedList);
            obsFinishedList.clear();

            obsFrozenList.addAll(obsFailedList);
            obsFailedList.clear();
        }
        allFinishedTasks.clear();
        totalNumberOfTargets = obsFrozenList.size();
        progressBar.progressProperty().setValue(0);
    }

    public void disablePlayPauseBtn() {
        playPauseBtn.setDisable(true);
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.dashboardScene = primaryStage.getScene();
    }
}
