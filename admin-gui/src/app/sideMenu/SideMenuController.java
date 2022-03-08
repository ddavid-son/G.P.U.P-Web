package app.sideMenu;

import app.settings.SettingsController;
import argumentsDTO.CommonEnums.*;
//import app.graphVizForm.GraphVizFormController;
import app.mainScreen.ControlPanelController;
import app.taskForm.TaskFormController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class SideMenuController {

    private ControlPanelController appController;
    @FXML
    public Button graphVizBtn;

    @FXML
    private Button goToDashboardBtn;

    @FXML
    private Button findPathBtn;

    @FXML
    private Button findCircleBtn;

    @FXML
    private Button displayRelatedBtn;

    @FXML
    private Button runTaskBtn;

    @FXML
    private Button settingBtn;

    private final String TASK_FORM_FXML = "/resources/fxml/TaskForm.fxml";
    private final String GRAPH_VIZ_FORM = "/resources/fxml/graphVizForm.fxml";

    public TaskType taskType;
    private File currentFileInTask = new File("");

    @FXML
    private void onGoToDashboardBtnClicked(ActionEvent event) {
        appController.goToDashboard();
    }

    public String setCssAccordingToTheme() {
        switch (appController.theme) {
            case "theme1":
                return getClass().getResource("/resources/css/theme1.css").toExternalForm();
            case "theme2":
                return getClass().getResource("/resources/css/theme2.css").toExternalForm();
            case "theme3":
                return getClass().getResource("/resources/css/dTheme.css").toExternalForm();
        }
        return getClass().getResource("/resources/css/theme1.css").toExternalForm();
    }

    @FXML
    private void OnDisplayRelatedBtnClick(ActionEvent event) {
        appController.displayRelated();
    }

    @FXML
    private void OnFindCircleBtnClick(ActionEvent event) {

        appController.findAllCircles();
    }

    @FXML
    private void OnFindPathBtnClick(ActionEvent event) {

        appController.findAllPaths();
    }

    @FXML
    private void OnRunTaskBtnClick(ActionEvent event) {
        if (!appController.taskHasTargetsSelected())
            return;

        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource(TASK_FORM_FXML);
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            TaskFormController taskFormController = fxmlLoader.getController();

            root.getStylesheets().add(appController.themeCSSPath);
            taskFormController.setAppController(appController, this);
            taskFormController.setTaskController();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Task Form");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setAllComponentsToDisabled(boolean disableXmlLoadBtn) {
        goToDashboardBtn.setDisable(disableXmlLoadBtn);
        findPathBtn.setDisable(true);
        findCircleBtn.setDisable(true);
        displayRelatedBtn.setDisable(true);
        runTaskBtn.setDisable(true);
        graphVizBtn.setDisable(true);
    }

    public void setAllComponentsToEnabled() {
        goToDashboardBtn.setDisable(false);
        findPathBtn.setDisable(false);
        findCircleBtn.setDisable(false);
        displayRelatedBtn.setDisable(false);
        runTaskBtn.setDisable(false);
        graphVizBtn.setDisable(false);
    }

    public void setAppController(ControlPanelController appController) {
        this.appController = appController;
        setButtonsIcon();
    }

    private void setButtonsIcon() {
        goToDashboardBtn.setGraphic(appController.getIcon("/dashboardIcon.png"));
        findPathBtn.setGraphic(appController.getIcon("/pathIcon1.png"));
        findCircleBtn.setGraphic(appController.getIcon("/circleIcon.png"));
        displayRelatedBtn.setGraphic(appController.getIcon("/relatedIcon1.png"));
        runTaskBtn.setGraphic(appController.getIcon("/launchIcon.png"));
        settingBtn.setGraphic(appController.getIcon("/settingIcon.png"));
        graphVizBtn.setGraphic(appController.getIcon("/gvIcon.png"));
    }

    @FXML
    private void OnSettingBtnClicked(ActionEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource("/resources/fxml/settingScreen.fxml");
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            SettingsController settingScreenController = fxmlLoader.getController();

            settingScreenController.setAppController(appController);
            root.getStylesheets().add(appController.themeCSSPath);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Setting Form");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    public void graphVizBtnClicked(ActionEvent actionEvent) {
   /*
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            URL url = getClass().getResource(GRAPH_VIZ_FORM);
            fxmlLoader.setLocation(url);
            Parent root = fxmlLoader.load(url.openStream());
            GraphVizFormController graphVizFormController = fxmlLoader.getController();

            root.getStylesheets().add(appController.themeCSSPath);
            graphVizFormController.setAppController(appController, execution);
            graphVizFormController.setGraphVizController();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("GraphViz");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    */
    }

    public void setThemeCSSPath(String themeCSSPath) {
        settingBtn.getScene().getStylesheets().clear();
        settingBtn.getScene().getStylesheets().add(themeCSSPath);
    }
}


