package app.graphTableView;

import app.mainScreen.ControlPanelController;
import dataTransferObjects.GraphInfoDTO;
import dataTransferObjects.InfoAboutTargetDTO;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.List;
import java.util.stream.Collectors;

public class GraphTableViewController {

    private ControlPanelController appController;

    @FXML
    private Button goToTaskViewBtn;

    @FXML
    private TableView<InfoAboutTargetDTO> graphTable;

    @FXML
    private TableColumn<InfoAboutTargetDTO, String> nameCol;

    @FXML
    private TableColumn<InfoAboutTargetDTO, String> typeCol;

    @FXML
    private TableColumn<?, ?> dependsOnCol;

    @FXML
    private TableColumn<InfoAboutTargetDTO, Integer> dependsOnImmediateCol;

    @FXML
    private TableColumn<InfoAboutTargetDTO, Integer> dependsOnRelatedCol;

    @FXML
    private TableColumn<?, ?> RequiredForCol;

    @FXML
    private TableColumn<InfoAboutTargetDTO, Integer> RequiredForImmediateCol;

    @FXML
    private TableColumn<InfoAboutTargetDTO, Integer> RequiredForRelatedCol;

    @FXML
    private TableColumn<InfoAboutTargetDTO, Integer> serialSetCol;

    @FXML
    private TableColumn<InfoAboutTargetDTO, String> dataCol;

    @FXML
    public TableColumn<InfoAboutTargetDTO, CheckBox> selectCol = new TableColumn<>("CheckBox");


    @FXML
    private TableView<GraphInfoDTO> stateTable;

    @FXML
    private TableColumn<GraphInfoDTO, Integer> independentCol;

    @FXML
    private TableColumn<GraphInfoDTO, Integer> rootCol;

    @FXML
    private TableColumn<GraphInfoDTO, Integer> leafCol;

    @FXML
    private TableColumn<GraphInfoDTO, Integer> middleCol;

    @FXML
    private TableColumn<GraphInfoDTO, Integer> totalCol;

    @FXML
    private Button serialSetSummaryBtn;

    private ObservableList<InfoAboutTargetDTO> dataForTable;

    private ObservableList<GraphInfoDTO> dataForStateTable;


    @FXML
    private void onSerialSetSummaryBtnClicked() {
        //appController.showSerialSetSummary();
    }

    @FXML
    public void onGoToTaskViewBtnClicked() {
        appController.goBackToTaskView();
    }

    public void setAppController(ControlPanelController appController) {
        this.appController = appController;
    }

    public void setAllComponentsToDisabled() {
        graphTable.setDisable(true);
        stateTable.setDisable(true);
    }

    public void setAllComponentsToEnabled() {
        graphTable.setDisable(false);
        stateTable.setDisable(false);
        serialSetSummaryBtn.setDisable(false);
    }

    public void loadGraphToTableView(List<InfoAboutTargetDTO> allTargets) {
        dataForTable = FXCollections.observableArrayList(allTargets);
        graphTable.getItems().clear();
        nameCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTargetName()));
        typeCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTargetTypeString()/*getTargetType().toString()*/));
        dependsOnImmediateCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getDirectDependsOnByCount()).asObject());
        dependsOnRelatedCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getDependsOnCount()).asObject());
        RequiredForImmediateCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getDirectRequiredByCount()).asObject());
        RequiredForRelatedCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getRequiredForCount()).asObject());
        serialSetCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getSerialSetsNames().size()).asObject());
        dataCol.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getUserData()));

        graphTable.getColumns().remove(selectCol);
        handleCheckBoxColAddition();
        graphTable.setItems(dataForTable);
    }

    private void handleCheckBoxColAddition() {
        CheckBox CB = new CheckBox();
        selectCol.setGraphic(CB);
        selectCol.setMinWidth(50);
        selectCol.setMaxWidth(50);
        selectCol.setCellValueFactory(arg0 -> {
            CheckBox checkBox = new CheckBox();

            checkBox.selectedProperty().setValue(arg0.getValue().getIsSelected());
            checkBox.selectedProperty().addListener((ov, old_val, new_val) ->
                    arg0.getValue().setIsSelected(new_val));

            CB.selectedProperty().addListener((ov, old_val, new_val) -> {
                checkBox.selectedProperty().setValue(new_val);
            });

            return new SimpleObjectProperty<>(checkBox);
        });

        graphTable.getColumns().add(0, selectCol);
    }

    public void loadSummaryToTableView(GraphInfoDTO graphStateSummary) {
        dataForStateTable = FXCollections.observableArrayList(graphStateSummary);
        independentCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(graphStateSummary.getTotalNumberOfIndependents()).asObject());
        rootCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(graphStateSummary.getTotalNumberOfRoots()).asObject());
        leafCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(graphStateSummary.getTotalNumberOfLeaves()).asObject());
        middleCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(graphStateSummary.getTotalNumberOfMiddles()).asObject());
        totalCol.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(graphStateSummary.getTotalNumberOfTargets()).asObject());

        stateTable.setItems(dataForStateTable);
    }

    public TableView<InfoAboutTargetDTO> getTargetsGraph() {
        return graphTable;
    }

    public boolean hasTargetSelected() {
        return dataForTable.stream().anyMatch(InfoAboutTargetDTO::getIsSelected);
    }

    public List<String> getSelectedTargetNames() {
        return dataForTable.stream()
                .filter(InfoAboutTargetDTO::getIsSelected)
                .map(InfoAboutTargetDTO::getTargetName)
                .collect(Collectors.toList());
    }

    public void setThemeCSSPath(String themeCSSPath) {
        serialSetSummaryBtn.getStylesheets().clear();
        serialSetSummaryBtn.getStylesheets().add(themeCSSPath);
    }
}
