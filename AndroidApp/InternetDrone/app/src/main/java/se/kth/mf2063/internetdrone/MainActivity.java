package se.kth.mf2063.internetdrone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import org.mavlink.messages.MAV_AUTOPILOT;
import org.mavlink.messages.MAV_MODE_FLAG;
import org.mavlink.messages.MAV_STATE;
import org.mavlink.messages.MAV_TYPE;
import org.mavlink.messages.ja4rtor.msg_heartbeat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class MainActivity extends AppCompatActivity {

    Button toggleScrollBtn;
    Button commBtn;
    TextView infoTxtVw;
    ScrollView infoScrollView;
    Button hotspotBtn;

    boolean wifiState = false;
    private boolean scrollingEnabled = true;

    private volatile boolean stop = false;
    private volatile boolean isCloudConnected = false;

    D2xxManager d2xxManager = null;
    FT_Device ft_device = null;

    private final Object usbLock = new Object();
    private Object cloudConnection = new Object();
    private Object taskTermination = new Object();

    BlockingQueue mailbox = new LinkedBlockingQueue(50);

    private static final String ACTION_USB_PERMISSION = "se.kth.mf2063.internetdrone.USB_PERMISSION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toggleScrollBtn = (Button) findViewById(R.id.toggleScrollBtn);
        commBtn = (Button) findViewById(R.id.commBtn);
        hotspotBtn = (Button) findViewById(R.id.hotspotBtn);
        infoScrollView = (ScrollView) findViewById(R.id.info_sv);
        infoTxtVw = (TextView) findViewById(R.id.infoTxtVw);

        toggleScrollBtn.setTextColor(Color.parseColor("red"));
        hotspotBtn.setTextColor(Color.parseColor("blue"));
        commBtn.setTextColor(Color.parseColor("blue"));

        toggleScrollBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                scrollingEnabled ^= true;
                if (scrollingEnabled) {
                    toggleScrollBtn.setTextColor(Color.parseColor("red"));
                } else {
                    toggleScrollBtn.setTextColor(Color.parseColor("blue"));
                }
            }
        });

        commBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (connectSerialUsb()) {

                    ft_device.purge(D2xxManager.FT_PURGE_TX);
                    ft_device.restartInTask();

                    stop = false;

                    startDroneCommunicationTasks();
                    startCloudCommunicationTask();

                }
            }
        });

        hotspotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                wifiState ^= true;

                if (wifiState) {
                    enableTethering();
                    hotspotBtn.setTextColor(Color.parseColor("red"));
                } else {
                    disableTethering();
                    hotspotBtn.setTextColor(Color.parseColor("blue"));
                }
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.setPriority(500);
        this.registerReceiver(mUsbReceiver, filter);
    }

    private boolean connectSerialUsb() {

        D2xxManager.DriverParameters driverParameters = new D2xxManager.DriverParameters();
        Log.d("D2xxManager", "MaxBufferSize= " + driverParameters.getMaxBufferSize());
        Log.d("D2xxManager", "MaxTransferSize= " + driverParameters.getMaxTransferSize());
        Log.d("D2xxManager", "ReadTimeout= " + driverParameters.getReadTimeout());

        try {
            d2xxManager = D2xxManager.getInstance(this);
        } catch (D2xxManager.D2xxException ex) {
            infoTxtVw.setText(infoTxtVw.getText() + "\n" + "Could not get d2xxManager");
            return false;
        }

        int devCount = 0;

        devCount = d2xxManager.createDeviceInfoList(this);

        if (devCount > 0) {
            D2xxManager.FtDeviceInfoListNode[] deviceList = new D2xxManager.FtDeviceInfoListNode[devCount];
            d2xxManager.getDeviceInfoList(devCount, deviceList);

            // deviceList[0] = ftdid2xx.getDeviceInfoListDetail(0);

            infoTxtVw.setText(infoTxtVw.getText() + "\n" +
                    "Number of Devices: " + Integer.toString(devCount));


            if (null != deviceList[0].serialNumber) {
                infoTxtVw.setText(infoTxtVw.getText() + "\n" +
                        "Device SerialNumber: " + deviceList[0].serialNumber);
            }
        }

        if (devCount > 0) {
            synchronized (usbLock) {
                ft_device = d2xxManager.openByIndex(this, 0, driverParameters);
            }
        }

        synchronized (usbLock) {
            if ((null != ft_device) && (ft_device.isOpen())) {
                // Don't know
                ft_device.setBitMode((byte) 0, D2xxManager.FT_BITMODE_RESET);
                // Baud Rate 115200
                ft_device.setBaudRate(115200);
                // 8N1
                ft_device.setDataCharacteristics(D2xxManager.FT_DATA_BITS_8, D2xxManager.FT_STOP_BITS_1, D2xxManager.FT_PARITY_NONE);
                // No Flow Control
                ft_device.setFlowControl(D2xxManager.FT_FLOW_NONE, (byte) 0x00, (byte) 0x00);
                // Disable DTR / RTS
                ft_device.clrDtr();
                ft_device.clrRts();

                return true;

            } else {
                return false;
            }
        }
    }

    private void enableTethering() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int ws = wifiManager.getWifiState();

        Method[] methods = wifiManager.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals("setWifiApEnabled")) {
                try {
                    method.invoke(wifiManager, null, true);
                } catch (Exception ex) {
                }
                break;
            }
        }
    }

    private void disableTethering() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        int ws = wifiManager.getWifiState();

        Method[] methods = wifiManager.getClass().getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals("setWifiApEnabled")) {
                try {
                    method.invoke(wifiManager, null, false);
                } catch (Exception ex) {
                }
                break;
            }
        }
    }

    private void startCloudCommunicationTask() {
        /* TODO: Change the following ip to a convenient one or to a domain name */
        CloudCommunicationTask cloudCommunicationTask = new CloudCommunicationTask("130.229.152.16", 12345);
        cloudCommunicationTask.start();
    }

    private void startDroneCommunicationTasks() {
        stop = false;
        DroneRxTask droneRxTask = new DroneRxTask(mainHandler);
        droneRxTask.start();
        DroneTxTask droneTxTask = new DroneTxTask(mainHandler);
        droneTxTask.start();
    }

    private Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String rxData = msg.getData().getString("msg");

            if (infoTxtVw.getLineCount() >= (infoTxtVw.getMaxLines() - 1)) {
                infoTxtVw.setText(rxData);
            } else {
                infoTxtVw.setText(infoTxtVw.getText() + "\n" + rxData);
            }

            /* Scroll the information text view to the end */
            if (scrollingEnabled) {
                infoScrollView.post(new Runnable() {
                    public void run() {
                        infoScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }

            Log.d("Handler", rxData);
        }

    };

    /*
    *
    * Cloud Communication Main Task
    *
    */
    class CloudCommunicationTask extends Thread {
        Socket cloudClient;
        String host;
        int port;

        CloudCommunicationTask(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            ObjectOutputStream objectOutputStream = null;
            ObjectInputStream objectInputStream = null;

            while (true != stop) {

                /* Connecting Phase */
                do {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {

                    }

                    try {
                        cloudClient = new Socket(host, port);
                        if (null != cloudClient) {
                            isCloudConnected = true;
                        }
                    } catch (IOException e) {
                        isCloudConnected = false;
                    }
                } while ((true != stop) && (true != isCloudConnected));

                try {
                    objectOutputStream = new ObjectOutputStream(cloudClient.getOutputStream());
                } catch (IOException e) {
                    isCloudConnected = false;
                }

                try {
                    objectInputStream = new ObjectInputStream(cloudClient.getInputStream());
                } catch (IOException e) {
                    isCloudConnected = false;
                }

                CloudTxTask cloudTxTask = null;
                CloudRxTask cloudRxTask = null;

                if ((true == isCloudConnected) && (null != cloudClient)) {
                    cloudTxTask = new CloudTxTask(mainHandler, mailbox, objectOutputStream);
                    cloudRxTask = new CloudRxTask(mainHandler, objectInputStream);
                    cloudTxTask.start();
                    cloudRxTask.start();
                } else {
                    continue;
                }

                synchronized (taskTermination) {
                    try {
                        taskTermination.wait();
                    } catch (InterruptedException e) {
                    }
                }

                cloudTxTask.interrupt();
                cloudRxTask.interrupt();

                synchronized (taskTermination) {
                    try {
                        taskTermination.wait();
                    } catch (InterruptedException e) {
                    }
                }

                try {
                    cloudClient.close();
                } catch (IOException e) {
                }

                isCloudConnected = false;

                synchronized (cloudConnection) {
                    cloudConnection.notifyAll();
                }
            }
        }
    }

    /*
    *
    * Cloud Communication Transmission Task
    *
    */
    class CloudTxTask extends Thread {
        private final Handler handler;
        private final BlockingQueue mailbox;
        private final ObjectOutputStream objectOutputStream;

        CloudTxTask(Handler handler, BlockingQueue mailbox,
                    ObjectOutputStream objectOutputStream) {
            this.handler = handler;
            this.setPriority(Thread.MIN_PRIORITY);
            this.mailbox = mailbox;
            this.objectOutputStream = objectOutputStream;
        }

        @Override
        public void run() {
            byte[] mavlinkBuffer = null;
            se.kth.mf2063.internetdrone.Message cloudMessage = new se.kth.mf2063.internetdrone.Message();

            while (true == isCloudConnected) {
                try {
                    mavlinkBuffer = (byte[]) mailbox.take();
                    cloudMessage.setMessageType(MessageType.MAVLINK);
                    cloudMessage.setByteArray(mavlinkBuffer);
                    objectOutputStream.writeObject(cloudMessage);
                } catch (InterruptedException e) {

                } catch (IOException e) {
                    isCloudConnected = false;
                }
            }

            synchronized (taskTermination) {
                taskTermination.notify();
            }

            synchronized (cloudConnection) {
                try {
                    cloudConnection.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /*
    *
    * Cloud Communication Reception Task
    *
    */
    class CloudRxTask extends Thread {
        private final Handler handler;
        private final ObjectInputStream objectInputStream;

        CloudRxTask(Handler h, ObjectInputStream objectInputStream) {
            handler = h;
            this.setPriority(Thread.NORM_PRIORITY);
            this.objectInputStream = objectInputStream;
        }

        @Override
        public void run() {
            Socket cloudClient = null; // connect to the server
            Object message = null;
            se.kth.mf2063.internetdrone.Message cloudMessage = null;

            while (true == isCloudConnected) {

                try {
                    message = objectInputStream.readObject();
                } catch (ClassNotFoundException e) {
                    continue;
                } catch (IOException e) {
                    isCloudConnected = false;
                    continue;
                }

                if ((null != message) && (message instanceof se.kth.mf2063.internetdrone.Message)) {
                    cloudMessage = (se.kth.mf2063.internetdrone.Message) message;

                    switch (cloudMessage.getMessageType()) {
                        case MAVLINK:
                        /* send message to Drone */
                            synchronized (usbLock) {
                                if ((null != ft_device) && (ft_device.isOpen())) {
                                    ft_device.write(cloudMessage.getByteArray(), cloudMessage.getByteArray().length);
                                    ft_device.purge(D2xxManager.FT_PURGE_TX);
                                } else {
                                /* Drone is not connected */
                                /* drop message for now */
                                }
                            }
                            break;
                        case WIFIOFF:
                            disableTethering();
                            break;
                        case WIFION:
                            enableTethering();
                            break;
                        default:
                            break;
                    }
                }
            }

            synchronized (taskTermination) {
                taskTermination.notify();
            }

            synchronized (cloudConnection) {
                try {
                    cloudConnection.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /*
    *
    * Drone PixHawk Communication Transmission Task
    *
    */
    class DroneTxTask extends Thread {
        private final Handler handler;

        DroneTxTask(Handler h) {
            handler = h;
            this.setPriority(Thread.NORM_PRIORITY);
        }

        @Override
        public void run() {

            int bytesWritten = 0;

            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("msg", "Tx Thread Started");
            msg.setData(data);
            handler.sendMessage(msg);

            /* Transmitter system id and component id */
            /* 100 has been picked up for the android app */
            int sysId = 100;
            int componentId = 100;
            byte[] heartBeatMessageBytes = null;

            /* HeartBeat parameters */
            msg_heartbeat heartBeatMessage = new msg_heartbeat(sysId, componentId);
            heartBeatMessage.type = MAV_TYPE.MAV_TYPE_QUADROTOR;
            heartBeatMessage.autopilot = MAV_AUTOPILOT.MAV_AUTOPILOT_ARDUPILOTMEGA;
            heartBeatMessage.base_mode = MAV_MODE_FLAG.MAV_MODE_FLAG_STABILIZE_ENABLED;
            heartBeatMessage.system_status = MAV_STATE.MAV_STATE_STANDBY;
            heartBeatMessage.mavlink_version = 3;

            while (true != stop) {

                bytesWritten = 0;

                try {
                    Thread.sleep(900);
                } catch (InterruptedException e) {

                }

                /*  Sequence wrap around 255  */
                heartBeatMessage.sequence++;
                heartBeatMessage.sequence = heartBeatMessage.sequence % 255;

                try {
                    heartBeatMessageBytes = heartBeatMessage.encode();
                } catch (IOException e) {
                    continue;
                }

                synchronized (usbLock) {
                    if ((null != ft_device) && (ft_device.isOpen())) {
                        bytesWritten = ft_device.write(heartBeatMessageBytes, heartBeatMessageBytes.length);
                        ft_device.purge(D2xxManager.FT_PURGE_TX);
                    }
                }

                if (bytesWritten > 0) {
                    msg = new Message();
                    data = new Bundle();
                    data.putString("msg", /*System.nanoTime()+*/"Tx HB #" + heartBeatMessage.sequence + "");
                    msg.setData(data);
                    handler.sendMessage(msg);
                }
            }
        }
    }

    /*
    *
    * Drone PixHawk Communication Reception Task
    *
    */
    class DroneRxTask extends Thread {

        private final Handler handler;

        DroneRxTask(Handler h) {
            handler = h;
            this.setPriority(Thread.MIN_PRIORITY);
        }

        @Override
        public void run() {
            byte[] rxBuffer = null;

            Message msg = new Message();
            Bundle data = new Bundle();
            int numberOfWaitingBytes = 0;
            int numberOfRxBytes = 0;

            data.putString("msg", "Rx Thread Started");
            msg.setData(data);
            handler.sendMessage(msg);


            while (true != stop) {

                numberOfRxBytes = 0;

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {

                }

                synchronized (usbLock) {

                    if ((null != ft_device) && (ft_device.isOpen())) {
                        numberOfWaitingBytes = ft_device.getQueueStatus();
                        if (numberOfWaitingBytes > 0) {
                            rxBuffer = new byte[numberOfWaitingBytes];
                            numberOfRxBytes = ft_device.read(rxBuffer, numberOfWaitingBytes);
                        }
                    } else {
                        msg = new Message();
                        data = new Bundle();
                        data.putString("msg", "Not connected");
                        msg.setData(data);
                        handler.sendMessage(msg);
                    }
                }

                if (numberOfRxBytes > 0) {
                    // Mavlink Decode
                    String mavlinkMessageInfo = DroneCommunication.mavlink_decode(rxBuffer);
                    msg = new Message();
                    data = new Bundle();
                    data.putString("msg", mavlinkMessageInfo);
                    msg.setData(data);
                    handler.sendMessage(msg);
                }
            }
        }
    }

    /* Buffer bytes in hexadecimal */
    String bufferToHexString(byte[] buffer) {
        StringBuilder sb = new StringBuilder();
        sb.append("numberOfRxBytes: " + buffer.length + "\n");
        for (byte b : buffer) {
            sb.append(String.format("%02X ", b));
        }
        sb.append("\n");
        return sb.toString();
    }

    /* USB disconnect broadcast receiver */
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                /* Start Drone Tasks automatically */
                if (connectSerialUsb()) {

                    ft_device.purge(D2xxManager.FT_PURGE_TX);
                    ft_device.restartInTask();

                    stop = false;

                    startDroneCommunicationTasks();
                    startCloudCommunicationTask();
                }
            }

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (usbLock) {
                    if (ft_device != null) {
                        ft_device.close();
                        ft_device = null;
                        /* handle cloud termination properly */
                        isCloudConnected = false;
                        /* stop all tasks */
                        stop = true;
                    }
                }
            }
        }
    };
}
