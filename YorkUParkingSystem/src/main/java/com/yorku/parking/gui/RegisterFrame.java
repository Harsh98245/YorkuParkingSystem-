// ✅ RegisterFrame.java (Final Version)
package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

public class RegisterFrame extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JRadioButton studentBtn, facultyBtn, nonFacultyBtn, visitorBtn;
    private JButton registerBtn;

    public RegisterFrame() {
        setTitle("Register");
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
        JPanel rolePanel = new JPanel(new FlowLayout());
        studentBtn = new JRadioButton("Student");
        facultyBtn = new JRadioButton("Faculty");
        nonFacultyBtn = new JRadioButton("NonFaculty");
        visitorBtn = new JRadioButton("Visitor");
        ButtonGroup roleGroup = new ButtonGroup();
        roleGroup.add(studentBtn);
        roleGroup.add(facultyBtn);
        roleGroup.add(nonFacultyBtn);
        roleGroup.add(visitorBtn);
        rolePanel.add(studentBtn);
        rolePanel.add(facultyBtn);
        rolePanel.add(nonFacultyBtn);
        rolePanel.add(visitorBtn);
        panel.add(rolePanel);

        registerBtn = new JButton("Register");
        panel.add(registerBtn);

        registerBtn.addActionListener(this::registerUser);

        add(panel);
        setVisible(true);
    }

    private void registerUser(ActionEvent e) {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String role = studentBtn.isSelected() ? "student" :
                      facultyBtn.isSelected() ? "faculty" :
                      nonFacultyBtn.isSelected() ? "non-faculty staff" :
                      visitorBtn.isSelected() ? "visitor" : "";

        if (email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        try (PrintWriter out = new PrintWriter(new FileWriter("src/main/resources/users.csv", true))) {
            out.println(email + "," + password + "," + role);
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving registration.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Registration successful!");
        dispose();
        new LoginFrame(); // auto-login redirection
    }
}
