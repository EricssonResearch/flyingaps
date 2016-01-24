
import java.io.*;
import java.net.Socket;

import javafx.application.Platform;
import javafx.concurrent.Task;
import se.kth.mf2063.internetdrone.Message;

/**
 * A class to receive all the messages and handle them.
 */
public class Server extends Task<Void> {
    /**
     * A reference to the client socket.
     */
    private Socket clientSocket;
    /**
     * A reference to the WpProtocol object.
     */
    private WpProtocol wpProtocol;
    /**
     * A reference to the GUI instance.
     */
    private EventHandling handleGUI;

    /**
     * Creates the server object and sets all fields.
     * @param clientSocket A reference to the client socket.
     * @param handleGUI    A reference to the GUI instance.
     * @param wpProtocol   A reference to the WpProtocol object.
     */
    public Server(EventHandling handleGUI, Socket clientSocket, WpProtocol wpProtocol) {
        this.clientSocket = clientSocket;
        this.handleGUI = handleGUI;
        this.wpProtocol = wpProtocol;
    }

    /**
     * Starting point of thread. After it receives a message from the drone, it is handled in DroneCommunication.
     */
    @Override
    protected Void call(){

        System.out.println("Receiving server thread running!");
        Message msg;
        Object receivedObject;
        String mavlinkMessageInfo;

        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(ois != null ) {
            try {
                receivedObject = ois.readObject();
            } catch (IOException e) {
                ois = null;
                continue;
            } catch (ClassNotFoundException e) {
                ois = null;
                continue;
            }

            if ((receivedObject != null) && (receivedObject instanceof Message)) {
                msg = (Message) receivedObject;

                mavlinkMessageInfo = DroneCommunication.mavlink_decode(msg.getByteArray(), wpProtocol);

                if(mavlinkMessageInfo.length() != 0)
                    System.out.println(mavlinkMessageInfo);
            } else {
                System.err.println("Incorrect object received. Check Message.java version.");
            }
        }

        System.out.println("Drone disconnected!");
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Closing socket failed!");
        }
        Platform.runLater(() -> {
            handleGUI.connectedCheckBox.setText("Drone not connected");
            handleGUI.connectedCheckBox.setSelected(false);
        });
        handleGUI.setUpServer(); // Restart everything

        return null;
    }
}