package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;

public class RegisterFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JRadioButton studentButton, facultyButton, staffButton, visitorButton;
    private JButton registerButton;

    public RegisterFrame() {
        setTitle("User Registration");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        panel.add(new JLabel("Email:"));
        emailField = new JTextField();
        panel.add(emailField);

        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        panel.add(passwordField);

        panel.add(new JLabel("Select Role:"));

        // Role selection using radio buttons
        JPanel rolePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        studentButton = new JRadioButton("Student");
        facultyButton = new JRadioButton("Faculty");
        staffButton = new JRadioButton("NonFaculty");
        visitorButton = new JRadioButton("Visitor");

        ButtonGroup group = new ButtonGroup();
        group.add(studentButton);
        group.add(facultyButton);
        group.add(staffButton);
        group.add(visitorButton);

        rolePanel.add(studentButton);
        rolePanel.add(facultyButton);
        rolePanel.add(staffButton);
        rolePanel.add(visitorButton);
        panel.add(rolePanel);

        registerButton = new JButton("Register");
        panel.add(new JLabel()); // filler
        panel.add(registerButton);

        registerButton.addActionListener(e -> registerUser());

        add(panel);
        setVisible(true);
    }

    private void registerUser() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        String userType = null;
        if (studentButton.isSelected()) userType = "Student";
        else if (facultyButton.isSelected()) userType = "Faculty";
        else if (staffButton.isSelected()) userType = "Non-Faculty Staff";
        else if (visitorButton.isSelected()) userType = "Visitor";

        if (email.isEmpty() || password.isEmpty() || userType == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields and select a role.");
            return;
        }

        if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$")) {
            JOptionPane.showMessageDialog(this,
                "Password must contain uppercase, lowercase, number, and symbol.",
                "Error", JOptionPane.ERROR_MESSAGE);
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
