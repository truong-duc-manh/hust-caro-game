import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Game {

    int id;
    int numberClients; // So luong nguoi choi
    int c1;
    int c2;
    int curC; // Nguoi choi hien tai
    int[][] xboard;
    int[][] oboard;

    public Game() {
        this.numberClients = 0;
        this.xboard = new int[30][30];
        this.oboard = new int[30][30];
    }

    public void addClient() {
        if (numberClients == 0) {
            c1 = 1;
            curC = c1;
            numberClients++;
            System.out.println("Add client1");
        } else if (numberClients == 1) {
            c2 = 2;
            curC = c2;
            numberClients++;
            System.out.println("Add client2");
        }
    }

    public void changeTurn() {
        curC = curC == c1 ? c2 : c1;
    }
}

