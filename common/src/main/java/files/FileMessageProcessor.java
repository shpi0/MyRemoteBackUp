package files;

import message.FileMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileMessageProcessor {
    private static FileMessageProcessor ourInstance = new FileMessageProcessor();

    public static FileMessageProcessor getInstance() {
        return ourInstance;
    }

    private FileMessageProcessor() {
    }

    public FileMessage generateFileMessage(Path path) {
        byte[] data = null;
        try {
            data = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new FileMessage(path.getFileName().toString(), data);
    }

    public void fileMessageProcess(String userName, FileMessage message) {
        try {
            Files.write(Paths.get("data/" + userName + "/" + message.getFileName()), message.getFileData(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fileMessageProcess(FileMessage message) {
        try {
            Files.write(Paths.get("data/" + message.getFileName()), message.getFileData(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
