package com.yorku.parking.utils;

import java.io.*;
import java.util.*;

public class BookingUtil {
	public static List<String> getBookingsForUser(String username) {
	    List<String> bookings = new ArrayList<>();
	    try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/bookings.csv"))) {
	        String line;
	        while ((line = br.readLine()) != null) {
	            if (line.startsWith(username)) {
	                bookings.add(line);
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return bookings;
	}

	public static void updateSpaceStatus(String spaceInfo, String status) {
	    List<String> lines = new ArrayList<>();
	    try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/parking_spaces.csv"))) {
	        String line;
	        while ((line = br.readLine()) != null) {
	            String[] parts = line.split(",");
	            if (parts.length >= 3 && (parts[0] + " - " + parts[1] + " (" + parts[2] + ")").equals(spaceInfo)) {
	                lines.add(parts[0] + "," + parts[1] + "," + status); // overwrite status
	            } else {
	                lines.add(line); // keep line as is
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    try (PrintWriter writer = new PrintWriter(new FileWriter("src/main/resources/parking_spaces.csv"))) {
	        for (String updatedLine : lines) {
	            writer.println(updatedLine);
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	public static void removeBookingLine(String bookingLine) {
	    File inputFile = new File("src/main/resources/bookings.csv");
	    File tempFile = new File("src/main/resources/bookings_temp.csv");

	    try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
	         PrintWriter writer = new PrintWriter(new FileWriter(tempFile))) {
	        String line;
	        while ((line = reader.readLine()) != null) {
	            if (!line.trim().equals(bookingLine.trim())) {
	                writer.println(line);
	            }
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    inputFile.delete();
	    tempFile.renameTo(inputFile);
	}
	
	public static void saveBooking(String username, String plate, String space, int hours) {
	    try (PrintWriter out = new PrintWriter(new FileWriter("src/main/resources/bookings.csv", true))) {
	        out.println(username + "," + plate + "," + space + "," + hours);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}





	
}
