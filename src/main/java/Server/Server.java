package Server;


import Client.Message;
import Client.MessageType;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;

public class Server {
    public static List<Client> clients;
    public static Set<String> activeClients;
    static final int PORT = 9999;
    static final byte[] BUFFER = new byte[1024];
    public DatagramSocket datagramSocket;
    public Gson gson = new Gson();
    public Server() throws IOException, InterruptedException {
        System.out.println("Server");

        datagramSocket = new DatagramSocket(PORT);
        clients = new ArrayList<>();


        while (true) {

            DatagramPacket datagramPacket = new DatagramPacket(BUFFER, BUFFER.length);
            datagramSocket.receive(datagramPacket);
            String message = new String(datagramPacket.getData(), 0, datagramPacket.getLength(), StandardCharsets.UTF_8);
            Message newMessage = gson.fromJson(message, Message.class);

            switch (newMessage.messageType){
                case MESSAGE:
                    sendMessageToAllClients(newMessage);

                    System.out.println(newMessage.name + " " + newMessage.message);
                    break;
                case NEW_USER:
                    if (clients.stream().filter(Objects::nonNull).filter(c -> c.username.equals(newMessage.name)).findFirst().orElse(null) == null){
                        System.out.println("okay");
                        clients.add(Client.builder()
                                .username(newMessage.name)
                                .clientAddress(datagramPacket.getAddress())
                                .clientPort(datagramPacket.getPort())
                                .build()
                        );



                        sendMessageToAllClients(Message.builder()
                                .name("Server")
                                .message(newMessage.name + " joined the server!")
                                .messageType(MessageType.MESSAGE)
                                .build()
                        );

                    }else {
                        Message messageFromServer = Message.builder()
                                .name("Server")
                                .message("")
                                .messageType(MessageType.USER_EXISTS)
                                .build();
                        String jsonMessage = gson.toJson(messageFromServer);
                        byte[] buffer = jsonMessage.getBytes(StandardCharsets.UTF_8);
                        DatagramPacket datagramPacket2 = new DatagramPacket(buffer,buffer.length,datagramPacket.getAddress() ,datagramPacket.getPort() );
                        datagramSocket.send(datagramPacket2);
                        System.out.println("Not okay");
                    }
                    break;
                case USER_DISCONNECTED:
                    clients.removeIf(c -> c.username.equals(newMessage.name));

                    sendMessageToAllClients(Message.builder()
                            .name("Server")
                            .message(newMessage.name + " left the server!")
                            .messageType(MessageType.MESSAGE)
                            .build()
                    );
            };
        }


    }

    private void sendMessageToAllClients(Message message) throws IOException, InterruptedException {
        String newMessage = gson.toJson(message);
        byte[] buffer = newMessage.getBytes(StandardCharsets.UTF_8);

        for (Client c : clients){
            System.out.println("Sending message 1 to: " + c.username +" " + c.clientAddress.toString() + ":" + c.clientPort);
            DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length,c.clientAddress ,c.clientPort );
            datagramSocket.send(datagramPacket);
        }
        TimeUnit.SECONDS.sleep(1);
        generateUsersListAndSendToClients();
    }

    public void generateUsersListAndSendToClients() throws IOException {
        String newUsersList = "";
        for (Client c : clients){
            newUsersList = newUsersList + c.username + ",";
        }
        Message messageFromServer = Message.builder()
                .name("Server")
                .message(newUsersList)
                .messageType(MessageType.USERS_LIST)
                .build();
        String jsonMessage = gson.toJson(messageFromServer);
        byte[] buffer = jsonMessage.getBytes(StandardCharsets.UTF_8);
        for (Client c : clients){
            System.out.println("Sending message 3 to: " + c.username +" " + c.clientAddress.toString() + ":" + c.clientPort);
            DatagramPacket datagramPacket2 = new DatagramPacket(buffer,buffer.length,c.clientAddress ,c.clientPort );
            datagramSocket.send(datagramPacket2);
        }
    }
}
