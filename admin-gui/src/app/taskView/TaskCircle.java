package app.taskView;

import argumentsDTO.CommonEnums.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class TaskCircle {
    @FXML
    private Button transparentBtn;
    private StackPane stackPane;
    private Label targetName;
    private Circle circle;

    public TaskCircle(String name, TargetState state) {
        stackPane = new StackPane();
        targetName = new Label(name);
        targetName.resize(90, 30);
        circle = new Circle(50, Paint.valueOf("BLUE"));
        transparentBtn = new Button();
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
    }

    public TaskCircle() {
        stackPane = new StackPane();
        targetName = new Label("emptyCtor");
        targetName.resize(90, 30);
        circle = new Circle(50, Paint.valueOf("BLUE"));
        transparentBtn = new Button();
        transparentBtn.resize(80, 80);
        transparentBtn.opacityProperty().setValue(0);
        stackPane.getChildren().add(circle);
        stackPane.getChildren().add(targetName);
        stackPane.getChildren().add(transparentBtn);
        stackPane.setId("emptyCtor");
    }

    public StackPane getStackPane() {
        return stackPane;
    }
}
