import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;

public class Server extends Task<Void> {
    private int port;
    private Socket clientSocket;
    @FXML
    private CheckBox connectedCheckBox;

    public Server(int port, EventHandling handleGUI, Socket clientSocket) {
        this.port=port;
        this.clientSocket=clientSocket;
    }

    @Override
    protected Void call() throws Exception {
        BufferedReader in = null;

        System.out.println("Receiving server thread running!");
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String msg;
            while (true) {
                if ((msg = in.readLine()) != null) {
                    System.out.println(msg);
                }
                else {
                    return null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            in.close();
            clientSocket.close();
            connectedCheckBox.setText("Drone not connected");//Does not really work
            connectedCheckBox.setSelected(false);
            System.out.println("Drone disconnected!");
        }
        return null;
    }
}