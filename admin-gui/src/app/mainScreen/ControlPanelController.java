package app.mainScreen;

import app.circleDisplay.CircleDisplayController;
import app.dashboard.DashBoardController;
import app.findAllPaths.FindAllPathsController;
import app.graphTableView.GraphTableViewController;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import resources.Constants;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static app.util.FXUtils.handleErrors;

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

    private final String FIND_ALL_PATHS_FXML_FILE = "/resources/fxml/findAllPaths.fxml";
    private final String CIRCLE_DISPLAY_FXML_FILE = "/resources/fxml/circleDisplay.fxml";
    private ScrollPane dashboard;
    private String userName;
    private DashBoardController dashBoardController;


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

    public void findAllPaths() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource(FIND_ALL_PATHS_FXML_FILE);
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            FindAllPathsController PathFindPopUpWindow = fxmlLoader.getController();
            root.getStylesheets().add(themeCSSPath);

            PathFindPopUpWindow.setAppController(this);
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
        delegateExecutionOfTaskToAnotherThread(taskArgs);
    }

    public void delegateExecutionOfTaskToAnotherThread(TaskArgs taskArgs) {
        String taskType = taskArgs.getTaskType().toString();

        String finalUrl = HttpUrl.parse(Constants.FULL_SERVER_PATH + "/create-task")
                .newBuilder()
                .addQueryParameter("task-type", taskType)
                .build()
                .toString();

        RequestBody requestBody = getRequestBody(taskArgs);

        Request request = new Request.Builder()
                .url(finalUrl)
                .post(requestBody)
                .build();

        HttpClientUtil.runAsync(request, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                handleErrors(e, "", "Error! could not create the task");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    System.out.println(responseBody);
                    handleErrors(null, responseBody, "Error! could not create the task");
                } else {
                    // update label with success info
                }
            }
        });
    }

    @NotNull
    private RequestBody getRequestBody(TaskArgs taskArgs) {
        RequestBody rb;
        if (taskArgs.getTaskType() == TaskType.COMPILATION) {
            CompilationArgs ca = (CompilationArgs) taskArgs;
            rb = RequestBody.create(
                    HttpClientUtil.GSON.toJson(ca, new TypeToken<CompilationArgs>() {
                    }.getType()),
                    MediaType.parse("application/json"));
        } else {
            SimulationArgs ca = (SimulationArgs) taskArgs;
            rb = RequestBody.create(
                    HttpClientUtil.GSON.toJson(ca, new TypeToken<SimulationArgs>() {
                    }.getType()),
                    MediaType.parse("application/json"));
        }
        return rb;
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
    // -------------------------------------------------task methods------------------------------------------------- //

    //----------------------------------------------- task view ----------------------------------------------------- //
    public void goToTaskView(TaskArgs taskArgs) {
        createNewTaskController(taskArgs);
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
            //taskViewController.setAppController(this);
            taskViewController.setTaskView(taskArgs);
            this.taskViewScreen = (ScrollPane) root;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setTaskViewController(TaskViewController taskViewController) {
        this.taskViewController = taskViewController;
    }

    public void goToMainScreen() {
        mainScreen.setCenter(graphTableViewComponent);
    }


    public void setDashboardController(DashBoardController dashBoardController) {
        this.dashBoardController = dashBoardController;
        dashBoardController.setAppController(this);
    }
    //----------------------------------------------- task view ----------------------------------------------------- //
}