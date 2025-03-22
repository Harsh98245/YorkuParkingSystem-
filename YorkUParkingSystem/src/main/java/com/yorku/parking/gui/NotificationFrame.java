package com.yorku.parking.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class NotificationFrame extends JFrame {
    private JTextArea messageArea;
    private String username;

    public NotificationFrame(String username) {
        this.username = username;
        setTitle("Notifications");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        messageArea = new JTextArea();
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);

        JButton clearButton = new JButton("Clear My Notifications");
        clearButton.addActionListener(e -> clearNotifications());

        add(scrollPane, BorderLayout.CENTER);
        add(clearButton, BorderLayout.SOUTH);

        loadNotifications();
        setVisible(true);
    }

    private void loadNotifications() {
        messageArea.setText("");
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/notifications.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 4);
                if (parts.length == 4 && (parts[0].equalsIgnoreCase(username) || parts[0].equalsIgnoreCase("ALL"))) {
                    messageArea.append("🔔 [" + parts[1] + "] " + parts[2] + " (" + parts[3] + ")\n");
                }
            }
        } catch (IOException e) {
            messageArea.setText("⚠️ Error reading notifications.");
        }
    }

    private void clearNotifications() {
        List<String> remaining = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/resources/notifications.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith(username + ",")) {
                    remaining.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter writer = new PrintWriter("src/main/resources/notifications.csv")) {
            for (String l : remaining) {
                writer.println(l);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        messageArea.setText("✅ Notifications cleared.");
    }
}
