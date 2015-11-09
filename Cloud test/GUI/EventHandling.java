import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.mavlink.messages.ja4rtor.msg_command_long;
import org.mavlink.messages.ja4rtor.msg_mission_item;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class EventHandling {
    int sysId = 0;
    int componentId = 0;
    private Socket clientSocket;
    private int port = 12345;
    private WebEngine engine;
    ObjectOutputStream objectOut;
    Message msg;
    byte[] mavLinkByteArray = null;

    @FXML
    private Button missionButton;
    @FXML
    private Button liftButton;
    @FXML
    private Button landButton;
    @FXML
    private WebView webView;
    @FXML
    private TextField latTextfield;
    @FXML
    private TextField lngTextfield;
    @FXML
    CheckBox connectedCheckBox;
    @FXML
    private Slider wifiSlider;

    public EventHandling() {
        System.out.println("Gui created!");
    }

    @FXML
    private void initialize() {
        System.out.println("Init!");
        setUpServer();

        missionButton.setOnAction((event) -> {
            flyTo();
        });

        liftButton.setOnAction((event) -> {
            lift();
        });

        landButton.setOnAction((event) -> {
            land();
        });

        wifiSlider.setOnDragDone((event) -> {
             wifi(wifiSlider.getValue());
        });

        engine = webView.getEngine();
        String url = Main.class.getResource("map.html").toExternalForm();
        engine.load(url);

        JSObject window = (JSObject)engine.executeScript("window");
        window.setMember("app", new JavaScriptCalls());

    }

    public class JavaScriptCalls {
        public void updateLatAndLng(String lat, String lng) {
            latTextfield.setText(lat);
            lngTextfield.setText(lng);
        }
    }

    void setUpServer() {
        System.out.println("setup server socket!");
        Task<Socket> task = new Task<Socket>() {
            @Override
            protected Socket call() {
                ServerSocket serverSocket = null;

                try {
                    serverSocket = new ServerSocket(port);
                    clientSocket = serverSocket.accept();//The thread is blocked here
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Connection accepted!");
                Server serverTask = new Server(EventHandling.this, clientSocket);
                new Thread(serverTask).start();
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    System.err.println("Closing server socket failed!");
                }
                return clientSocket;
            }
        };
        new Thread(task).start();

        task.setOnSucceeded((workerStateEvent) -> {//When serverSocket.accept() is done, retrieve the handle to the client and create stream
            clientSocket = task.getValue();
            connectedCheckBox.setText("Drone connected");
            connectedCheckBox.setSelected(true);
            try {
                objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            msg = new Message();
        });
    }

    private void flyTo() {
        msg_mission_item mi = new msg_mission_item(sysId, componentId);
        mi.target_system = sysId;
        mi.target_component = componentId;
        mi.seq = 1;
        mi.frame = 0;
        mi.command = 17;
        mi.current = 0; //No idea what this is
        mi.autocontinue = 1; //Autocontinue, but there wont be anymore until the next command is sent?
        mi.param1 = 0;
        mi.param2 = 0;
        mi.param3 = 0;
        mi.param4 = 0;
        mi.x = Float.parseFloat(latTextfield.getText());
        mi.y = Float.parseFloat(lngTextfield.getText());
        mi.z = 0;

        try {
            mavLinkByteArray = mi.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg.setByteArray(mavLinkByteArray);
        msg.setMessageType(MessageType.MAVLINK);

        try {
            objectOut.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void lift() {
        msg_mission_item mi = new msg_mission_item(sysId, componentId);
        mi.target_system = sysId;
        mi.target_component = componentId;
        mi.seq = 1;
        mi.frame = 1;
        mi.command = 22;
        mi.current = 0; //No idea what this is
        mi.autocontinue = 1; //Autocontinue, but there wont be anymore until the next command is sent?
        mi.param1 = 0;
        mi.param2 = 0;
        mi.param3 = 0;
        mi.param4 = 0;
        mi.x = Float.parseFloat(latTextfield.getText());
        mi.y = Float.parseFloat(lngTextfield.getText());
        mi.z = 0;

        try {
            mavLinkByteArray = mi.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg.setByteArray(mavLinkByteArray);
        msg.setMessageType(MessageType.MAVLINK);
        try {
            objectOut.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void land() {
        msg_mission_item mi = new msg_mission_item(sysId, componentId);
        mi.target_system = sysId;
        mi.target_component = componentId;
        mi.seq = 1;
        mi.frame = 1;
        mi.command = 21;
        mi.current = 0; //No idea what this is
        mi.autocontinue = 1; //Autocontinue, but there wont be anymore until the next command is sent?
        mi.param1 = 0;
        mi.param2 = 0;
        mi.param3 = 0;
        mi.param4 = 0;
        mi.x = Float.parseFloat(latTextfield.getText());
        mi.y = Float.parseFloat(lngTextfield.getText());
        mi.z = 0;

        try {
            mavLinkByteArray = mi.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        msg.setByteArray(mavLinkByteArray);
        msg.setMessageType(MessageType.MAVLINK);
        try {
            objectOut.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void wifi(double status) {
        msg.setByteArray(null);

        if(status == 0) {
            msg.setMessageType(MessageType.WIFIOFF);
        }
        else {
            msg.setMessageType(MessageType.WIFION);
        }

        try {
            objectOut.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    msg_command_long cl = new msg_command_long();//testing in progress
}