package message;

import files.FilePart;
import lombok.Data;

import java.io.Serializable;

@Data
public class FilePartMessage extends AbstractMessage implements Serializable {

    private FilePart fp;

    public FilePartMessage(FilePart fp) {
        this.fp = fp;
    }
}
