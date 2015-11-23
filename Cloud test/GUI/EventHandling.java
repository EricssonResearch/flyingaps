
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.mavlink.messages.ja4rtor.*;
import se.kth.mf2063.internetdrone.Message;
import se.kth.mf2063.internetdrone.MessageType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class EventHandling {
    int sysId = 255;
    int componentId = 190;
    int target_sysId = 1;
    int target_componentId = 1;
    //int sysId = 100;
    //int componentId = 100;
    private Socket clientSocket;
    private int port = 12345;
    private WebEngine engine;
    ObjectOutputStream objectOut;
    private double wifiStatus = 0;
    private int missionNumber=0;

    @FXML
    private Button flyButton;
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
    @FXML
    private Button armButton;
    @FXML
    private Button statusButton;
    @FXML
    private Button rcButton;
    @FXML
    private Button homeButton;
    @FXML
    private TextField altTextfield;
    @FXML
    private Button missionButton;
    @FXML
    private Button ackButton;


    public EventHandling() {
        System.out.println("Gui created!");
    }

    @FXML
    private void initialize() {
        System.out.println("Init!");
        setUpServer();

        flyButton.setOnAction((event) -> {
            flyTo();
        });

        liftButton.setOnAction((event) -> {
            lift();
        });

        landButton.setOnAction((event) -> {
            land();
        });

        rcButton.setOnAction((event) -> {
            rc();
        });

        statusButton.setOnAction((event) -> {
            status();
        });

        armButton.setOnAction((event) -> {
            arm();
        });

        wifiSlider.setOnMouseReleased((event) -> {
            wifi(wifiSlider.getValue());
        });

        homeButton.setOnAction((event) -> {
            home();
        });

        ackButton.setOnAction((event) -> {
            home();
        });
        missionButton.setOnAction((event) -> {
            home();
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

    void setUpServer() {//Only allow one connection
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
        });
    }

    private void flyTo() {
        byte[] mavLinkByteArray = null;

        msg_mission_item mi = new msg_mission_item(sysId, componentId);
        mi.target_system = target_sysId;
        mi.target_component = target_componentId;
        mi.seq = missionNumber;
        mi.frame = 3;
        mi.command = 17;
        mi.current = 0;
        mi.autocontinue = 0;
        mi.x = Float.parseFloat(latTextfield.getText());
        mi.y = Float.parseFloat(lngTextfield.getText());
        mi.z = 0;

        try {
            mavLinkByteArray = mi.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
        executeMission();
    }
    private void rc() {
        byte[] mavLinkByteArray = null;

        msg_rc_channels_override rc = new msg_rc_channels_override(sysId, componentId);
        rc.target_system = target_sysId;
        rc.target_component = target_componentId;
        rc.chan1_raw = 1000;
        rc.chan2_raw = 1000;
        rc.chan3_raw = 1000;
        rc.chan4_raw = 1000;
        rc.chan5_raw = 1000;
        rc.chan6_raw = 1000;
        rc.chan7_raw = 1000;
        rc.chan8_raw = 1000;

        try {
            mavLinkByteArray = rc.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
    }
    private void lift() {
        byte[] mavLinkByteArray = null;

        /*
        msg_mission_item mi = new msg_mission_item(sysId, componentId);
        mi.target_system = 1;
        mi.target_component = 1;
        mi.seq = missionNumber;
        mi.frame = 1;
        mi.command = 22;
        mi.current = 0;
        mi.autocontinue = 0;
        mi.x = 0;
        mi.y = 0;
        mi.z = 1000;
        */

        msg_command_long cl = new msg_command_long(sysId, componentId);
        cl.target_system = target_sysId;
        cl.target_component = target_componentId;
        cl.command = 24;
        cl.param7 = Float.parseFloat(altTextfield.getText());

        try {
            mavLinkByteArray = cl.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
        //executeMission();
    }
    private void land() {
        byte[] mavLinkByteArray = null;

        msg_mission_item mi = new msg_mission_item(sysId, componentId);
        mi.target_system = target_sysId;
        mi.target_component = target_componentId;
        mi.seq = missionNumber;
        mi.frame = 3;
        mi.command = 21;
        mi.current = 0;
        mi.autocontinue = 0;
        mi.x = 0;
        mi.y = 0;
        mi.z = 0;

        try {
            mavLinkByteArray = mi.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
        executeMission();
    }

    private void mission() {
        byte[] mavLinkByteArray = null;

        msg_mission_count mc = new msg_mission_count(sysId, componentId);
        mc.count = 2;

        try {
            mavLinkByteArray = mc.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);

        msg_mission_item mi1 = new msg_mission_item(sysId, componentId);
        mi1.param1 = 0;
        mi1.param2 = 0;
        mi1.param3 = 0;
        mi1.param4 = 0;
        mi1.x = 59;
        mi1.y = 18;
        mi1.z = 40;
        mi1.seq = 0;
        mi1.command = 16;
        mi1.frame = 0;
        mi1.current = 0;
        mi1.autocontinue = 1;

        try {
            mavLinkByteArray = mi1.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);

        msg_mission_item mi2 = new msg_mission_item(sysId, componentId);
        mi2.param1 = 0;
        mi2.param2 = 0;
        mi2.param3 = 0;
        mi2.param4 = 0;
        mi2.x = 0;
        mi2.y = 0;
        mi2.z = 0;
        mi2.seq = 1;
        mi2.command = 22;
        mi2.frame = 0;
        mi2.current = 0;
        mi2.autocontinue = 1;

        try {
            mavLinkByteArray = mi1.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
    }
    private void ack() {
        byte[] mavLinkByteArray = null;

        msg_mission_ack mi = new msg_mission_ack(sysId, componentId);
        mi.target_system = target_sysId;
        mi.target_component = target_componentId;
        mi.type = 0;

        try {
            mavLinkByteArray = mi.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
    }

    private void status() {
        byte[] mavLinkByteArray = null;
        /*msg_param_request_list mprl = new msg_param_request_list(sysId, componentId);
        mprl.target_system = 1;
        mprl.target_component = 1;*/

        msg_command_long cl = new msg_command_long(sysId, componentId);
        cl.target_system = target_sysId;
        cl.target_component = target_componentId;
        cl.command = 511;
        cl.param1 = 147;
        cl.param2 = 0;

        try {
            mavLinkByteArray = cl.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
    }

    private void arm() {
        byte[] mavLinkByteArray = null;
        msg_command_long cl = new msg_command_long(sysId, componentId);
        cl.target_system = target_sysId;
        cl.target_component = target_componentId;
        cl.command = 400; //MAV_CMD_COMPONENT_ARM_DISARM
        cl.param1 = 1;
        cl.confirmation = 0; //Is this gonna work...? Maybe not even needed

        try {
            mavLinkByteArray = cl.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);

        msg_set_mode sm = new msg_set_mode(sysId, componentId);
        sm.target_system = target_sysId;
        sm.base_mode = 220;
        sm.custom_mode = 0;

        try {
            mavLinkByteArray = sm.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
    }

    private void home() {
        byte[] mavLinkByteArray = null;
        msg_command_long cl = new msg_command_long(sysId, componentId);
        cl.target_system = target_sysId;
        cl.target_component = target_componentId;
        cl.command = 179;
        cl.param1 = 1;
        cl.confirmation = 0; //Is this gonna work...? Maybe not even needed

        try {
            mavLinkByteArray = cl.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
    }

    private void wifi(double status) {
        if(wifiStatus == status)
            return;

        wifiStatus=status;

        if(status == 0) {
            send(MessageType.WIFIOFF, null);
        }
        else {
            send(MessageType.WIFION, null);
        }
    }

    private void executeMission() {
        byte[] mavLinkByteArray = null;
        msg_command_long cl = new msg_command_long(sysId, componentId);
        cl.target_system = target_sysId;
        cl.target_component = target_componentId;
        cl.command = 300; //supposed to be MAV_CMD_MISSION_START, but the ENUM does not seem to work for me
        cl.confirmation = 0;
        cl.param1=missionNumber;
        cl.param2=missionNumber;
        missionNumber++;

        try {
            mavLinkByteArray = cl.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
    }

    private void send(MessageType messageType, byte[] mavLinkByteArray) {
        Message msg = new Message();
        msg.setByteArray(mavLinkByteArray);
        msg.setMessageType(messageType);

        try {
            objectOut.writeObject(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}