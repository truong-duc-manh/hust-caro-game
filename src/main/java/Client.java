import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

@Getter
@Setter
public class Client {

    private final String SERVER_IP = "127.0.0.1";
    private final int SERVER_PORT = 9090;
    Socket socket;
    DataInputStream in;
    BufferedReader keyboard;
    DataOutputStream out;

    JFrame f;
    JFrame f1;
    JPanel p;
    JButton[][] btns;
    JButton btnOk;
    JTextField inpName;
    JLabel lblName;

    int x,y;
    TicToe ticToe;
    int id;
    String playerName;
    boolean isStarted;
    boolean isYourTurn;



    public Client() throws IOException {
        this.isYourTurn = false;
        this.isStarted = false;
        this.playerName = "";
        this.socket = new Socket(SERVER_IP, SERVER_PORT);
        this.in = new DataInputStream(socket.getInputStream());
        this.keyboard = new BufferedReader(new InputStreamReader(System.in));
        this.out = new DataOutputStream(socket.getOutputStream());
        gameScreen();
        f.setVisible(false);
        namePlayerScreen();


        // Thread send message
        Thread sendMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    String conStr = "client connect";
                    try {
                        out.writeUTF(conStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    System.out.println("> ");
                    String command = null;

                    try {
                        command = keyboard.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (command.equals("quit")) break;

                    try {
                        out.writeUTF(command);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.exit(0);
            }
        });

        // Thread read message
        Thread readMessage = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        String serverResponse = in.readUTF();

                        if (serverResponse.contains("server client")) {
                            String[] temp = serverResponse.split(" ");
                            System.out.println(Arrays.toString(temp));
                            setId(Integer.valueOf(temp[2]));
                            setTicToe(TicToe.valueOf(temp[3]));
                            if (getId() == 1) isYourTurn = true;
                            else isYourTurn = false;
                        } else if (serverResponse.contains("caro")) {
                            System.out.println("Print");
                            SwingUtilities.invokeLater(() -> {
                                fillBtn(serverResponse);
                            });
                        } else if (serverResponse.equalsIgnoreCase("server clients startgame")) {
                            System.out.println("Start game");
                        }

                        if (serverResponse == null) break;
                        System.out.println("Server says: " + serverResponse);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        sendMessage.start();
        readMessage.start();
    }

    /**
     Screen
     */

    //Screen choi game
    public void gameScreen() throws IOException {
        f = new JFrame();
        f.setTitle("Caro game");
        f.setSize(1000, 800);
        x = 30;
        y = 30;
        f.getContentPane().setLayout(null);
        f.setResizable(false);

        // Panel chua cac Button
        p = new JPanel();
        p.setBounds(10,50,600,600);
        p.setLayout(new GridLayout(x,y));
        f.add(p);

        //Ten nguoi choi
        lblName = new JLabel();
        lblName.setBounds(800, 50, 100,100);
        f.add(lblName);

        //Ve ban caro
        btns = new JButton[x][y];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                btns[i][j] = new JButton();
                JButton btn = new JButton();
                // Xu li click
                btn.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(btn.getText().equals("") && isYourTurn){
                            SwingUtilities.invokeLater(() -> {
                                int a = btn.getX()/20;
                                int b = btn.getY()/20;
                                sendCordToSer(a, b);
                            });
                        } else {
                            System.out.println("You cannot click");
                        }
                    }
                });
                p.add(btn);
                p.setVisible(true);
            }
        }
        togglePanel();
        f.setVisible(true);
    }



    //Screen nhap ten
    public void namePlayerScreen() {
        f1 = new JFrame();
        inpName = new JTextField();
        btnOk = new JButton("OK");

        f1.setTitle("Create Name");
        f1.setSize(500,250);
        f1.getContentPane().setLayout(null);
        f1.setResizable(false);

        //Tao input
        inpName = new JTextField();
        inpName.setBounds(75,80, 250, 30);

        //Tao button
        btnOk = new JButton("OK");
        btnOk.setBounds(340, 80, 50, 30);
        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                lblName.setText(inpName.getText());
                setPlayerName(inpName.getText());
                f1.setVisible(false);
                f.setVisible(true);
            }
        });

        //Hien thi frame
        f1.add(inpName);
        f1.add(btnOk);
        f1.setVisible(true);
    }

    public void togglePanel() {
        if (isStarted) p.setVisible(true);
        else p.setVisible(false);
    }

    /**
    Logic xu li message nhan tu server
     */

    //Danh dau vao o chon
    public void fillBtn (String ret) {
        String[] str = ret.split(" ");
        int a = Integer.valueOf(str[2]);
        int b = Integer.valueOf(str[3]);
        int index = b * 30 + a;
        JButton temp = new JButton();
        if (str[0].equals("player1")) {
            if (getId() == 1) isYourTurn = false;
            else isYourTurn = true;
            setTicToe(TicToe.X);
            temp.setText(ticToe.getTictoe());
            temp.setForeground(Color.RED);
        }
        else {
            if (getId() == 2) isYourTurn = false;
            else isYourTurn = true;
            setTicToe(TicToe.O);
            temp.setText(ticToe.getTictoe());
            temp.setForeground(Color.BLUE);
        }
        p.remove(index);
        p.add(temp, index);
        p.revalidate();
        p.repaint();
    }

    /**
    Logic gui message len server
     */

    //Gui message dia chi danh toi server
    public void sendCordToSer(int x, int y) {
        String send = "player" + getId() + " caro " + x + " " + y;
        try {
            out.writeUTF(send);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Gui ten len server
    public void sendName(String name) {
        String send = "player" + getId() + " name " + name;
        try {
            out.writeUTF(send);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
