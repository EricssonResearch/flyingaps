
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


public class EventHandling {
    int sysId = 255;
    int componentId = 190;
    int target_sysId = 1;
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

    private final int mode_RTL      = 6;
    private final int mode_POSHOLD  = 16;
    private final int mode_LAND     = 9;
    private final int mode_OF_LOITER= 10;
    private final int mode_STABILIZE= 0;
    private final int mode_AUTO     = 3;
    private final int mode_GUIDED   = 4;
    private final int mode_DRIFT    = 11;
    private final int mode_FLIP     = 14;
    private final int mode_AUTOTUNE = 15;
    private final int mode_ALT_HOLD = 2;
    private final int mode_LOITER   = 5;
    private final int mode_POSITION = 8;
    private final int mode_CIRCLE   = 7;
    private final int mode_SPORT    = 13;
    private final int mode_ACRO     = 1;

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
    private Button disarmButton;
    @FXML
    private Button statusButton;
    @FXML
    private TextField altTextfield;
    @FXML
    private TextField holdTextfield;
    @FXML
    private TextField seqTextfield;
    @FXML
    private Button missionButton;
    @FXML
    private Button autoButton;
    @FXML
    private Button clearButton;
    @FXML
    private Button currentButton;
    @FXML
    private Button stabilButton;
    @FXML
    private Button listButton;
    @FXML
    private Button guidedButton;
    @FXML
    private Button startButton;
    @FXML
    private Button stopButton;
    @FXML
    private TableColumn commandField;
    @FXML
    private TableColumn latField;
    @FXML
    private TableColumn longField;
    @FXML
    private TableColumn altField;
    @FXML
    private TableColumn holdField;
    @FXML
    private TableView missionTable;


    public EventHandling() {
        System.out.println("Gui created!");
    }

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

    public class JavaScriptCalls {
        public void updateLatAndLng(String lat, String lng) {
            latTextfield.setText(lat);
            lngTextfield.setText(lng);
        }
    }

    void setUpServer() {//Only allow one connection so far
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

        task.setOnSucceeded((workerStateEvent) -> {//When serverSocket.accept() is done, retrieve the handle to the client and create stream
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
        mi1.x = 0f;
        mi1.y = 0f;
        mi1.z = 0;
        mi1.seq = 0;
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
            mmi.command = mi.getCommandId();
            mmi.param1 = mi.getTime();
            mmi.param2 = 0;
            mmi.param3 = 0;
            mmi.param4 = 0;
            mmi.x = mi.getLatitude();
            mmi.y = mi.getLongitude();
            mmi.z = mi.getAltitude();
            mmi.seq = seq;
            mmi.current = 0;
            mmi.autocontinue = 1;
            mmi.frame = 3;

            if(mi.getCommandId() == 21) {
                mmi.autocontinue = 0;
            }

            System.out.println(mmi.toString());
            try {
                mavLinkByteArray = mmi.encode();
            } catch (IOException e) {
                e.printStackTrace();
            }

            q.add(mavLinkByteArray);
            seq++;
        }
        wpProtocol.notifyLock(-1,1);
    }

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

    private void current() {
        byte[] mavLinkByteArray = null;
        /*msg_command_long cl = new msg_command_long(sysId, componentId);
        cl.target_system = target_sysId;
        cl.target_component = target_componentId;
        cl.command = 300;
        cl.confirmation = 0;
        cl.param1=0;
        cl.param2=0;

        try {
            mavLinkByteArray = cl.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

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