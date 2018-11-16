package message;

import lombok.Data;

import java.io.Serializable;

/**
 * Сообщение с содержимым файла.
 * Если файл отправляется в одном сообщении целиком - undivided = true, иначе
 * указывается общее кол-во кусков файла partsCount, чтобы обратная сторона понимала,
 * сколько сообщений нужно принять и номер текущего куска файла partNum.
 */

@Data
public class FileMessage extends AbstractMessage implements Serializable {

    private String fileName;
    private byte[] fileData;
    private int partsCount;
    private int partNum;
    private String folder;

    public boolean isDivided() {
        return partsCount > 1;
    }

    public boolean isLastPart() {
        return partNum == partsCount;
    }

    public boolean isFirstPart() {
        return partNum == 1;
    }

    public FileMessage(String fileName, byte[] fileData) {
        this.fileName = fileName;
        this.fileData = fileData;
        this.partsCount = 1;
        this.partNum = 1;
    }

    public FileMessage(String fileName, String folder, byte[] fileData, int partsCount, int partNum) {
        this.fileName = fileName;
        this.folder = folder;
        this.fileData = fileData;
        this.partsCount = partsCount;
        this.partNum = partNum;
    }

}
