package Server;

import javafx.scene.Parent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Server {
    public static List<Client> clients;
    public static Set<String> activeClients;
    public static DataOutputStream dos;
    public static DataInputStream dis;
    static final int PORT = 9999;

    public Server() throws IOException {
        System.out.println("Server");
        ServerSocket serverSocket = new ServerSocket(PORT);
        clients = new ArrayList<>();
        activeClients = new HashSet<>();
        Thread th = new Thread(() -> {
            while(true){
                try {
                    TimeUnit.SECONDS.sleep(2);
                }catch (InterruptedException ignored){

                }
                System.out.println("Checking active list");
                if (activeClients.size() != 0){
                    System.out.println("Sending active users List");
                    for (Client c : clients){
                        try {
                            c.getDos().writeUTF("ACTIVEUSERLIST: " + activeClients);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        th.start();
        Thread th2 = new Thread(() -> {
            while (true) {
                try {


                    Socket socket = serverSocket.accept();
                    System.out.println("Client connected");
                    dos = new DataOutputStream(socket.getOutputStream());
                    dis = new DataInputStream(socket.getInputStream());
                    String userName = dis.readUTF();
                    Client client = new Client(userName, socket, true, dis, dos);
                    clients.add(client);

                    System.out.println("Server: " + userName + " joined the chat");
                    for (Client c : clients) {
                        c.getDos().writeUTF("Server: " + userName + " joined the chat");
                        if (c.isOnline){
                            activeClients.add(userName);
                        }
                    }
                }catch (Exception e) {
                    System.out.println("Error");
                }
            }
        });
        th2.start();
    }



}
