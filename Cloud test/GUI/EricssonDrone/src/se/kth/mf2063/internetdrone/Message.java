package se.kth.mf2063.internetdrone;

import java.io.Serializable;

/**
 * A serializable class to send to the drone.
 */
public class Message implements Serializable {
    /**
     * The ID of the message.
     */
    private static final long serialVersionUID = 124234678453620001L;
    /**
     * The Mavlink message byte array if messageType is Mavlink
     */
    private byte [] byteArray;
    /**
     * The type of the message to be sent(either Mavlink to the drone or a command to the phone)
     */
    private MessageType messageType;

    /**
     * Returns the byte array containing the Mavlink message.
     * @return the byte array containing the Mavlink message.
     */
    public byte[] getByteArray() {
        return byteArray;
    }

    /**
     * Sets the Mavlink message byte array.
     * @param  byteArray The Mavlink message byte array.
     */
    public void setByteArray(byte[] byteArray) {
        this.byteArray = byteArray;
    }

    /**
     * Returns the type of the Message.
     * @return the type of the Message.
     * @see    MessageType
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * Sets type of the medssage to be sent(either Mavlink to the drone or a command to the phone)
     * @param  messageType  The type of the message to be sent(either Mavlink to the drone or a command to the phone)
     * @see    MessageType
     */
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}