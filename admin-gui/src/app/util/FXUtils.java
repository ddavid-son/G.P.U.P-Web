package app.util;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public class FXUtils {
    public static void handleErrors(Exception e, String bodyMessage, String headerMessage) {
       /* Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error occurred");
            alert.setHeaderText(headerMessage);
            if (e != null) {
                alert.setContentText(e.getMessage());
            } else {
                alert.setContentText(bodyMessage);
            }
            alert.showAndWait();
        });*/
        System.out.println(e.getMessage());
    }

}
