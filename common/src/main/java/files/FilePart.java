package files;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Data
public class FilePart {

    private long firstBytePos;

    private byte[] fileData;

    private String fileName;

    private String filePath;

    private int totalParts;

    private int currentPartNum;

    public FilePart(long firstBytePos, byte[] fileData, String fileName, String filePath, int totalParts, int currentPartNum) {
        this.firstBytePos = firstBytePos;
        this.fileData = fileData;
        this.fileName = fileName;
        this.filePath = filePath;
        this.totalParts = totalParts;
        this.currentPartNum = currentPartNum;
    }

    public FilePart(Path path, String filePath) throws IOException {
        this.firstBytePos = 0;
        this.fileData = Files.readAllBytes(path);
        this.fileName = path.getFileName().toString();
        this.filePath = filePath;
        this.totalParts = 1;
        this.currentPartNum = 1;
    }

}
