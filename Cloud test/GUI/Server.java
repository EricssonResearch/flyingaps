import java.io.*;
import java.net.Socket;

import javafx.application.Platform;
import javafx.concurrent.Task;

public class Server extends Task<Void> {
    private Socket clientSocket;
    EventHandling handleGUI;

    public Server(EventHandling handleGUI, Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.handleGUI = handleGUI;
    }

    @Override
    protected Void call(){

        System.out.println("Receiving server thread running!");
        Message msg;
        Object receivedObject = null;
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
                e.printStackTrace();
                continue;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            if ((receivedObject != null) && (receivedObject instanceof Message)) {
                msg = (Message) receivedObject;
                mavlinkMessageInfo = DroneCommunication.mavlink_decode(msg.getByteArray());
                System.out.println(mavlinkMessageInfo);
            } else {
                System.err.println("Incorrect object received. Check Massage.java version.");
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