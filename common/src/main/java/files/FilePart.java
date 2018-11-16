package files;

import lombok.Data;

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
}
