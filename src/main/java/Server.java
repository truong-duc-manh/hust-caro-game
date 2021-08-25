import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 9090;
    private static String[] names = {"Willy", "Felix", "Carlsbad", "Hobob"};
    private static String[] adjs = {"the gentle", "the un-gentle", "the overwrought"};

    private static ClientHandler[] clients = new ClientHandler[100];
    private static ExecutorService pool = Executors.newFixedThreadPool(2);
    public static int clientId = 0;

    public static void main(String[] args) throws IOException {
        ServerSocket listener = new ServerSocket(PORT);

        while (true) {
            System.out.println("[SERVER] Wating for client connection...");
            Socket client = listener.accept();

            System.out.println(clientId);
            System.out.println("[SERVER] Connected to client!");
            clientId++;
            ClientHandler clientThread = new ClientHandler(client, clients, clientId);
            clients[clientId] = clientThread;
            clientThread.addClient(clientId);
            pool.execute(clientThread);

        }
    }
}

