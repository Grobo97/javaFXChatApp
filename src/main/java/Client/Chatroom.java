package Client;

import com.google.gson.Gson;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class Chatroom  {
    public static Thread th;
    public TextArea usersList;
    public Button btnSend;
    public TextArea chatArea;
    public TextField messageToSend;
    public static DatagramSocket datagramSocket;
    byte[] buf = new byte[1024];
    InetAddress ip;
    Gson gson = new Gson();


    public Chatroom() throws IndexOutOfBoundsException {
        try {
            ip = InetAddress.getByName(ConnectionData.server);
            th = new Thread(() -> {
                try {

                    while(true) {
                        try {

                            DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                            datagramSocket.receive(datagramPacket);
                            String origMessage = new String(datagramPacket.getData(),0,datagramPacket.getLength());
                            Message newMessage = gson.fromJson(origMessage,Message.class);
                            if (newMessage.messageType.equals(MessageType.USERS_LIST)){
                                usersList.setText("");
                                System.out.println("Updating UsersList");
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        String[] newUsersList = newMessage.message.split(",");
                                        for(String s : newUsersList){
                                            if(s.isEmpty()) continue;
                                            usersList.setText(usersList.getText()+"\n" + s);
                                        }
                                    }
                                });
                            }else {

                                if (newMessage.name.equals(ConnectionData.userName)){
                                    newMessage.name= "Ty";
                                }
                                String finalMessage = newMessage.name + ": " + newMessage.message;
                                System.out.println("Adding Message to the board");
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        chatArea.appendText(finalMessage + "\n");
                                        System.out.println(finalMessage);

                                    }
                                });
                            }
                        }catch (Exception e){
                            System.out.println("Tu się zepsuło");
                            System.out.println(e);

                        }
                    }
                } catch(Exception E) {
                    System.out.println("Błąd" + E.getMessage());
                }
            });
            th.start();
        } catch(IOException E) {
            E.printStackTrace();
        }
    }


    public void onClickSend() {
        try {
//            DatagramSocket datagramSocket = new DatagramSocket();

            Message message = Message.builder()
                    .name(ConnectionData.userName)
                    .message(messageToSend.getText())
                    .messageType(MessageType.MESSAGE)
                    .build();
            String jsonMessage = gson.toJson(message);
            byte[] buffer = jsonMessage.getBytes(StandardCharsets.UTF_8);
            DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length, ip, ConnectionData.port );
            datagramSocket.send(datagramPacket);

            messageToSend.setText("");


        } catch(IOException E) {
            E.printStackTrace();
        }

    }

    public void buttonPressed(KeyEvent e) {
        if(e.getCode().equals(KeyCode.ENTER))
        {
            onClickSend();
        }
    }
}
