package com.yorku.parking;

import java.awt.Color;
import java.awt.Font;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatIntelliJLaf;
import com.yorku.parking.gui.WelcomeFrame;

public class Main {
    public static void main(String[] args) {
        System.out.println("🚗 Starting YorkU Parking System...");

        // Set FlatLaf look and feel
        try {
            UIManager.setLookAndFeel(new FlatIntelliJLaf());

            // Custom UI design tweaks
            UIManager.put("Button.arc", 20);
            UIManager.put("Component.arc", 15);
            UIManager.put("ProgressBar.arc", 15);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("Panel.background", Color.WHITE);
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));

        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("❌ Failed to initialize FlatLaf. Using default.");
        }

        // Launch the Login GUI
        SwingUtilities.invokeLater(WelcomeFrame::new);
    }
}