package com.yorku.parking.gui;

import com.formdev.flatlaf.FlatLightLaf;
import com.yorku.parking.utils.SessionManager;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginFrame() {
        FlatLightLaf.setup(); // Apply modern theme
        UIManager.put("Button.arc", 20);
        UIManager.put("Component.arc", 15);
        UIManager.put("TextComponent.arc", 10);
        UIManager.put("Panel.background", Color.WHITE);
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));

        setTitle("🚗 YorkU Parking System - Login");
        setSize(420, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Panel setup with MigLayout
        JPanel panel = new JPanel(new MigLayout("wrap 2", "[right][grow,fill]", "[]15[]15[]20[]"));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel userLabel = new JLabel("👤 Username:");
        JLabel passLabel = new JLabel("🔒 Password:");
        usernameField = new JTextField();
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        panel.add(userLabel);
        panel.add(usernameField);
        panel.add(passLabel);
        panel.add(passwordField);
        panel.add(loginButton, "span, split 2, center, width 100!");
        panel.add(registerButton, "width 100!");

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e -> register());

        add(panel);
        setVisible(true);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (SessionManager.isLoggedIn(username)) {
            JOptionPane.showMessageDialog(this, "🚫 User already logged in.", "Login Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (validateLogin(username, password)) {
            SessionManager.login(username);
            JOptionPane.showMessageDialog(this, "✅ Login Successful!");
            dispose();
            boolean isManager = checkIfManager(username);
            new DashboardFrame(username, isManager);
        } else {
            JOptionPane.showMessageDialog(this, "❌ Invalid Username or Password", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")) {
            JOptionPane.showMessageDialog(this, "🔐 Password must contain uppercase, lowercase, number, and symbol.", "Weak Password", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠️ Username cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(",")[0].equalsIgnoreCase(username)) {
                    JOptionPane.showMessageDialog(this, "⚠️ Username already exists.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/users.csv", true))) {
            writer.write(username + "," + password);
            writer.newLine();
            JOptionPane.showMessageDialog(this, "🎉 Registration Successful!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean validateLogin(String username, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/users.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] creds = line.split(",");
                if (creds.length >= 2 && creds[0].equals(username) && creds[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean checkIfManager(String username) {
        return username.toLowerCase().contains("manager") || username.toLowerCase().contains("admin");
    }
}
