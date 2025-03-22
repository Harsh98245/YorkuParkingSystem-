package com.yorku.parking.utils;

import java.io.*;
import java.util.*;

public class BookingUtil {
	public static String getBookingsForUser(String username) {
	    StringBuilder sb = new StringBuilder();
	    sb.append("=== Your Bookings ===\n");
	    try (BufferedReader br = new BufferedReader(new FileReader("src/main/resources/bookings.csv"))) {
	        String line;
	        while ((line = br.readLine()) != null) {
	            if (line.startsWith(username + ",")) {
	                sb.append(line).append("\n");
	            }
	        }
	    } catch (IOException e) {
	        sb.append("Error loading bookings.");
	    }
	    return sb.toString();
	}


	
}
