import java.io.Serializable;

enum MessageType {LIFT, LAND, HOVER, GOTO, WIFION, WIFIOFF};

public class Message implements Serializable {
    private static final long serialVersionUID = 124234678453620001L;
    private byte [] byteArray;
    private MessageType messageType;

    public byte[] getByteArray() {
        return byteArray;
    }

    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
