import files.FileMessageProcessor;
import files.FileType;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import message.AbstractMessage;
import message.FileMessage;
import message.MRBMessage;
import message.MessageType;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MainController implements Initializable {

    private final static String ROOT_FOLDER = "data";
    private final static String PARENT_FOLDER_LINK = "[..]";

    private boolean loggedIn = false;
    private String selectedFile;
    private boolean localListSelected;
    protected String newFileName;
    private Map<String, FileType> localFilesMap = new HashMap<>();
    private Map<String, FileType> serverFilesMap = new HashMap<>();
    private LinkedList<String> folders = new LinkedList<>();

    @FXML
    ListView<String> localListView;

    @FXML
    ListView<String> serverListView;

    @FXML
    Button loginBtn;

    @FXML
    Button deleteBtn;

    @FXML
    Button refreshBtn;

    @FXML
    Button sendBtn;

    @FXML
    Button getBtn;

    @FXML
    Button registerBtn;

    @FXML
    Button renameBtn;

    @FXML
    Button newFolderBtn;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Network.start();
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    AbstractMessage msg = Network.readObject();
                    if (msg instanceof FileMessage) {
                        FileMessageProcessor.getInstance().fileMessageProcess((FileMessage) msg);
                        refreshLocalFileList();
                    }
                    if (msg instanceof MRBMessage) {
                        switch (((MRBMessage) msg).getMessageType()) {
                            case LOGIN_FAILED:
                                showAlert("Login failed!");
                                break;
                            case LOGIN_SUCCESS:
                                loggedIn = true;
                                showAlert("Login ok!");
                                Network.sendMsg(new MRBMessage(MessageType.FILE_LIST_REQUEST));
                                loginBtn.setDisable(true);
                                registerBtn.setDisable(true);
                                refreshBtn.setDisable(false);
                                break;
                            case REGISTER_DONE:
                                showAlert("Registration done! You may login now.");
                                break;
                            case REGISTER_FAIL:
                                showAlert("Registration failed!");
                                break;
                            case FILE_LIST:
                                refreshServerFileList((Map<String, FileType>) ((MRBMessage) msg).getFilesData(), (List<String>) ((MRBMessage) msg).getData());
                                break;
                            case FILE_DELETE_OK:
                                refreshButton(new ActionEvent());
                                break;
                            case FILE_RECEIVED_SUCCESS:
                                refreshButton(new ActionEvent());
                                break;
                            case FILE_RENAME_SUCCESS:
                                refreshButton(new ActionEvent());
                                break;
                            case FILE_RENAME_FAIL:
                                showAlert("File renaming on server failed!");
                                break;
                            case CREATE_FOLDER_FAIL:
                                showAlert("Creating folder on server failed!");
                                break;
                            case CREATE_FOLDER_SUCCESS:
                                refreshButton(new ActionEvent());
                                break;
                        }
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
        initializeLocalListView();
        initializeServerListView();
        loginBtn.setDisable(false);
        refreshBtn.setDisable(true);
        deleteBtn.setDisable(true);
        sendBtn.setDisable(true);
        getBtn.setDisable(true);
        renameBtn.setDisable(true);
        newFolderBtn.setDisable(true);
    }

    private String buildPathToCurrentFolder() {
        StringBuilder sb = new StringBuilder();
        sb.append(ROOT_FOLDER).append("/");
        for (String s :folders) {
            sb.append(s);
            sb.append("/");
        }
        return sb.toString();
    }

    public void initializeLocalListView() {
        refreshLocalFileList();
        localListView.setOnMouseClicked(event -> {
            selectedFile = localListView.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2) {
                    if (localFilesMap.get(selectedFile).equals(FileType.FOLDER)) {
                        if (selectedFile.equals(PARENT_FOLDER_LINK)) {
                            folders.removeLast();
                        } else {
                            folders.add(selectedFile.substring(1, selectedFile.length() - 1));
                        }
                        refreshLocalFileList();
                    }
            }
            getBtn.setDisable(true);
            if (!Paths.get(ROOT_FOLDER + "/" + selectedFile).toFile().isDirectory()) {
                sendBtn.setDisable(false);
            } else {
                sendBtn.setDisable(true);
            }
            localListSelected = true;
            renameBtn.setDisable(false);
            deleteBtn.setDisable(false);
            newFolderBtn.setDisable(false);
        });
    }

    public void initializeServerListView() {
        serverListView.setOnMouseClicked(event -> {
            selectedFile = serverListView.getSelectionModel().getSelectedItem();
            if (event.getClickCount() == 2) {
                if (serverFilesMap.get(selectedFile).equals(FileType.FOLDER)) {
                    Network.sendMsg(new MRBMessage(MessageType.FOLDER_CHANGE, new ArrayList<>(Arrays.asList(selectedFile))));
                }
            }
            sendBtn.setDisable(true);
            getBtn.setDisable(false);
            localListSelected = false;
            renameBtn.setDisable(false);
            deleteBtn.setDisable(false);
            newFolderBtn.setDisable(false);
        });
    }

    private void refreshLocalFileList() {
        runFX(() -> {
            try {
                localFilesMap.clear();
                localListView.getItems().clear();
                if (!buildPathToCurrentFolder().equals(ROOT_FOLDER + "/")) {
                    localFilesMap.put(PARENT_FOLDER_LINK, FileType.FOLDER);
                    localListView.getItems().add(PARENT_FOLDER_LINK);
                }
                Files.list(Paths.get(buildPathToCurrentFolder()))
                        .filter(path -> Files.isDirectory(path))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> {
                            o = "[" + o + "]";
                            localFilesMap.put(o, FileType.FOLDER);
                            localListView.getItems().add(o);
                        });
                Files.list(Paths.get(buildPathToCurrentFolder()))
                        .filter(path -> !Files.isDirectory(path))
                        .map(p -> p.getFileName().toString())
                        .forEach(o -> {
                            localFilesMap.put(o, FileType.FILE);
                            localListView.getItems().add(o);
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void refreshServerFileList(Map<String, FileType> fileMap, List<String> fileList) {
        if (loggedIn) {
            serverFilesMap.clear();
            serverFilesMap.putAll(fileMap);
            runFX(() -> {
                serverListView.getItems().clear();
                fileList.forEach(o -> serverListView.getItems().add(o));
            });
        }
    }

    private void runFX(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public void showAlert(String text) {
        runFX(() -> new Alert(Alert.AlertType.WARNING, text, ButtonType.OK).showAndWait());
    }

    public void sendButton(ActionEvent actionEvent) {
        if (loggedIn) {
            Network.sendMsg(FileMessageProcessor.getInstance().generateFileMessage(Paths.get(ROOT_FOLDER + "/" + selectedFile)));
        }
    }

    public void getButton(ActionEvent actionEvent) {
        if (loggedIn) {
            Network.sendMsg(new MRBMessage(MessageType.FILE_REQUEST, new ArrayList<String>(Arrays.asList(selectedFile))));
        }
    }

    public void deleteButton(ActionEvent actionEvent) {
        if (loggedIn) {
            if (localListSelected) {
                try {
                    Files.deleteIfExists(Paths.get(ROOT_FOLDER + "/" + selectedFile));
                    refreshLocalFileList();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Network.sendMsg(new MRBMessage(MessageType.FILE_DELETE, new ArrayList<String>(Arrays.asList(selectedFile))));
            }
        }
    }

    public void refreshButton(ActionEvent actionEvent) {
        refreshLocalFileList();
        if (loggedIn) {
            Network.sendMsg(new MRBMessage(MessageType.FILE_LIST_REQUEST));
        }
    }

    public void newFolderButton(ActionEvent actionEvent) {
        newFileName = null;
        if (loggedIn) {
            try {
                Stage stage = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/NewFolder.fxml"));
                Parent root = loader.load();
                NewFolderController lc = (NewFolderController) loader.getController();
                lc.main = this;
                stage.setTitle("Create folder");
                stage.setScene(new Scene(root, 400, 200));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (newFileName == null || "".equals(newFileName)) {
                return;
            }
            if (localListSelected) {
                try {
                    Files.createDirectory(Paths.get(buildPathToCurrentFolder() + newFileName));
                } catch (IOException e) {
                    showAlert("Directory creation failed!");
                    e.printStackTrace();
                }
                refreshLocalFileList();
            } else {
                Network.sendMsg(new MRBMessage(MessageType.CREATE_FOLDER, new ArrayList<String>(Arrays.asList(newFileName))));
            }
        }
    }

    public void renameButton(ActionEvent actionEvent) {
        newFileName = null;
        if (loggedIn) {
            try {
                Stage stage = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Rename.fxml"));
                Parent root = loader.load();
                RenameController lc = (RenameController) loader.getController();
                lc.main = this;
                lc.fileName.setText(selectedFile);
                stage.setTitle("Rename file");
                stage.setScene(new Scene(root, 400, 200));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (newFileName == null || "".equals(newFileName)) {
                return;
            }
            if (localListSelected) {
                try {
                    Files.move(Paths.get(ROOT_FOLDER + "/" + selectedFile), Paths.get(ROOT_FOLDER + "/" + newFileName));
                } catch (IOException e) {
                    showAlert("File renaming failed!");
                    e.printStackTrace();
                }
                refreshLocalFileList();
            } else {
                Network.sendMsg(new MRBMessage(MessageType.FILE_RENAME, new ArrayList<String>(Arrays.asList(selectedFile, newFileName))));
            }
        }
    }

    public void registerButton(ActionEvent actionEvent) {
        if (!loggedIn) {
            try {
                Stage stage = new Stage();
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/Register.fxml"));
                Parent root = loader.load();
                RegisterController lc = (RegisterController) loader.getController();
                stage.setTitle("Registration");
                stage.setScene(new Scene(root, 400, 200));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loginButton(ActionEvent actionEvent) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Parent root = loader.load();
            LoginController lc = (LoginController) loader.getController();
            stage.setTitle("Authorization");
            stage.setScene(new Scene(root, 400, 200));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
