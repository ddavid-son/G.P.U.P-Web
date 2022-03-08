package app.Utils;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FXUtil {
    public static void handleErrors(Exception e, String bodyMessage, String headerMessage) {
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

    public static Node getIcon(String resourceName, int size) {
        Image image = new Image("/resources/icons" + resourceName);
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(size);
        imageView.setFitWidth(size);
        return imageView;
    }
}
