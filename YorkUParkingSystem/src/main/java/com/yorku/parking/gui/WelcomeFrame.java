package com.yorku.parking.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

public class WelcomeFrame extends JFrame {

    public WelcomeFrame() {
        setTitle("Client Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
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
        JButton checkoutBtn = createStyledButton("Check Out");
        JButton managerLoginBtn = createStyledButton("Manager Login");
        JButton superManagerLoginBtn = createStyledButton("Super Manager Login");
        JButton registerBtn = createStyledButton("Register");

        clientLoginBtn.addActionListener(e -> {
            dispose();
            new RoleBasedLoginFrame("Client");
        });

        checkoutBtn.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(this, "Enter your username to check out:");
            if (username != null && !username.trim().isEmpty()) {
                handleCheckout(username.trim());
            }
        });

        managerLoginBtn.addActionListener(e -> {
            dispose();
            new ManagerDashboardFrame();
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
        panel.add(checkoutBtn);
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
    private void handleCheckout(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/bookings.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && parts[1].equals(username)) {
                    String space = parts[3];
                    int hours = Integer.parseInt(parts[4]);
                    String role = getUserRole(username);
                    double rate = switch (role.toLowerCase()) {
                        case "student" -> 5.0;
                        case "faculty" -> 8.0;
                        case "nonfaculty" -> 10.0;
                        default -> 15.0;
                    };
                    double total = rate * hours;
                    double deposit = rate;
                    double remaining = total - deposit;

                    new PaymentFrame(username, space, role, hours, remaining, false); // false = not a deposit
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "No booking found for this user.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getUserRole(String username) {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/users.csv"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3 && parts[0].equalsIgnoreCase(username)) {
                    return parts[2];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "visitor";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(WelcomeFrame::new);
    }
}
