package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

public class RegisterFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeDropdown;
    private JButton registerButton;

    public RegisterFrame() {
        setTitle("User Registration");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel("User Type:"));
        userTypeDropdown = new JComboBox<>(new String[]{"Student", "Faculty", "Non-Faculty Staff", "Visitor"});
        panel.add(userTypeDropdown);

        registerButton = new JButton("Register");
        panel.add(registerButton);

        registerButton.addActionListener(e -> registerUser());

        add(panel);
        setVisible(true);
    }

    private void registerUser() {
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String userType = (String) userTypeDropdown.getSelectedItem();

        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")) {
            JOptionPane.showMessageDialog(this, "Password must contain uppercase, lowercase, number, and symbol.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (FileWriter fw = new FileWriter("src/main/resources/users.csv", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(email + "," + password + "," + userType);
            JOptionPane.showMessageDialog(this, "Registration Successful!");
            dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
