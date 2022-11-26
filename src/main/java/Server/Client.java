package Server;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.net.InetAddress;
@Builder
@AllArgsConstructor
public class Client {
    public String username;
    public InetAddress clientAddress;
    public int clientPort;
}
