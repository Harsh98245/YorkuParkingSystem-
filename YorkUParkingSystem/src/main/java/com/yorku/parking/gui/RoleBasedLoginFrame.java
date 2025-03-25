package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;

public class RoleBasedLoginFrame extends JFrame {

    public RoleBasedLoginFrame(String loginType) {
        setTitle(loginType + " Login");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("👤 Username/Email:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(userLabel, gbc);

        JTextField userField = new JTextField();
        gbc.gridx = 1;
        add(userField, gbc);

        JLabel passLabel = new JLabel("🔒 Password:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passLabel, gbc);

        JPasswordField passField = new JPasswordField();
        gbc.gridx = 1;
        add(passField, gbc);

        ButtonGroup roleGroup = new ButtonGroup();
        JPanel rolePanel = new JPanel(new FlowLayout());

        if (loginType.equals("Client")) {
            gbc.gridx = 0;
            gbc.gridy = 2;
            gbc.gridwidth = 2;
            add(new JLabel("🎓 Select your role:"), gbc);

            JRadioButton studentBtn = new JRadioButton("Student");
            JRadioButton facultyBtn = new JRadioButton("Faculty");
            JRadioButton nonFacultyBtn = new JRadioButton("NonFaculty");
            JRadioButton visitorBtn = new JRadioButton("Visitor");

            roleGroup.add(studentBtn);
            roleGroup.add(facultyBtn);
            roleGroup.add(nonFacultyBtn);
            roleGroup.add(visitorBtn);

            rolePanel.add(studentBtn);
            rolePanel.add(facultyBtn);
            rolePanel.add(nonFacultyBtn);
            rolePanel.add(visitorBtn);

            gbc.gridy = 3;
            add(rolePanel, gbc);
        }

        JButton loginBtn = new JButton("Login");
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        add(loginBtn, gbc);

        JButton registerBtn = new JButton("Register");
        gbc.gridx = 1;
        add(registerBtn, gbc);

        setVisible(true);
    }
}
