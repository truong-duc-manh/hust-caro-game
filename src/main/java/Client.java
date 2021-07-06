import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
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
    JScrollPane chatPane;
    JButton[][] btns;
    JButton btnOk;
    JButton btnSendMes;
    JTextField inpName;
    JTextField inpMessage;
    JTextPane chatBox;
    SimpleAttributeSet left;
    SimpleAttributeSet right;
    StyledDocument doc;
    JLabel lblName;
    JLabel lblWaiting;

    int x,y;
    TicToe ticToe;
    int id;
    String playerName;
    boolean isStarted;
    boolean isYourTurn;
    boolean isReset;



    public Client() throws IOException {
        this.isYourTurn = false;
        this.isStarted = false;
        this.isReset = false;
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
                        String ret = in.readUTF();
                        if (ret.contains("server add client")) {
                            String[] temp = ret.split(" ");
                            System.out.println(Arrays.toString(temp));
                            setId(Integer.valueOf(temp[3]));
                            setTicToe(TicToe.valueOf(temp[4]));
                            if (getId() == 1) isYourTurn = true;
                            else isYourTurn = false;
                        }
                        else if (ret.contains("caro")) {
                            System.out.println(ret);
                            SwingUtilities.invokeLater(() -> {
                                fillBtn(ret);
                            });
                        }
                        else if (ret.equalsIgnoreCase("server allow clients game start")) {
                            isStarted = true;
                            togglePanel(isStarted);
                        }
                        else if (ret.contains("message")){
                            receiveMess(ret);
                        }
                        else if (ret.contains("win")){
                            alertWin(ret);
                        }

                        System.out.println("[SERVER] says: " + ret);
                        if (ret == null) break;
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

        // Lbl cho nguoi choi khac
        lblWaiting = new JLabel("Waiting for another player...");
        lblWaiting.setBounds(220,300, 250,100);
        f.add(lblWaiting);

        //Ten nguoi choi
        lblName = new JLabel();
        lblName.setBounds(750, 80, 200,100);
        f.add(lblName);

        //Chat box
        chatBox = new JTextPane();
        chatBox.setEditable(false);
        doc = chatBox.getStyledDocument();
        left = new SimpleAttributeSet();
        StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
        StyleConstants.setForeground(left, Color.RED);
        right = new SimpleAttributeSet();
        StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
        StyleConstants.setForeground(right, Color.BLUE);

        chatPane = new JScrollPane(chatBox);
        chatPane.setBounds(650, 150, 300, 400);
        chatPane.setVisible(true);
        f.getContentPane().add(chatPane);

        //Input send message
        inpMessage = new JTextField();
        inpMessage.setBounds(650, 570, 250, 30);
        f.add(inpMessage);

        //Btn send message
        btnSendMes = new JButton("Send");
        btnSendMes.setBounds(900, 570, 60, 30);
        f.add(btnSendMes);
        btnSendMes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!inpMessage.getText().isEmpty()) {
                    String message = inpMessage.getText();
                    String addToChatBox = "[" + playerName +"]" + "\n" + message + "\n\n";
                    try {
                        doc.setParagraphAttributes(doc.getLength(), 1, right, false);
                        doc.insertString(doc.getLength(), addToChatBox, right);
                    } catch (BadLocationException badLocationException) {
                        badLocationException.printStackTrace();
                    }
                    inpMessage.setText(null);
                    sendMessage(message);
                }
            }
        });

        drawCaroBoard();

        togglePanel(isStarted);

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }

    //Ve ban caro
    public void drawCaroBoard() {
        p.removeAll();
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
            }
        }
        p.updateUI();
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
                String temp = "Player name: " + inpName.getText();
                lblName.setText(temp);
                setPlayerName(inpName.getText());
                sendReadyMessage();
                f1.setVisible(false);
                f.setVisible(true);
            }
        });

        //Hien thi frame
        f1.add(inpName);
        f1.add(btnOk);
        f1.setVisible(true);
    }

    public void togglePanel(boolean isStarted) {
        if (isStarted) {
            p.setVisible(true);
            lblWaiting.setVisible(false);
        }
        else {
            p.setVisible(false);
            lblWaiting.setVisible(true);
        }
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

    public void receiveMess(String ret) {
        String[] retArr = ret.split("-");
        String playerIdStr = "player" + getId();
        if (!retArr[0].equals(playerIdStr)) {
            System.out.println(playerIdStr);
            String opName = retArr[1];
            String mess = retArr[3];
            String addToChatBox = "[" + opName + "]" + "\n" + mess + "\n\n";
            try {
                doc.setParagraphAttributes(doc.getLength(), 1, left, false);
                doc.insertString(doc.getLength(), addToChatBox, left );
            } catch (BadLocationException badLocationException) {
                badLocationException.printStackTrace();
            }
        }
    }

    public void alertWin(String ret) {
        String[] retArr = ret.split(" ");
        String winPlayer = retArr[0];
        JDialog d = new JDialog(f, "Win Alert");
        JLabel l = new JLabel(winPlayer + " win");
        JButton btnReset = new JButton("Play again");

        JPanel pn = new JPanel();
        pn.setLayout(new BoxLayout(pn, BoxLayout.Y_AXIS));
        pn.setBorder(new EmptyBorder(20,10,20,10));
        pn.add(l);
        pn.add(btnReset);

        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnReset.setAlignmentX(Component.CENTER_ALIGNMENT);

        d.add(pn);
        d.setSize(200,200);
        d.setLocation(f.getX()+200,f.getY()+200);
        d.setVisible(true);

        btnReset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isReset = true;
                sendResetGame();
                drawCaroBoard();
                if (playerName.equals(winPlayer)) isYourTurn = false;
                else isYourTurn = true;
                d.setVisible(false);
            }
        });
    }

    /**
    Logic gui message len server
     */

    //Gui message dia chi danh toi server
    public void sendCordToSer(int x, int y) {
        String send = "player" + getId() + " caro " + x + " " + y + " " + getPlayerName();
        try {
            out.writeUTF(send);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Gui ready len server
    public void sendReadyMessage() {
        String send = "player" + getId() + " ready";
        try {
            out.writeUTF(send);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Gui message len server
    public void sendMessage(String mess) {
        String send = "player" + getId() + "-" + getPlayerName() + "-message-" + mess;
        try {
            out.writeUTF(send);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendResetGame() {
        String send = "player" + getId() + " " + getPlayerName() + " " + "reset";
        try {
            out.writeUTF(send);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
