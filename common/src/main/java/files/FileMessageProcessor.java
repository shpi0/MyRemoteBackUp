package files;

import message.FileMessage;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileMessageProcessor {

    public void addFileDataToFilePart(FilePart fp, int maxFileSize, byte[] data) throws IOException {
        if (fp.getFileData() == null) {
            RandomAccessFile raf = new RandomAccessFile(Paths.get(fp.getLocalFilePath() + fp.getFileName()).toFile(), "r");
            raf.seek((fp.getCurrentPartNum() - 1) * maxFileSize);
            System.out.println("Reading part " + fp.getCurrentPartNum() + " of file " + fp.getFileName() + ", part size: " + fp.getPartSize() + " bytes");
            if (data.length != fp.getPartSize()) {
                data = new byte[fp.getPartSize()];
            }
            raf.read(data, 0, fp.getPartSize());
            fp.setFileData(data);
            raf.close();

        }
    }

    public void writeIncomingFileMessageToDisk(FileMessage msg, String localFolder, int maxFileSize) throws IOException {
        if (msg.isDivided()) {
            RandomAccessFile raf = new RandomAccessFile(msg.getFolder() + msg.getFileName(), "rw");
            raf.seek((msg.getPartNum() - 1) * maxFileSize);
            raf.write(msg.getFileData());
            raf.close();
            System.out.println("Write file part " + msg.getPartNum());
        } else {
            Files.write(Paths.get(localFolder + msg.getFileName()), msg.getFileData(), StandardOpenOption.CREATE);
        }
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
