
package app.relatedView;

import app.mainScreen.ControlPanelController;
import argumentsDTO.CommonEnums.*;
import dataTransferObjects.WhatIfDTO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

public class RelatedViewController {

    @FXML
    private ListView<String> targetListView;

    @FXML
    private GridPane relatedTableView;

    @FXML
    private ListView<String> whatIfList;

    @FXML
    private Button relationIndicatorBtn;

    @FXML
    private CheckBox filterNonImmediateCB;

    RelationType relationType = RelationType.DEPENDS_ON;

    private ControlPanelController appController;

    private WhatIfDTO whatIfSelection = new WhatIfDTO();

    //private Engine execution;

    @FXML
    void switchRelationTypeClicked(ActionEvent event) {
/*        relationType = relationType == RelationType.DEPENDS_ON ?
                RelationType.REQUIRED_FOR :
                RelationType.DEPENDS_ON;
        relationIndicatorBtn.setText(relationType.toString().replace("_", " "));
        //whatIfSelection = execution.getWhatIf(whatIfSelection.getTargetName(), relationType);
        whatIfList.getItems().setAll(filterNonImmediateCB.isSelected() ?
                whatIfSelection.getAllImmediate() :
                whatIfSelection.getAllRelated());*/
    }

    @FXML
    void filterBtn(ActionEvent event) {

    }
/*
    public void setAppController(ControlPanelController appController, Engine execution) {
        this.appController = appController;
        this.execution = execution;
    }

    public void loadTargetList() {
        targetListView.getItems().addAll(execution.getAllTargetNames());
        targetListView.setPlaceholder(new Text("No targets found"));
        targetListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            whatIfSelection = execution.getWhatIf(newValue, relationType);
            whatIfList.getItems().setAll(filterNonImmediateCB.isSelected() ?
                    whatIfSelection.getAllImmediate() :
                    whatIfSelection.getAllRelated());
        });

        filterNonImmediateCB.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                whatIfList.getItems().setAll(whatIfSelection.getAllImmediate());
            } else {
                whatIfList.getItems().setAll(whatIfSelection.getAllRelated());
            }
        });
    }
*/
}
