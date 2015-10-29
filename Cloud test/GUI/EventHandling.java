import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class EventHandling {

    private Socket clientSocket;
    private int port = 12345;
    private WebEngine engine;

    @FXML
    private Button missionButton;
    @FXML
    private WebView webView;
    @FXML
    private TextField latTextfield;
    @FXML
    private TextField lngTextfield;

    public EventHandling() {
        System.out.println("Gui created!");
    }

    @FXML
    private void initialize() {
        System.out.println("Init!");
        setUpServer();

        missionButton.setOnAction((event) -> {
            PrintWriter out;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("Teeest!");
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("Button pressed!");
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

    private void setUpServer() {
        System.out.println("setup!");
        Task<Socket> task = new Task<Socket>() {
            @Override
            protected Socket call() {
                ServerSocket serverSocket = null;

                try {
                    serverSocket = new ServerSocket(port);
                    clientSocket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Connection accepted!");
                Server serverTask = new Server(12346, EventHandling.this, clientSocket);
                new Thread(serverTask).start();
                return clientSocket;
            }
        };
        new Thread(task).start();

        task.setOnSucceeded((workerStateEvent) -> {
            clientSocket = task.getValue();
        });
    }
}