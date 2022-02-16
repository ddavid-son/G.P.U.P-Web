package app.mainScreen;

import app.circleDisplay.CircleDisplayController;
import app.findAllPaths.FindAllPathsController;
import app.graphTableView.GraphTableViewController;
/*import app.circleDisplay.CircleDisplayController;
import app.findAllPaths.FindAllPathsController;
import app.relatedView.RelatedViewController;
import app.serialSet.SerialSetController;*/
import app.relatedView.RelatedViewController;
import argumentsDTO.*;
import argumentsDTO.CommonEnums.*;
import app.sideMenu.SideMenuController;
import app.taskView.TaskViewController;
import app.util.http.HttpClientUtil;
import com.google.gson.reflect.TypeToken;
import dataTransferObjects.GraphInfoDTO;
import dataTransferObjects.InfoAboutTargetDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import resources.Constants;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ControlPanelController {

    @FXML
    private ScrollPane sideMenuComponent;
    @FXML
    private SideMenuController sideMenuComponentController;
    @FXML
    private ScrollPane graphTableViewComponent;
    @FXML
    private GraphTableViewController graphTableViewComponentController;

    BorderPane mainScreen;
    ScrollPane taskViewScreen;
    TaskViewController taskViewController;

    private String engineName;
    public String theme = "theme1";
    public List<String> targetFromPreviousRun;
    public String themeCSSPath = "/resources/css/theme1.css";

    private File activeFile;
    private ScrollPane serialSetScreen;
    private final String FIND_ALL_PATHS_FXML_FILE = "/resources/fxml/findAllPaths.fxml";
    private final String CIRCLE_DISPLAY_FXML_FILE = "/resources/fxml/circleDisplay.fxml";
    private ScrollPane dashboard;
    private String userName;


    // ---------------------------------------------------- Utils --------------------------------------------------- //
    public void setThemeCSSPath(String themeCSSPath) {
        this.themeCSSPath = themeCSSPath;
        sideMenuComponentController.setThemeCSSPath(themeCSSPath);
        graphTableViewComponentController.setThemeCSSPath(themeCSSPath);

        // set new css to this scene
        graphTableViewComponent.getStylesheets().clear();
        graphTableViewComponent.getStylesheets().add(themeCSSPath);
    }

    public String getUsername() {
        return userName;
    }

    @FXML
    public void initialize() {
        if (sideMenuComponentController != null && graphTableViewComponentController != null) {
            sideMenuComponentController.setAppController(this);
            graphTableViewComponentController.setAppController(this);
            setAllComponentsToDisabled();
        }
    }

    public Node getIcon(String resourceName) {
        return getIcon(resourceName, 50);
    }

    public Node getIcon(String resourceName, int size) {
        Image image = new Image("/resources/icons" + resourceName);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        return imageView;
    }

    protected void setAllComponentsToDisabled() {
        graphTableViewComponentController.setAllComponentsToDisabled();
        sideMenuComponentController.setAllComponentsToDisabled(false);
    }

    public void setControlPanel(String engineName, ScrollPane dashboard, String userName) {
        this.engineName = engineName;
        this.dashboard = dashboard;
        this.userName = userName;
        fetchDTOsForAdmin();
    }

    private void initControlPanel(List<InfoAboutTargetDTO> allTargetInfo, GraphInfoDTO graphInfo) {
        try {
            graphTableViewComponentController.loadGraphToTableView(allTargetInfo);
            graphTableViewComponentController.loadSummaryToTableView(graphInfo);
            graphTableViewComponentController.setAllComponentsToEnabled();
            sideMenuComponentController.setAllComponentsToEnabled();
            if (mainScreen == null) {
                mainScreen = (BorderPane) graphTableViewComponent.getScene().getRoot();
            }
        } catch (IllegalArgumentException e) {
            handleErrors(
                    e,
                    "",
                    "The file you selected is not a valid XML file"
            );
        }
    }

    public void goToDashboard() {
        ((Stage) mainScreen.getScene().getWindow()).setScene(dashboard.getScene());
    }
    // ---------------------------------------------------- Utils --------------------------------------------------- //


    // -------------------------------------------------- EX2 ADDONS ------------------------------------------------ //
    private void fetchDTOsForAdmin() {
        String finalUrl = Constants.FULL_SERVER_PATH + "/get-dtos-for-admin" + "?graph-name=" + engineName;

        Request request = new Request.Builder()
                .url(finalUrl)
                .method("GET", null)
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleErrors(e, "", "Error fetching  data");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    String errorMessage = response.body().string();
                    handleErrors(null, errorMessage, "Error fetching execution data");
                } else {
                    String[] responseBody = response.body().string().split("~~~");

                    GraphInfoDTO sum = HttpClientUtil.GSON.fromJson(responseBody[0],
                            new TypeToken<GraphInfoDTO>() {
                            }.getType());

                    List<InfoAboutTargetDTO> all = HttpClientUtil.GSON.fromJson(responseBody[1],
                            new TypeToken<List<InfoAboutTargetDTO>>() {
                            }.getType());

                    Platform.runLater(() -> initControlPanel(all, sum));
                }
            }
        });
    }

    public File getActiveFile() {
        return activeFile;
    }

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

    public void findAllPaths() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource(FIND_ALL_PATHS_FXML_FILE);
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            FindAllPathsController PathFindPopUpWindow = fxmlLoader.getController();
            root.getStylesheets().add(themeCSSPath);

            PathFindPopUpWindow.setAppController(this);
            //PathFindPopUpWindow.loadComboBoxes(execution.getAllTargetNames(), execution);
            getAllTargets(engineName, PathFindPopUpWindow::loadComboBoxes);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Find all paths");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void getAllTargets(String engineName, Consumer<List<String>> allTargetsConsumer) {

        String finalUrl = HttpUrl.parse(
                        Constants.FULL_SERVER_PATH + "/get-all-targets")
                .newBuilder()
                .addQueryParameter("engine-name", engineName)
                .toString();


        HttpClientUtil.runAsync(finalUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleErrors(e, "", "Error fetching targets");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.code() != 200) {
                    handleErrors(null,
                            "Could not acquire targets, please try again",
                            "Error fetching targets");
                } else {
                    String s = response.body().string();
                    Platform.runLater(() ->
                            allTargetsConsumer.accept(HttpClientUtil.GSON.fromJson(s, new TypeToken<List<String>>() {
                            }.getType())));
                }
            }
        });
    }

    public void findAllCircles() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource(CIRCLE_DISPLAY_FXML_FILE);
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            CircleDisplayController circleDisplay = fxmlLoader.getController();

            circleDisplay.setAppController(this, engineName);
            getAllTargets(engineName, circleDisplay::displayCircles);
            root.getStylesheets().add(themeCSSPath);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Find all circles");
            stage.setScene(new Scene(root));
            stage.getScene().getStylesheets().add(getClass().getResource("/app/circleDisplay/displayCircle.css").toExternalForm());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void displayRelated() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/resources/fxml/relatedView.fxml");
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            RelatedViewController relatedViewController = fxmlLoader.getController();
            relatedViewController.setAppController(this);

            getAllTargets(engineName, relatedViewController::loadTargetList);
            root.getStylesheets().add(themeCSSPath);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("display related");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // -------------------------------------------------- EX2 ADDONS ------------------------------------------------ //


    // -------------------------------------------------task methods-------------------------------------------------- //
    public boolean taskHasTargetsSelected() {
        if (!graphTableViewComponentController.hasTargetSelected()) {
            handleErrors(
                    null,
                    "You must select at least one target before preforming this action",
                    "Error running task"
            );
            return false;
        }
        return true;
    }

    public String getEngineName() {
        return engineName;
    }


    public void runTask(TaskArgs taskArgs) {
        List<String> targetNames = graphTableViewComponentController.getSelectedTargetNames();
        if (taskArgs.isWhatIf()) {
            targetNames = fetchWhatIfResults(targetNames, taskArgs.getRelationType());
        }
        targetFromPreviousRun = targetNames; // here for the visibility of Incremental button
        taskArgs.getTargetsSelectedForGraph().addAll(targetNames);
        goToTaskView(taskArgs);
        taskViewController.delegateExecutionOfTaskToAnotherThread(taskArgs);
    }

    private List<String> fetchWhatIfResults(List<String> targetNames, RelationType relationType) {
        String finalURl = HttpUrl.parse(Constants.FULL_SERVER_PATH + "/get-what-if")
                .newBuilder()
                .addQueryParameter("engine-name", engineName)
                .addQueryParameter("relation-type", relationType.toString())
                .build()
                .toString();

        RequestBody body = RequestBody.create(
                HttpClientUtil.GSON.toJson(targetNames),
                MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
                .url(finalURl)
                .post(body)
                .build();

        try {
            Response response = HttpClientUtil.HTTP_CLIENT.newCall(request).execute();
            return getAllWhatIfResultsFromResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<String> getAllWhatIfResultsFromResponse(Response response) {
        String s = "";
        try {
            if (response.isSuccessful()) {
                s = response.body().string();
                return HttpClientUtil.GSON.fromJson(s, new TypeToken<List<String>>() {
                }.getType());
            }
            handleErrors(null,
                    "http call ended unsuccessfully: error code " + response.code() + "\n message: " + s,
                    "Error getting what if results");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    public boolean currentSelectedTargetsAreTheSameAsPreviousRun() {
        List<String> targetNames = graphTableViewComponentController.getSelectedTargetNames();
        return targetNames.equals(targetFromPreviousRun);
        //todo: this is not accurate when the order of the list is changed(maybe because of sorting option of the table)
    }

    public void copyTextToClipboard(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    public void openFile(String imagePath) {
        try {
            Desktop.getDesktop().open(new File(imagePath));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    // -------------------------------------------------task methods------------------------------------------------- //

    //----------------------------------------------- task view ----------------------------------------------------- //
    public void goToTaskView(TaskArgs taskArgs) {
        if (!taskArgs.isIncremental())
            createNewTaskController(taskArgs);
        // replace the center of the main screen with the task view
        mainScreen.setCenter(taskViewScreen);
    }

    public void goBackToTaskView() {
        if (taskViewScreen != null) {
            mainScreen.setCenter(taskViewScreen);
        }
    }

    private void createNewTaskController(TaskArgs taskArgs) {
        try {
            URL url = getClass().getResource("/resources/fxml/TaskView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load();
            taskViewController = fxmlLoader.getController();

            root.getStylesheets().add(themeCSSPath);
            taskViewController.setAppController(this);
            taskViewController.setTaskView(taskArgs);
            this.taskViewScreen = (ScrollPane) root;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

/*    public void showSerialSetSummary() {
        if (serialSetScreen == null)
            createSerialSet();
        if (mainScreen.getRight() == serialSetScreen)
            mainScreen.setRight(null);
        else
            mainScreen.setRight(serialSetScreen);
    }

    private void createSerialSet() {
        try {
            URL url = getClass().getResource("/resources/fxml/serialSetView.fxml");
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load();
            SerialSetController serialSetController = fxmlLoader.getController();

            root.getStylesheets().add(themeCSSPath);
            serialSetController.setAppController(this, execution);
            serialSetController.setSerialSet();

            this.serialSetScreen = (ScrollPane) root;
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }*/

    public void setTaskViewController(TaskViewController taskViewController) {
        this.taskViewController = taskViewController;
    }

    public void goToMainScreen() {
        mainScreen.setCenter(graphTableViewComponent);
    }

    public void resetListOnTaskView(boolean isIncremental) {
        taskViewController.resetAllLists(isIncremental);
    }

    public void resumeExecution() {
        //execution.resumeTask();
    }

    public void pauseExecution() {
        //execution.pauseTask();
    }

    public void setNumberOfThreads(Integer value) {
        //execution.setNumberOfThreads(value);
    }
    //----------------------------------------------- task view ----------------------------------------------------- //
}