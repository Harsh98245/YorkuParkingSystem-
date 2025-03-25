package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class WelcomeFrame extends JFrame {

    public WelcomeFrame() {
        setTitle("Client Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.LIGHT_GRAY);

        JLabel welcomeLabel = new JLabel("Welcome! Please choose an option:");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        panel.add(welcomeLabel);

        JButton clientLoginBtn = createStyledButton("Client Login");
        JButton managerLoginBtn = createStyledButton("Manager Login");
        JButton superManagerLoginBtn = createStyledButton("Super Manager Login");
        JButton registerBtn = createStyledButton("Register");

        clientLoginBtn.addActionListener(e -> {
            dispose();
            new RoleBasedLoginFrame("Client");
        });

        managerLoginBtn.addActionListener(e -> {
            dispose();
            new ManagerDashboardFrame(); // Opens manager dashboard directly
        });

        superManagerLoginBtn.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(this, "Enter Super Manager Username:");
            String password = JOptionPane.showInputDialog(this, "Enter Password:");
            if (validateSuperManager(username, password)) {
                dispose();
                new SuperManagerPanel(username);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Super Manager credentials.");
            }
        });

        registerBtn.addActionListener(this::openRegister);

        panel.add(clientLoginBtn);
        panel.add(managerLoginBtn);
        panel.add(superManagerLoginBtn);
        panel.add(registerBtn);

        add(panel);
        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(200, 35));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE);
        return button;
    }

    private void openRegister(ActionEvent e) {
        dispose();
        new RegisterFrame();
    }

    private boolean validateSuperManager(String username, String password) {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/users.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equals(username)
                        && parts[1].equals(password) && parts[2].equalsIgnoreCase("SuperManager")) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WelcomeFrame::new);
    }
}
