package Client;

import com.google.gson.Gson;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class Controller {
    public Button joinBtn;
    public TextField username;
    public TextField port;
    public TextField server;
    public int PORT;
    Gson gson = new Gson();

    public void enterChat(MouseEvent mouseEvent) throws IOException {
        ConnectionData.server = server.getText();
        this.PORT = Integer.parseInt(port.getText());
        ConnectionData.userName = username.getText();
        ConnectionData.port = PORT;
        InetAddress ip = InetAddress.getByName(ConnectionData.server);
        DatagramSocket datagramSocket = new DatagramSocket();
        Chatroom.datagramSocket=datagramSocket;
        Message message = Message.builder()
                .name(ConnectionData.userName)
                .message("")
                .messageType(MessageType.NEW_USER)
                .build();
        String jsonMessage = gson.toJson(message);
        byte[] buffer = jsonMessage.getBytes(StandardCharsets.UTF_8);
        DatagramPacket datagramPacket = new DatagramPacket(buffer,buffer.length, ip, ConnectionData.port );
        datagramSocket.send(datagramPacket);
        datagramPacket = new DatagramPacket(new byte[1024], new byte[1024].length);
        datagramSocket.receive(datagramPacket);
        String messageFromServer = new String(datagramPacket.getData(), 0, datagramPacket.getLength(), StandardCharsets.UTF_8);
        Message newMessage = gson.fromJson(messageFromServer, Message.class);
        if (newMessage.messageType.equals(MessageType.USER_EXISTS)){
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Błąd logowania");
            a.setContentText("Nazwa użytkownika jest już zajęta.");
            a.show();
            username.setText("");
            return;
        }
        Stage stage;
        stage = (Stage) server.getScene().getWindow();
        //Parent parent = FXMLLoader.load(getClass().getResource("room.fxml"));
        Parent root = FXMLLoader.load(Controller.class.getClassLoader().getResource("chatroom.fxml"));
        stage.setScene(new Scene(root, 800, 472));
        stage.setTitle(ConnectionData.userName);

        stage.setOnCloseRequest(e-> {
            //e.consume();
            Message messageOnExit = Message.builder()
                    .name(ConnectionData.userName)
                    .message("")
                    .messageType(MessageType.USER_DISCONNECTED)
                    .build();
            String jsonMessage2 = gson.toJson(messageOnExit);
            byte[] buffer2 = jsonMessage2.getBytes(StandardCharsets.UTF_8);
            DatagramPacket datagramPacket2 = new DatagramPacket(buffer2,buffer2.length, ip, ConnectionData.port );
            try {
                datagramSocket.send(datagramPacket2);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            Chatroom.th.interrupt();
            System.exit(0);
        });
        stage.setResizable(false);

        stage.show();

    }
}
