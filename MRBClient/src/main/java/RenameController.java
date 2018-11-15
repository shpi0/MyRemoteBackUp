import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class RenameController implements Initializable {

    protected MainController main;

    @FXML
    TextField fileName;

    @FXML
    Button renameBtn;

    @FXML
    VBox globParent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        renameBtn.setDisable(true);
        fileName.setOnKeyReleased(value -> {
            if (fileName.getText().length() > 0) {
                renameBtn.setDisable(false);
            } else {
                renameBtn.setDisable(true);
            }
        });
    }

    public void close(ActionEvent actionEvent) {
        globParent.getScene().getWindow().hide();
    }

    public void rename(ActionEvent actionEvent) {
        main.newFileName = fileName.getText();
        globParent.getScene().getWindow().hide();
    }
}
