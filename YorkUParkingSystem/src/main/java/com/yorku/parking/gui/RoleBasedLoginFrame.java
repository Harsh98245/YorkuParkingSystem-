package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class RoleBasedLoginFrame extends JFrame {
    private JTextField userField;
    private JPasswordField passField;
    private JButton loginBtn, registerBtn, backBtn;
    private String loginType;
    private ButtonGroup roleGroup;
    private JPanel rolePanel;

    public RoleBasedLoginFrame(String type) {
        this.loginType = type;
        setTitle(type + " Login");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username/Email:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(userLabel, gbc);

        userField = new JTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(userField, gbc);

        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passLabel, gbc);

        passField = new JPasswordField();
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(passField, gbc);

        if (loginType.equals("Client")) {
            JLabel roleLabel = new JLabel("Select your role:");
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            add(roleLabel, gbc);

            rolePanel = new JPanel();
            roleGroup = new ButtonGroup();

            String[] roles = {"Student", "Faculty", "NonFaculty", "Visitor"};
            for (String role : roles) {
                JRadioButton radio = new JRadioButton(role);
                radio.setActionCommand(role);
                roleGroup.add(radio);
                rolePanel.add(radio);
            }

            gbc.gridy = 3;
            add(rolePanel, gbc);
        }

        loginBtn = new JButton("Login");
        registerBtn = new JButton("Register");
        backBtn = new JButton("Back");

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        add(loginBtn, gbc);

        gbc.gridx = 1;
        add(registerBtn, gbc);

        // Add the back button
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        add(backBtn, gbc);

        loginBtn.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword()).trim();
            String role = loginType;

            if (loginType.equals("Client")) {
                if (roleGroup.getSelection() == null) {
                    JOptionPane.showMessageDialog(this, "Please select a role.");
                    return;
                }

                for (Component comp : rolePanel.getComponents()) {
                    if (comp instanceof JRadioButton jrb && jrb.isSelected()) {
                        role = jrb.getText();
                        break;
                    }
                }
            }

            if (validateCredentials(username, password, role)) {
                dispose();
                if (role.equalsIgnoreCase("Manager")) {
                    new ManagerDashboardFrame();
                } else {
                    new DashboardFrame(username, false);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials.");
            }
        });

        registerBtn.addActionListener(e -> {
            dispose();
            new RegisterFrame();
        });

        // Action for back button
        backBtn.addActionListener(e -> {
            dispose();
            new WelcomeFrame(); // Go back to WelcomeFrame
        });
    }

    private boolean validateCredentials(String username, String password, String role) {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/users.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 &&
                    parts[0].equalsIgnoreCase(username) &&
                    parts[1].equals(password) &&
                    parts[2].equalsIgnoreCase(role)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
