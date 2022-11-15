package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

public class Client {
    public String username;
    public Socket clientSocket;
    public boolean isOnline;
    public DataInputStream dis;
    public DataOutputStream dos;

    public Client(String username, Socket clientSocket, boolean isOnline, DataInputStream dis, DataOutputStream dos) {
        this.username = username;
        this.clientSocket = clientSocket;
        this.isOnline = isOnline;
        this.dis = dis;
        this.dos = dos;
        new Thread(() -> {
            try {
                while(true) {
                    String message = dis.readUTF();
                    System.out.println(message);

                    List<Client> entry = Server.clients;
                    for (Client cli : entry) {
                        DataOutputStream edos = cli.getDos();
                        edos.writeUTF(message);
                    }
                }
            } catch (IOException E) {
                try {
                    dis.close();
                    dos.close();
                    Server.clients = Server.clients.stream()
                            .filter(e -> {
                                if(!(e == this)) {
                                    String exit_message = username + " Disconnected \n";
                                    System.out.println(exit_message);
                                    try {
                                        e.getDos().writeUTF(exit_message);
                                    } catch (IOException err) {
                                        err.printStackTrace();
                                    }
                                }
                                return !(e == this);
                            })
                            .collect(Collectors.toList());
                    Server.activeClients.remove(username);



                } catch(IOException E2) {
                    E2.printStackTrace();
                }
            }
        }).start();
    }

    public DataOutputStream getDos() {
        return this.dos;
    }
}
