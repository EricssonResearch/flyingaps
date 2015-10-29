import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server{
    private int port;

    public Server(int port){
        try(
                ServerSocket serverSocket = new ServerSocket(port);
                Socket clientSocket = serverSocket.accept();
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        ){
            String command;
            System.out.println("Drone connected!");

            while ((command = in.readLine()) != null) {
                    out.println(command);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        if(args.length != 1) {
            System.err.println("Usage 'java Server port-number'");
        }
        else {
            new Server(Integer.parseInt(args[0]));
        }
    }
}