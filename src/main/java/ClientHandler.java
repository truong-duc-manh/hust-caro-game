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
    private ClientHandler[] clients;
    static int[][] matrix = new int[30][30];
    static int[] game = new int[2];
    private int clientId;

    public ClientHandler(Socket clientSocket, ClientHandler[] clients, int clientId) throws IOException {
        this.client = clientSocket;
        this.clients = clients;
        this.clientId = clientId;
        in = new DataInputStream(clientSocket.getInputStream());
        out = new DataOutputStream(clientSocket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                String ret = in.readUTF();

                if (ret.startsWith("say")) {
                    int firstSpace = ret.indexOf(" ");
                    if (firstSpace != -1) {
                        outToAll (ret.substring(firstSpace+1));
                    }
                }
                else if (ret.contains("caro") && !ret.contains("message")){
                    outToAll(ret);
                    addToMatrix(ret);
                    if (isDraw()) outToAll("Draw");
                    else {
                        String[] strs = ret.split(" ");
                        String playerName = strs[4];
                        int winner = checkWinner();
                        if (winner == 1) outToAll(playerName + " win");
                        else if (winner == 2) outToAll(playerName + " win");
                    }
                }
                else if (ret.contains("ready") && !ret.contains("message")) {
                    System.out.println(ret);
                    startGame();
                }
                else if (ret.contains("message")) {
                    outToAll(ret);
                }
                else if (ret.contains("reset")){
                    outToAll(ret);
                    resetMatrix();
                }
                else if (ret.contains("exit")){
//                    handlePlayerExit(ret);
                }

            }
        } catch (IOException e) {
            System.err.println("IO exception in client handler");
            System.err.println(e.getStackTrace());
        } finally {
            try {
                out.close();
                in.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
    Socket logic read message
     */

    //Them client vao game va chon x,o cho client
    public void addClient(int clientId) throws IOException {
        String ticToe = "";
        for (int i = 0; i < ClientHandler.game.length; i++) {
            if (ClientHandler.game[i] == 0) {
                if (i == 0) ticToe = TicToe.X.getTictoe();
                else ticToe = TicToe.O.getTictoe();
                ClientHandler.game[i] = 1;
                String serverConnect = "server add client " + (i+1) + " " + ticToe;
                out.writeUTF(serverConnect);
                return;
            }
        }
        String serverFull = "server full";
        out.writeUTF(serverFull);
    }

    // Gui message den tat ca client
    public void outToAll(String msg) {
        for (int i = 0; i < clients.length; i++) {
            if (clients[i] != null) {
                try {
                    clients[i].out.writeUTF(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Gui message den 2 player
    public void outToPlayers(String msg) {

    }

    //Khi so luong client = 2 gui message ve client va bat dau game
    public void startGame() {
        String send = "server allow clients game start";
        if (ClientHandler.game[0] != 0 && ClientHandler.game[1] != 0) {
            outToAll(send);
        }
    }

//    public void handlePlayerExit(String ret) {
//        String[] strs = ret.split(" ");
//        int playerId = Integer.valueOf(strs[1]);
//        int playerIndex = playerId - 1;
//
//        clients.remove(playerIndex);
//        ClientHandler.game[playerIndex] = 0;
//        String send = "server game not ready";
//        System.out.println(clients.size());
//        outToAll(send);
//    }


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

    // Kiem tra hoa
    public boolean isDraw() {
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 30; j++) {
                if (ClientHandler.matrix[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    // Reset matrix
    public void resetMatrix() {
        for (int i = 0; i < 30; i++) {
            for (int j = 0; j < 30; j++) {
                ClientHandler.matrix[i][j] = 0;
            }
        }
    }

}

