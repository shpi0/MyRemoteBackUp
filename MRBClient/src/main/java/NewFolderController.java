import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class NewFolderController implements Initializable {
    protected MainController main;

    @FXML
    TextField folderName;

    @FXML
    Button createBtn;

    @FXML
    VBox globParent;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createBtn.setDisable(true);
        folderName.setOnKeyReleased(value -> {
            if (folderName.getText().length() > 0) {
                createBtn.setDisable(false);
            } else {
                createBtn.setDisable(true);
            }
        });
    }

    public void close(ActionEvent actionEvent) {
        globParent.getScene().getWindow().hide();
    }

    public void create(ActionEvent actionEvent) {
        main.newFileName = folderName.getText();
        globParent.getScene().getWindow().hide();
    }
}
