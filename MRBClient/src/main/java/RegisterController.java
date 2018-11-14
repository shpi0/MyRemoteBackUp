import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import message.MRBMessage;
import message.MessageType;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {
    @FXML
    TextField login;

    @FXML
    PasswordField password;

    @FXML
    PasswordField password2;

    @FXML
    VBox globParent;

    @FXML
    Button regBtn;

    public int id;

    private Runnable r = new Runnable() {
        @Override
        public void run() {
            regBtn.setDisable(true);
            if (password.getText() != null && password2.getText() != null && login.getText() != null) {
                if (password.getText().length() >= 4 && login.getText().length() >= 4) {
                    if (password.getText().equals(password2.getText())) {
                        regBtn.setDisable(false);
                    }
                }
            }
        }
    };

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        regBtn.setDisable(true);
        password.setOnKeyReleased(value -> r.run());
        password2.setOnKeyReleased(value -> r.run());
        login.setOnKeyReleased(value -> r.run());
    }

    public void close(ActionEvent actionEvent) {
        globParent.getScene().getWindow().hide();
    }

    public void reg(ActionEvent actionEvent) {
        Network.sendMsg(new MRBMessage(MessageType.REGISTER_REQUEST, new ArrayList<>(Arrays.asList(login.getText(), password.getText(), password2.getText()))));
        globParent.getScene().getWindow().hide();
    }
}
