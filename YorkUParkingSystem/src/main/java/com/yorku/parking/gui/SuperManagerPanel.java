

package com.yorku.parking.gui;

import com.yorku.parking.utils.AccountGeneratorUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class SuperManagerPanel extends JFrame {
    private JTextArea outputArea;

    public SuperManagerPanel(String username) {
        setTitle("Super Manager Control Panel");
        setSize(500, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton generateBtn = new JButton("Generate Manager Account");
        generateBtn.addActionListener(this::generateAccount);

        outputArea = new JTextArea(5, 40);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        panel.add(generateBtn, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        add(panel);
        setVisible(true);
    }

    private void generateAccount(ActionEvent e) {
        try {
            String[] credentials = AccountGeneratorUtil.generateManagerAccount();
            outputArea.append("\nUsername: " + credentials[0] + "\nPassword: " + credentials[1] + "\n---\n");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Failed to generate account.", "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}