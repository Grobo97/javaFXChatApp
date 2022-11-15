package Client;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Chatroom  {
    public static Thread th;
    public TextArea usersList;
    public Button btnSend;
    public TextArea chatArea;
    public TextField messageToSend;
    Socket sock;
    DataOutputStream dos;
    DataInputStream dis;

    public Chatroom() throws IndexOutOfBoundsException {
        try {

            sock = new Socket(ConnectionData.server, ConnectionData.port);
            dos = new DataOutputStream(sock.getOutputStream());
            dis = new DataInputStream(sock.getInputStream());
            dos.writeUTF(ConnectionData.userName);

            th = new Thread(() -> {
                try {

                    while(true) {
                        try {
                            String message = dis.readUTF();
                            if (message.startsWith("ACTIVEUSERLIST")){
                                usersList.setText("");
                                String[] encodedMsg = message.split(" ");
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 1; i < encodedMsg.length; i++){
                                            String activeUser = encodedMsg[i].replaceAll("[\\[,\\]]","");
                                            System.out.println(activeUser);

                                            usersList.appendText(activeUser + "\n");
                                        }
                                    }
                                });

//                                usersList.getItems().removeAll();
//                                usersList.getItems().addAll(encodedMsg[1]);
                            }else {

                                if (message.startsWith(ConnectionData.userName)){
                                    message = message.replace(ConnectionData.userName+":", "Ty:");
                                }
                                String finalMessage = message;
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
                    try{
                        System.out.println("Błąd" + E.getMessage());
                        dis.close();
                        dos.close();

                    } catch (IOException e) {
                        System.out.println(e);
                    }
                }

            });

            th.start();

        } catch(IOException E) {
            E.printStackTrace();
        }

    }



    public void onClickSend() {
        try {
            String message = ConnectionData.userName + ": " + messageToSend.getText();
            dos.writeUTF(message);
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
