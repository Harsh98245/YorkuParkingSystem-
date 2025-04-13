package com.yorku.parking.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ParkingService {
    private static final String PARKING_SPACES_FILE = "src/main/resources/parking_spaces.csv";

    public String[] loadParkingLots() {
        Set<String> lots = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(PARKING_SPACES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    lots.add(parts[2].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lots.toArray(new String[0]);
    }

    public boolean setLotStatus(String selectedLot, String status) {
        if (selectedLot == null) return false;
        
        File inputFile = new File(PARKING_SPACES_FILE);
        File tempFile = new File(PARKING_SPACES_FILE + ".temp");
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4 && parts[2].equals(selectedLot)) {
                    parts[3] = status;
                    line = String.join(",", parts);
                    updated = true;
                }
                writer.write(line);
                writer.newLine();
            }
            reader.close();
            writer.close();
            
            if (updated) {
                inputFile.delete();
                tempFile.renameTo(inputFile);
            }
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public List<String> getSensorData() {
        List<String> sensorData = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(PARKING_SPACES_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    sensorData.add("[" + parts[0] + "] Location: " + parts[1] + " | Lot: " + parts[2] + " | Status: " + parts[3]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sensorData;
    }
}