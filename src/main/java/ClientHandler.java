import lombok.Getter;
import lombok.Setter;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

@Getter
@Setter
public class ClientHandler implements Runnable{

    private Socket client;
    private DataInputStream in;
    private DataOutputStream out;
    private ArrayList<ClientHandler> clients;
    static int[][] matrix = new int[30][30];

    public ClientHandler(Socket clientSocket, ArrayList<ClientHandler> clients) throws IOException {
        this.client = clientSocket;
        this.clients = clients;
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                String request = in.readUTF();
                if (request.contains("name")) {
//                    out.writeUTF(Server.getRandomName());

                } else if (request.startsWith("say")) {
                    int firstSpace = request.indexOf(" ");
                    if (firstSpace != -1) {
                        outToAll (request.substring(firstSpace+1));
                    }
                } else if (request.contains("caro")){
                    outToAll(request);
                    addToMatrix(request);
                    System.out.println("Done");
                    int winner = checkWinner();
                    if (winner == 1) outToAll("player1 win");
                    else if (winner == 2) outToAll("player2 win");
                }
            }
        } catch (IOException e) {
            System.err.println("IO exception in client handler");
            System.err.println(e.getStackTrace());
        } finally {
            try {
                out.close();
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
    Socket logic
     */

    //Them client vao game va chon x,o cho client
    public void addClient(int numberClient) throws IOException {
        String ticToe = "";
        if (numberClient == 1) ticToe = TicToe.X.getTictoe();
        else if (numberClient == 2) ticToe = TicToe.O.getTictoe();
        String serverConnect = "server client " + numberClient + " " + ticToe;
        System.out.println("Clients size " + clients.size());
        out.writeUTF(serverConnect);
    }

    // Gui message den tat ca client
    public void outToAll(String msg) {
        for (ClientHandler aClient : clients) {
            try {
                aClient.out.writeUTF(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Khi so luong client = 2 gui message ve client va bat dau game
    public void startGame() {
        if (clients.size() == 2) {
            String send = "server clients startgame";
            outToAll(send);
        }
    }


    /**
    Logic game
     */

    // Them vao ma tran danh
    private void addToMatrix(String msg) {
        String[] str = msg.split(" ");
        int a = Integer.valueOf(str[2]);
        int b = Integer.valueOf(str[3]);
        String p = str[0];
        for (ClientHandler aClient : clients) {
            if (p.equals("player1")) {
                ClientHandler.matrix[a][b] = 1;
            } else {
                ClientHandler.matrix[a][b] = 2;
            }
        }
        System.out.println(ClientHandler.matrix);
    }

    //Kiem tra nguoi chien thang
    public int checkWinner() {
        for (int i = 0; i < 30-4; i++) {
            for (int j = 0; j < 30-4; j++) {
                if (ClientHandler.matrix[i][j] != 0) {
                    // Check hang ngang
                    if (ClientHandler.matrix[i][j] == ClientHandler.matrix[i][j+1] &&
                            ClientHandler.matrix[i][j] == ClientHandler.matrix[i][j+2] &&
                            ClientHandler.matrix[i][j] == ClientHandler.matrix[i][j+3] &&
                            ClientHandler.matrix[i][j] == ClientHandler.matrix[i][j+4]) {
                        System.out.println("Win");
                        return matrix[i][j];
                        // Check hang doc
                    } else if (ClientHandler.matrix[i][j] == ClientHandler.matrix[i+1][j] &&
                            ClientHandler.matrix[i][j] == ClientHandler.matrix[i+2][j] &&
                            ClientHandler.matrix[i][j] == ClientHandler.matrix[i+3][j] &&
                            ClientHandler.matrix[i][j] == ClientHandler.matrix[i+4][j]){
                        System.out.println("Win");
                        return matrix[i][j];
                    } else if (ClientHandler.matrix[i][j] == ClientHandler.matrix[i+1][j+1] &&
                            ClientHandler.matrix[i][j] == ClientHandler.matrix[i+2][j+2] &&
                            ClientHandler.matrix[i][j] == ClientHandler.matrix[i+3][j+3] &&
                            ClientHandler.matrix[i][j] == ClientHandler.matrix[i+4][j+4]){
                        System.out.println("Win");
                        return matrix[i][j];
                    } else if (ClientHandler.matrix[i][j] == ClientHandler.matrix[i+1][j-1] &&
                            ClientHandler.matrix[i][j] == ClientHandler.matrix[i+2][j-2] &&
                            ClientHandler.matrix[i][j] == ClientHandler.matrix[i+3][j-3] &&
                            ClientHandler.matrix[i][j] == ClientHandler.matrix[i+4][j-4] &&
                            j >=5){
                        System.out.println("Win");
                        return matrix[i][j];
                    }
                }
            }
        }
        return 0;
    }

}

