package app.taskForm;

import app.mainScreen.ControlPanelController;
import app.sideMenu.SideMenuController;
import argumentsDTO.CommonEnums.*;
import argumentsDTO.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

import static app.util.FXUtils.handleErrors;

public class TaskFormController {


    // ----------------------------------------------- Compilation Components ---------------------------------------- //
    private File dstFolder;
    private File srcFolder;
    public Button newRunBtnComp;
    private Label dstFolderNameLabel;
    private Label srcFolderNameLabel;
    public RadioButton whatIfOffRBComp;
    public RadioButton whatIfUpOnRBComp;
    public Button sourceFolderUploadBnt;
    public Button OutputFolderUploadBnt;
    public RadioButton whatIfDownOnRBComp;
    public GridPane compilationTabGridPane;
    @FXML
    private TextField compilationTaskNameTF;
    // ----------------------------------------------- Compilation Components ---------------------------------------- //


    // ----------------------------------------------- Simulation Components ---------------------------------------- //
    @FXML
    private Slider successSlider;

    @FXML
    private Node taskStage;

    @FXML
    private Slider warningSlider;

    @FXML
    private RadioButton yesRandomRB;

    @FXML
    private RadioButton noRandomRB;

    @FXML
    private GridPane simulationTabGridPane;

    @FXML
    private Button newRunBtn;

    @FXML
    private final IntField successField = new IntField(1, 100, 50);

    @FXML
    private final IntField warningField = new IntField(1, 100, 50);

    @FXML
    private final IntField sleepTimeField = new IntField(1, 20000, 1000);

    @FXML
    private Slider sleepTimeSlider;

    @FXML
    private RadioButton whatIfOffRB;

    @FXML
    private RadioButton whatIfDownOnRB;

    @FXML
    private RadioButton whatIfUpOnRB;

    @FXML
    private TextField simulationTaskNameTF;
    // ----------------------------------------------- Simulation Components ---------------------------------------- //

    private File currentFileInTask;
    private ControlPanelController appController;
    private SideMenuController sideMenuController;

    // -------------------------------------------- what if methods -------------------------------------------------- /
    @FXML
    void whatIfOffRBClicked(ActionEvent event) {
        whatIfOffRB.setSelected(true);
        whatIfDownOnRB.setSelected(false);
        whatIfUpOnRB.setSelected(false);
    }

    @FXML
    void whatIfOnRBClicked(ActionEvent event) {
        whatIfDownOnRB.setSelected(true);
        whatIfOffRB.setSelected(false);
        whatIfUpOnRB.setSelected(false);
    }

    @FXML
    public void whatIfUpOnRBClicked(ActionEvent event) {
        whatIfUpOnRB.setSelected(true);
        whatIfDownOnRB.setSelected(false);
        whatIfOffRB.setSelected(false);
    }

    public boolean getWhatIfDownIsSelected() {
        return whatIfDownOnRB.isSelected();
    }
    // -------------------------------------------- what if methods -------------------------------------------------- /


    // --------------------------------------------- Compilation Methods ----------------------------------------------//
    public void onWhatIfOffRBCompClicked(ActionEvent actionEvent) {
        whatIfOffRBComp.setSelected(true);
        whatIfDownOnRBComp.setSelected(false);
        whatIfUpOnRBComp.setSelected(false);
    }

    public void onWhatIfDownOnRBCompClicked(ActionEvent actionEvent) {
        whatIfDownOnRBComp.setSelected(true);
        whatIfUpOnRBComp.setSelected(false);
        whatIfOffRBComp.setSelected(false);
    }

    public void onWhatIfUpOnRBCompClicked(ActionEvent actionEvent) {
        whatIfUpOnRBComp.setSelected(true);
        whatIfDownOnRBComp.setSelected(false);
        whatIfOffRBComp.setSelected(false);
    }

    public void onNewRunBtnCompClicked(ActionEvent actionEvent) {
        //sideMenuController.setNewFileForTask();
        runCompilationTask();
    }

    private void runCompilationTask() {
        if (!(!compilationTaskNameTF.getText().isEmpty() && srcFolder != null &&
                dstFolder != null && srcFolder.exists() && dstFolder.exists()) &&
                !simulationTaskNameTF.getText().trim().matches(".*\\d")) {
            handleErrors(
                    null,
                    "Source and destination folders must be set before running a compilation task.",
                    "error - source or destination folder wasn't set"
            );
            return;
        }
        ((Stage) newRunBtn.getScene().getWindow()).close();

        CompilationArgs compilationArgs = new CompilationArgs(
                !whatIfOffRB.isSelected(),
                getWhatIfDownIsSelected() ?
                        RelationType.DEPENDS_ON :
                        RelationType.REQUIRED_FOR,
                this.srcFolder.toString(),
                this.dstFolder.toString()
        );

        updateTaskData(compilationArgs);
        appController.runTask(compilationArgs);
        if (sideMenuController.taskType == null) {
            sideMenuController.taskType = TaskType.COMPILATION;
        }
    }

    public void sourceFolderUploadBntClicked(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Source Folder");
        srcFolder = directoryChooser.showDialog(null);
        updateSRCLabel();
    }

    private void updateSRCLabel() {
        if (srcFolderNameLabel == null) {
            srcFolderNameLabel = new Label(srcFolder == null ?
                    "no folder was selected" :
                    srcFolder.getName());
            compilationTabGridPane.add(srcFolderNameLabel, 3, 1);
        } else {
            srcFolderNameLabel.setText(srcFolder == null ?
                    "no folder was selected" :
                    srcFolder.getName());
        }
    }

    public void onOutputFolderUploadBntClicked(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select destination Folder");
        dstFolder = directoryChooser.showDialog(null);
        updateDSTLabel();
    }

    private void updateDSTLabel() {
        if (dstFolderNameLabel == null) {
            dstFolderNameLabel = new Label(dstFolder == null ?
                    "no folder was selected" :
                    dstFolder.getName());
            compilationTabGridPane.add(dstFolderNameLabel, 3, 2);
        } else {
            dstFolderNameLabel.setText(dstFolder == null ?
                    "no folder was selected" :
                    dstFolder.getName());
        }
    }

    // --------------------------------------------- Compilation Methods ----------------------------------------------//


    // --------------------------------------------- Simulation Methods ----------------------------------------------//
    @FXML
    private void OnNewRunBtnClicked(ActionEvent event) {
        runSimulationTask();
    }

    @FXML
    void noRandomRBSelect(ActionEvent event) {
        noRandomRB.setSelected(true);
        yesRandomRB.setSelected(false);
    }

    @FXML
    void yesRandomRBSelect(ActionEvent event) {
        noRandomRB.setSelected(false);
        yesRandomRB.setSelected(true);
    }

    private void runSimulationTask() {
        if (assertSimulationUserInput()) {
            handleErrors(
                    null,
                    "Please fill all the fields - dont leave any field empty \n" +
                            "make sure the task name doesnt end with a number",
                    "Error in arguments that was inserted"
            );
            return;
        }
        ((Stage) newRunBtn.getScene().getWindow()).close();

        SimulationArgs simulationArgs = new SimulationArgs(
                ((double) successField.getValue()) / 100,
                ((double) warningField.getValue()) / 100,
                sleepTimeField.getValue(),
                yesRandomRB.isSelected(),
                !whatIfOffRB.isSelected(),
                getWhatIfDownIsSelected() ?
                        RelationType.DEPENDS_ON :
                        RelationType.REQUIRED_FOR
        );
        updateTaskData(simulationArgs);

        appController.runTask(simulationArgs);
        if (sideMenuController.taskType == null)
            sideMenuController.taskType = TaskType.SIMULATION;
    }

    private void updateTaskData(TaskArgs taskArgs) {
        taskArgs.setTaskName(
                taskArgs.getTaskType().equals(TaskType.SIMULATION) ?
                        simulationTaskNameTF.getText().trim() :
                        compilationTaskNameTF.getText().trim()
        );
        taskArgs.setTaskOwner(appController.getUsername());
        taskArgs.setOriginalGraph(appController.getEngineName());
    }

    private boolean assertSimulationUserInput() {
        return !(successField.getValue() > 0 &&
                warningField.getValue() > 0 &&
                sleepTimeField.getValue() > 0 &&
                !simulationTaskNameTF.getText().isEmpty() &&
                !simulationTaskNameTF.getText().trim().matches(".*\\d"));
    }
    // --------------------------------------------- Simulation Methods ----------------------------------------------//

    public void setTaskController() {
        simulationTabSetUp();
        compilationTabSetUp();
    }

    private void compilationTabSetUp() {
        whatIfOffRBComp.setSelected(true);
        whatIfDownOnRBComp.setSelected(false);
        whatIfUpOnRBComp.setSelected(false);

        sourceFolderUploadBnt.setGraphic(appController.getIcon("/UploadIcon.png"));
        OutputFolderUploadBnt.setGraphic(appController.getIcon("/UploadIcon.png"));
    }

    private void simulationTabSetUp() {
        sleepTimeSlider.setMax(20_000);
        simulationTabGridPane.add(successField, 4, 1);
        simulationTabGridPane.add(warningField, 4, 1);
        simulationTabGridPane.add(sleepTimeField, 4, 2);

        GridPane.setValignment(successField, VPos.TOP);
        GridPane.setValignment(warningField, VPos.BOTTOM);

        successSlider.valueProperty().bindBidirectional(successField.valueProperty());
        warningSlider.valueProperty().bindBidirectional(warningField.valueProperty());
        sleepTimeSlider.valueProperty().bindBidirectional(sleepTimeField.valueProperty());

        yesRandomRB.setSelected(true);
        noRandomRB.setSelected(false);
        whatIfOffRB.setSelected(true);
        whatIfUpOnRB.setSelected(false);
        whatIfDownOnRB.setSelected(false);
    }

    public void setAppController(ControlPanelController appController, SideMenuController sideMenuController) {
        this.appController = appController;
        this.sideMenuController = sideMenuController;
    }
}
