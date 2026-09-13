package com.example.hellofx.utils;

import com.example.hellofx.controllers.ReportsController.ReportSummary;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    // Portable path for persistent history records
    private static final String HISTORY_FILE = "logs/tracezero_history.dat";
    private static final String LEGACY_HISTORY_FILE = "src/main/java/com/example/hellofx/report/tracezero_history.dat";

    public static void saveHistory(List<ReportSummary> historyList) {
        try {
            File file = new File(HISTORY_FILE);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs(); // Ensure folder exists
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                // Convert to a standard ArrayList to ensure it saves safely
                oos.writeObject(new ArrayList<>(historyList));
            }
            System.out.println(">>> System: History successfully saved to disk.");
        } catch (Exception e) {
            System.out.println("!!! Error saving history: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<ReportSummary> loadHistory() {
        File file = new File(HISTORY_FILE);

        // Fallback/migration: Check if legacy file exists from previous development
        if (!file.exists()) {
            File legacy = new File(LEGACY_HISTORY_FILE);
            if (legacy.exists()) {
                file = legacy;
            } else {
                return new ArrayList<>(); // Return empty list if it's the first time running
            }
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<ReportSummary>) ois.readObject();
        } catch (Exception e) {
            System.out.println("!!! Error loading history: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
