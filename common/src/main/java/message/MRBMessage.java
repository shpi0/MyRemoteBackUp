package message;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Класс для отправки сообщений между клиентом и сервером.
 * Тип сообщения и необходимые данные в виде строки.
 */
@Data
public class MRBMessage extends AbstractMessage implements Serializable {

    private MessageType messageType;
    private List<?> data;

    public MRBMessage(MessageType messageType) {
        this.messageType = messageType;
        this.data = null;
    }

    public MRBMessage(MessageType messageType, List<?> data) {
        this.messageType = messageType;
        this.data = data;
    }

}
