package Client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class Controller {
    public Button joinBtn;
    public TextField username;
    public TextField port;
    public TextField server;
    public int PORT;

    public void enterChat(MouseEvent mouseEvent) throws IOException {
        System.out.println("Clicked");
        ConnectionData.server = server.getText();
        this.PORT = Integer.parseInt(port.getText());
        ConnectionData.userName = username.getText();
        ConnectionData.port = PORT;


        Stage stage;
        stage = (Stage) server.getScene().getWindow();
        //Parent parent = FXMLLoader.load(getClass().getResource("room.fxml"));
        Parent root = FXMLLoader.load(Controller.class.getResource("chatroom.fxml"));
        stage.setScene(new Scene(root, 800, 472));
        stage.setTitle(ConnectionData.userName);
        stage.setOnCloseRequest(e-> {
            //e.consume();
            Chatroom.th.interrupt();
            System.exit(0);
        });
        stage.setResizable(false);

        stage.show();
    }
}
