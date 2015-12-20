package com.ericsson.captiveportal;

import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    Button action1Btn;
    Button action2Btn;
    Button action3Btn;
    TextView infoTxtVw;
    ScrollView infoScrollView;
    boolean wifiState = false;
    boolean captivePortalState = false;
    Button hotspotBtn;
    Thread webServerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        infoScrollView = (ScrollView) findViewById(R.id.infoScrlVw);
        infoTxtVw = (TextView) findViewById(R.id.infoTxtVw);
        action1Btn = (Button) findViewById(R.id.action1Btn);
        action2Btn = (Button) findViewById(R.id.action2Btn);
        action3Btn = (Button) findViewById(R.id.action3Btn);
        hotspotBtn = (Button) findViewById(R.id.hotspotBtn);

        action2Btn.setTextColor(Color.parseColor("blue"));
        hotspotBtn.setTextColor(Color.parseColor("blue"));
        action3Btn.setTextColor(Color.parseColor("blue"));

        action1Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                runCommand("iptables -L");
                runCommand("cat /proc/sys/net/ipv4/ip_forward");
            }
        });

        action2Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                captivePortalState ^= true;

                if (captivePortalState) {
                    enableCaptivePortalNetworking();
                    action2Btn.setTextColor(Color.parseColor("red"));
                    Toast.makeText(getApplicationContext(),"Captive Portal Enabled", Toast.LENGTH_LONG).show();
                } else {
                    disableCaptivePortalNetworking();
                    action2Btn.setTextColor(Color.parseColor("blue"));
                    Toast.makeText(getApplicationContext(),"Captive Portal Disabled", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(),"Wifi Enabled", Toast.LENGTH_LONG).show();
                } else {
                    disableTethering();
                    hotspotBtn.setTextColor(Color.parseColor("blue"));
                    Toast.makeText(getApplicationContext(),"Wifi Disabled", Toast.LENGTH_LONG).show();
                }
            }
        });

        action3Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                webServerTask = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        HelloServer server = new HelloServer();
                        server.run();
                    }
                });
                webServerTask.start();
                Toast.makeText(getApplicationContext(),"WebServer Enabled", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void allowUser(String mac) {
        runCommand("iptables -t mangle -I 2 portal -m mac --mac-source "+mac+" -j RETURN");
    }

    private void blockUser(String mac) {
        runCommand("iptables -t mangle -D portal -m mac --mac-source "+mac+" -j RETURN");
    }

    String host = "192.168.43.1";

    private void enableCaptivePortalNetworking() {
        String commands[] =
                {
                        "echo \"1\" > /proc/sys/net/ipv4/ip_forward",
                        "iptables -N portal -t mangle;",

                        "iptables -t mangle -I portal -j MARK --set-mark 37;",
                        //
                        "iptables -t mangle -I PREROUTING -j portal;",

                        "iptables -t nat -I PREROUTING -m mark --mark 37 -p tcp --dport 80 -j DNAT --to-destination "+host+";",

                        //commands += "iptables -t filter -I INPUT -m mark --mark 37 -j DROP;",
                        "iptables -t filter -I INPUT -p udp --dport 53 -j ACCEPT;",
                        "iptables -t filter -I INPUT -p tcp --dport 80 -j ACCEPT;",
                        "iptables -t filter -I FORWARD -m mark --mark 37 -j DROP;",
                        //
                        "iptables -I FORWARD -i wlan0 -o p2p0 -j ACCEPT;",
                        "iptables -I FORWARD -i p2p0 -o wlan0 -m state --state ESTABLISHED,RELATED -j ACCEPT;",

                        "iptables -t nat -I POSTROUTING -o p2p0 -j MASQUERADE;"
                };
        /*for(String cmd : commands) {
            runCommand(cmd);
        }*/
        runCommand("source /storage/sdcard0/start_portal_rules.sh");

    }

    private void disableCaptivePortalNetworking() {
        String commands[] = {
                "echo \"0\" > /proc/sys/net/ipv4/ip_forward",
                "iptables -N portal -t mangle;",

                "iptables -t mangle -D portal -j MARK --set-mark 37;",

                "iptables -t mangle -D PREROUTING -j portal;",

                "iptables -t nat -I PREROUTING -m mark --mark 37 -p tcp --dport 80 -j DNAT --to-destination " + host + ";",

                //;commands += "iptables -t filter -D INPUT -m mark --mark 37 -j DROP;";
                "iptables -t filter -D INPUT -p udp --dport 53 -j ACCEPT;",
                "iptables -t filter -D INPUT -p tcp --dport 80 -j ACCEPT;",
                "iptables -t filter -D FORWARD -m mark --mark 37 -j DROP;",

                "iptables -D FORWARD -i wlan0 -o p2p0 -j ACCEPT;",
                "iptables -D FORWARD -i p2p0 -o wlan0 -m state --state ESTABLISHED,RELATED -j ACCEPT;",

                "iptables -t nat -D POSTROUTING -o p2p0 -j MASQUERADE;"
        };
        /*for(String cmd : commands) {
            runCommand(cmd);
        }*/
        runCommand("source /storage/sdcard0/stop_portal_rules.sh");
    }

    private void runCommand(String cmd) {
        runCommand(new String[]{cmd});
    }

    private void runCommand(String[] cmds) {
        String END_OF_COMMAND = "END_OF_COMMAND";
        String line;
        String output = "";
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            InputStream is = process.getInputStream();
            DataInputStream osRes = new DataInputStream(is);
            UiLog("Commands: "+cmds.length);
            for (String cmd :  cmds ) {
                UiLog(cmd + ";echo " + END_OF_COMMAND + "\n");
                os.writeBytes(cmd + ";echo " + END_OF_COMMAND + "\n");
                os.flush();

                while (true) {
                    line = osRes.readLine();
                    if(null != line) {
                        if (line.contains(END_OF_COMMAND)) {
                            break;
                        } else {
                            output += line;
                        }
                    } else {
                        break;
                    }

                }
            }
            if(!(output.trim().isEmpty())) {
                UiLog(output);
            }
            os.writeBytes("exit\n");
            os.flush();
        } catch(Exception e) {
            e.printStackTrace();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            UiLog(sw.toString());
        }
    }

    private void enableTethering() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);

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


    private void UiLog(String message) {
        Message msg;
        Bundle data;
        msg = new Message();
        data = new Bundle();
        msg.setData(data);
        data.putString("msg", message);
        mainHandler.sendMessage(msg);
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
            boolean scrollingEnabled = true;
            if (scrollingEnabled) {
                infoScrollView.post(new Runnable() {
                    public void run() {
                        infoScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        }

    };

}
