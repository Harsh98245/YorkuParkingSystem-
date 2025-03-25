

package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


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
        managerLoginButton.addActionListener(e -> {
            new ManagerDashboardFrame(); // Launch manager dashboard
            dispose(); // Close the current login frame
        });
        add(managerLoginButton, gbc);




        gbc.gridy++;
        JButton superManagerLoginButton = new JButton("Super Manager Login");
        superManagerLoginButton.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(this, "Enter Super Manager Username:");
            String password = JOptionPane.showInputDialog(this, "Enter Password:");
            if (validateSuperManager(username, password)) {
                new SuperManagerPanel(username);
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Super Manager credentials.");
            }
        });
        add(superManagerLoginButton, gbc);

        gbc.gridy++;
        JButton registerButton = new JButton("Register");
        registerButton.addActionListener(e -> new RegisterFrame());
        add(registerButton, gbc);

        setVisible(true);
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
        SwingUtilities.invokeLater(LoginFrame::new);
    }
}