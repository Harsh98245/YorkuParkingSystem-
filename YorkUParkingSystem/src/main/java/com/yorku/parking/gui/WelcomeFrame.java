package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class WelcomeFrame extends JFrame {

    public WelcomeFrame() {
        setTitle("Client Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        // Panel setup
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.LIGHT_GRAY);

        JLabel welcomeLabel = new JLabel("Welcome! Please choose an option:");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        panel.add(welcomeLabel);

        // Buttons
        JButton clientLoginBtn = createStyledButton("Client Login");
        JButton managerLoginBtn = createStyledButton("Manager Login");
        JButton superManagerLoginBtn = createStyledButton("Super Manager Login");
        JButton registerBtn = createStyledButton("Register");

        clientLoginBtn.addActionListener(e -> openLogin("client"));
        managerLoginBtn.addActionListener(e -> openLogin("manager"));
        superManagerLoginBtn.addActionListener(e -> openLogin("super"));
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

    private void openLogin(String role) {
        dispose();  // close WelcomeFrame
        LoginFrame loginFrame = new LoginFrame();  // Your existing login screen
        loginFrame.setTitle(role.substring(0, 1).toUpperCase() + role.substring(1) + " Login");
    }

    private void openRegister(ActionEvent e) {
        dispose();
        new RegisterFrame();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WelcomeFrame::new);
    }
}
