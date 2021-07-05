package view;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Getter
@Setter
public class NamePlayerScreen {
    private JFrame f;
    private JTextField inputName;
    private JButton btnOk;

    public NamePlayerScreen() {
        //Tao frame
        f = new JFrame();

        f.setTitle("Create Name");
        f.setSize(500,250);
        f.getContentPane().setLayout(null);
        f.setResizable(false);

        //Tao input
        inputName = new JTextField();
        inputName.setBounds(75,80, 250, 30);

        //Tao button
        btnOk = new JButton("OK");
        btnOk.setBounds(340, 80, 50, 30);
        btnOk.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getName();
            }
        });

        //Hien thi frame
        f.add(inputName);
        f.add(btnOk);
        f.setVisible(true);
    }

    public String getName() {
        return inputName.getText();
    }


}
