package Client;


import lombok.Builder;

@Builder
public class Message {
    public String name;
    public String message;
    public MessageType messageType;

}
