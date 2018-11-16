package files;

import message.FileMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

}
