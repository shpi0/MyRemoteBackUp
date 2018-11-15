import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import message.MRBMessage;
import message.MessageType;

import java.util.ArrayList;
import java.util.Arrays;

public class LoginController {
    @FXML
    TextField login;

    @FXML
    PasswordField password;

    @FXML
    VBox globParent;

    public void auth(ActionEvent actionEvent) {
        Network.sendMsg(new MRBMessage(MessageType.LOGIN_ATTEMPT, new ArrayList<>(Arrays.asList(login.getText(), password.getText()))));
        globParent.getScene().getWindow().hide();
    }
}
