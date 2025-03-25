package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
//import com.yorku.parking.utils.CredentialsValidator;

public class LoginFrame extends JFrame {

    public LoginFrame() {
        setTitle("Client Portal");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel("Welcome! Please choose an option:");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.CENTER;

        JButton clientLoginButton = new JButton("Client Login");
        clientLoginButton.addActionListener(e -> new RoleBasedLoginFrame("Client"));
        add(clientLoginButton, gbc);

        gbc.gridy++;
        JButton managerLoginButton = new JButton("Manager Login");
        managerLoginButton.addActionListener(e -> new RoleBasedLoginFrame("Manager"));
        add(managerLoginButton, gbc);

        gbc.gridy++;
        JButton superManagerLoginButton = new JButton("Super Manager Login");
        superManagerLoginButton.addActionListener(e -> new RoleBasedLoginFrame("SuperManager"));
        add(superManagerLoginButton, gbc);

        gbc.gridy++;
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> new RegisterFrame());
        add(registerButton, gbc);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}
