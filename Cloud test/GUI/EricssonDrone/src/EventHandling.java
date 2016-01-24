
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.collections.ObservableList;
import netscape.javascript.JSObject;
import org.mavlink.messages.ja4rtor.*;
import se.kth.mf2063.internetdrone.Message;
import se.kth.mf2063.internetdrone.MessageType;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A class to handle all the events in the GUI. It also instantiates most of the other classes.
 */
public class EventHandling {
    /**
     * The System ID of the Ground Control Station.
     */
    int sysId = 255;
    /**
     * The Component ID of the Ground Control Station.
     */
    int componentId = 190;
    /**
     * The System ID of the drone.
     */
    int target_sysId = 1;
    /**
     * The Component ID of the drone.
     */
    int target_componentId = 1;
    private Socket clientSocket;
    private int port = 9119;
    private WebEngine engine;
    ObjectOutputStream objectOut;
    private double wifiStatus = 0;
    private int missionNumber=0;
    private WpProtocol wpProtocol;
    private Object lock;
    private LinkedBlockingQueue<byte []> q;
    private long startTime;
    private ObservableList<MissionItem> missionItems;

    /**
     * Constant for flight mode parameters. Specific for Pixhawk. Check the documentation supplied for description of
     * the used mode or the official pixhawk documentation for the others.
     */
    private final int mode_RTL = 6, mode_POSHOLD = 16, mode_LAND = 9, mode_OF_LOITER= 10, mode_STABILIZE = 0,
            mode_AUTO = 3, mode_GUIDED = 4, mode_DRIFT = 11, mode_FLIP = 14, mode_AUTOTUNE = 15, mode_ALT_HOLD = 2,
            mode_LOITER = 5, mode_POSITION = 8, mode_CIRCLE = 7, mode_SPORT = 13, mode_ACRO = 1;

    /**
     * reference to button.
     */
    @FXML
    private Button flyButton, liftButton, landButton, statusButton, disarmButton, armButton, missionButton, autoButton, clearButton, currentButton, stabilButton, listButton, guidedButton, startButton, stopButton;
    /**
     * reference to the web view.
     */
    @FXML
    private WebView webView;
    /**
     * reference to textfield for command parameters.
     */
    @FXML
    private TextField latTextfield, lngTextfield, altTextfield, holdTextfield, seqTextfield;
    /**
     * A chechbox indicating if a drone is connected.
     */
    @FXML
    CheckBox connectedCheckBox;
    /**
     * reference to a slider that turns wifi on and off.
     */
    @FXML
    private Slider wifiSlider;
    /**
     * reference to table column.
     */
    @FXML
    private TableColumn commandField, latField, longField, altField, holdField;
    /**
     * reference to the mission list table.
     */
    @FXML
    private TableView missionTable;

    /**
     * Allocates a new EventHandling object.
     */
    public EventHandling() {
        System.out.println("Gui created!");
    }

    /**
     * Is called when the GUI is created and sets up all the action handlers. It also loads the google maps page in the web engine window.
     */
    @FXML
    private void initialize() {
        System.out.println("Init!");
        startTime = System.currentTimeMillis();
        setUpServer();
        missionItems = FXCollections.observableArrayList();
        commandField.setCellValueFactory(new PropertyValueFactory<MissionItem,String>("command"));
        latField.setCellValueFactory(new PropertyValueFactory<MissionItem,Float>("latitude"));
        longField.setCellValueFactory(new PropertyValueFactory<MissionItem,Float>("longitude"));
        altField.setCellValueFactory(new PropertyValueFactory<MissionItem,Float>("altitude"));
        holdField.setCellValueFactory(new PropertyValueFactory<MissionItem,Integer>("time"));

        flyButton.setOnAction((event) -> newMissionItem("FLY_TO"));
        liftButton.setOnAction((event) -> newMissionItem("TAKEOFF"));
        landButton.setOnAction((event) -> newMissionItem("LAND"));

        stabilButton.setOnAction((event) -> changeMode(mode_STABILIZE));
        guidedButton.setOnAction((event) -> changeMode(mode_GUIDED));
        autoButton.setOnAction((event) -> changeMode(mode_AUTO));

        missionButton.setOnAction((event) -> mission());
        statusButton.setOnAction((event) -> status());
        wifiSlider.setOnMouseReleased((event) -> wifi(wifiSlider.getValue()));
        clearButton.setOnAction((event) -> clear());
        currentButton.setOnAction((event) -> current());
        listButton.setOnAction((event) -> list());

        armButton.setOnAction((event) -> arm(1));
        disarmButton.setOnAction((event) -> arm(0));

        startButton.setOnAction((event) -> start());
        stopButton.setOnAction((event) -> stop());

        engine = webView.getEngine();
        String url = Main.class.getResource("map.html").toExternalForm();
        engine.load(url);

        JSObject window = (JSObject)engine.executeScript("window");
        window.setMember("app", new JavaScriptCalls());

    }

    /**
     * A class to handle javascript actions from the web engine.
     */
    public class JavaScriptCalls {
        /**
         * The method is called when the user right clicks on the map in the web engine. It sets the text fields for coordinates in the GUI.
         *
         * @param  lat  the latitude in decimal WGS84 as a string
         * @param  lng  the longitude in decimal WGS84 as a string
         */
        public void updateLatAndLng(String lat, String lng) {
            latTextfield.setText(lat);
            lngTextfield.setText(lng);
        }
    }

    /**
     * The method creates a task that blocks on serverSocket.accept(). When a connection is received within that task, a
     * Server object is created to handle the connection. When the task created has finished running, it
     * returns the reference to the accepted socket to the EventHandling class. The GUI is updated to show that a drone
     * is now connected. A wpProtocol task is created to
     *
     * Strings are printed in the standard output telling the user what is happening.
     */
     void setUpServer() {
        System.out.println("setup server socket!");
        lock = new Object();
        q = new LinkedBlockingQueue<>();
        wpProtocol = new WpProtocol(lock, q);

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

                Server serverTask = new Server(EventHandling.this, clientSocket, wpProtocol);
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

        task.setOnSucceeded((workerStateEvent) -> {//When serverSocket.accept() is done, retrieve the reference to the client and create stream
            clientSocket = task.getValue();
            connectedCheckBox.setText("Drone connected");
            connectedCheckBox.setSelected(true);
            try {
                objectOut = new ObjectOutputStream(clientSocket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            wpProtocol.setObjectOut(objectOut);
            new Thread(wpProtocol).start();
        });
    }

    /**
     * Called when either 'Fly to', 'Land' or 'Lift' are clicked in the GUI. It creates a mission item for the list in the GUI.
     *
     * @param  command  A string containing the mission item type.
     */
    private void newMissionItem(String command) {
        float lat = Float.parseFloat(latTextfield.getText());
        float lng = Float.parseFloat(lngTextfield.getText());
        float alt = Float.parseFloat(altTextfield.getText());
        int time = Integer.parseInt(holdTextfield.getText());
        MissionItem mi;

        if(command == "FLY_TO")
            mi = new MissionItem(command,lat,lng,alt,time,16);
        else if(command == "TAKEOFF")
            mi = new MissionItem(command,0,0,alt,0,22);
        else if(command == "LAND")
            mi = new MissionItem(command,0,0,alt,0,21);
        else {
            mi = new MissionItem("ERROR!",0,0,0,0,0);
        }

        missionItems.add(mi);
        missionTable.setItems(missionItems);
    }

    /**
     * Clears the mission item list and send a message to the drone to clear its list as well.
     */
    private void clear() {
        byte[] mavLinkByteArray = null;

        msg_mission_clear_all mmca = new msg_mission_clear_all(sysId, componentId);
        mmca.target_system = target_sysId;
        mmca.target_component = target_componentId;

        try {
            mavLinkByteArray = mmca.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
        missionTable.getItems().remove(0,missionItems.size());
        missionTable.refresh();
    }

    /**
     * Not properly tested, but it seems like this method(the Mavlink command sent) jumps to the first takeoff mission item in the list and continues from there.
     */
    private void start() {
        byte[] mavLinkByteArray = null;

        msg_command_long cl = new msg_command_long(sysId, componentId);
        cl.target_system = target_sysId;
        cl.target_component = target_componentId;
        cl.command = 22;
        cl.param7 = Float.parseFloat(altTextfield.getText());

        try {
            mavLinkByteArray = cl.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
    }

    /**
     * Not properly tested, but it seems like this method(the Mavlink command sent) jumps to the first land mission item in the list and continues from there.
     */
    private void stop() {
        byte[] mavLinkByteArray = null;

        msg_command_long cl = new msg_command_long(sysId, componentId);
        cl.target_system = target_sysId;
        cl.target_component = target_componentId;
        cl.command = 21;
        cl.param7 = Float.parseFloat(altTextfield.getText());

        try {
            mavLinkByteArray = cl.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
    }

    /**
     * Supposed to list the mission list currently in the drone, but it is added to the WpProtocol which from the beginning was something just meant to work, so somewhere in the horrible code, there is something wrong with this command sequence.
     */
    private void list() {
        byte[] mavLinkByteArray = null;

        msg_mission_request mmr = new msg_mission_request(sysId, componentId);
        mmr.target_system = target_sysId;
        mmr.target_component = target_componentId;

        try {
            mavLinkByteArray = mmr.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        q.add(mavLinkByteArray);
        wpProtocol.notifyLock(-1,4);
    }

    /**
     * Reads the list of mission items created in the GUI and tells WpProtocol to send it. It seems like the first
     * mission item you send specifies the home position. In this case all 0's are used to take the current position
     * where it starts.
     *
     * See the code for Mavlink parameter specifics.
     */
    private void mission() {
        byte[] mavLinkByteArray = null;

        msg_mission_count mc = new msg_mission_count(sysId, componentId);
        mc.count = missionItems.size()+1;

        try {
            mavLinkByteArray = mc.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        q.add(mavLinkByteArray);

        msg_mission_item mi1 = new msg_mission_item(sysId, componentId);
        mi1.target_system = target_sysId;
        mi1.target_component = target_componentId;
        mi1.param1 = 0;
        mi1.param2 = 0;
        mi1.param3 = 0;
        mi1.param4 = 0;
        mi1.x = 0;
        mi1.y = 0;
        mi1.z = 0;
        mi1.seq = 0; // First in the mission sequence
        mi1.command = 16;
        mi1.frame = 0;
        mi1.current = 0;
        mi1.autocontinue = 1;

        System.out.println(mi1.toString());

        try {
            mavLinkByteArray = mi1.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        q.add(mavLinkByteArray);

        msg_mission_item mmi;
        int seq=1;
        for(MissionItem mi : missionItems) {
            mmi = new msg_mission_item(sysId, componentId);
            mmi.target_system = target_sysId;
            mmi.target_component = target_componentId;
            mmi.command = mi.getCommandId(); // Command ID (Lift, Fly to, Land)
            mmi.param1 = mi.getTime(); // Hold time in case of fly to, otherwise 0 and not used.
            mmi.param2 = 0; // Not used
            mmi.param3 = 0; // Not used
            mmi.param4 = 0; // Not used
            mmi.x = mi.getLatitude();  // The latitude, 0 to keep current
            mmi.y = mi.getLongitude(); // The longitud, 0 to keep current
            mmi.z = mi.getAltitude(); // The altitude, see frame parameter
            mmi.seq = seq; // Number in the sequence of mission items
            mmi.current = 0; // Do not start the mission here
            mmi.autocontinue = 1; // Automatically continue to the next item when this one is done
            mmi.frame = 3; // Global latitude and longitude, altitude relative to where it started which is set to 0

            //If it is a land command, do not autocontinue.
            if(mi.getCommandId() == 21) {
                mmi.autocontinue = 0;
            }

            System.out.println(mmi.toString());
            try {
                mavLinkByteArray = mmi.encode();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Add to the send queue and increment sequence number
            q.add(mavLinkByteArray);
            seq++;
        }

        // Start sending the sequence
        wpProtocol.notifyLock(-1,1);
    }

    /**
     * Tells the drone to send the status message stream req_stream_id at the rate req_message_rate. The Mavlink message
     * used is officially deprecated, but it is the only one supported in the firmware used.
     *
     * See the Mavlink documentation for different stream IDs. For example https://pixhawk.ethz.ch/mavlink/ .
     */
    private void status() {
        byte[] mavLinkByteArray = null;
        msg_request_data_stream mrds = new msg_request_data_stream(sysId, componentId);
        mrds.target_system = 1;
        mrds.target_component = 1;
        mrds.start_stop = 1;
        mrds.req_stream_id = 2;
        mrds.req_message_rate = 1;

        try {
            mavLinkByteArray = mrds.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
    }

    /**
     * Arms the drone if all the pre-arm checks are passed or they are disabled.
     */
    private void arm(int arm) {
        byte[] mavLinkByteArray = null;

        msg_command_long cl = new msg_command_long(sysId, componentId);
        cl.target_system = target_sysId;
        cl.target_component = target_componentId;
        cl.command = 400;
        cl.param1 = arm;
        cl.confirmation = 0;

        try {
            mavLinkByteArray = cl.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
    }

    /**
     * Changes the mode of the drone.
     */
    private void changeMode(int newMode) {
        byte[] mavLinkByteArray = null;

        msg_set_mode sm = new msg_set_mode(sysId, componentId);
        sm.target_system = target_sysId;
        sm.base_mode = 1;
        sm.custom_mode = newMode;

        try {
            mavLinkByteArray = sm.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
    }

    /**
     * Send a message to the phone telling it to turn the wifi on or off.
     */
    private void wifi(double status) {
        if(wifiStatus == status)
            return;

        wifiStatus=status;

        if(status == 0) {
            send(MessageType.WIFION, null);
        }
        else {
            send(MessageType.WIFIOFF, null);
        }
    }

    /**
     * Not tested, but it should change the mission item currently active to the specified number in the mission item list.
     */
    private void current() {
        byte[] mavLinkByteArray = null;

        msg_mission_set_current mmsc = new msg_mission_set_current(sysId, componentId);
        mmsc.target_system = target_sysId;
        mmsc.target_component = target_componentId;
        mmsc.seq = Integer.parseInt(seqTextfield.getText());

        try {
            mavLinkByteArray = mmsc.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        send(MessageType.MAVLINK, mavLinkByteArray);
    }

    /**
     * Instantiates the Message class and fills it with the byte array and message type supplied and sends it to the drone.
     * @param  messageType  The type of the message to be sent(either Mavlink to the drone or a command to the phone)
     * @param  mavLinkByteArray The Mavlink message byte array if messageType is Mavlink
     * @see    MessageType
     */
    private void send(MessageType messageType, byte[] mavLinkByteArray) {
        Message msg = new Message();
        msg.setByteArray(mavLinkByteArray);
        msg.setMessageType(messageType);

        try {
            synchronized(objectOut) {
                objectOut.writeObject(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}