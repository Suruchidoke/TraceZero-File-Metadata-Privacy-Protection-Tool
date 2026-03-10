package com.example.hellofx.utils;

import com.example.hellofx.controllers.ReportsController.ReportSummary;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryManager {

    // Saves the history file in the same folder as your PDFs
    private static final String HISTORY_FILE = "F:/Demo/TraceZero/src/main/java/com/example/hellofx/report/tracezero_history.dat";

    public static void saveHistory(List<ReportSummary> historyList) {
        try {
            File file = new File(HISTORY_FILE);
            file.getParentFile().mkdirs(); // Ensure folder exists

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
        if (!file.exists()) {
            return new ArrayList<>(); // Return empty list if it's the first time running
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<ReportSummary>) ois.readObject();
        } catch (Exception e) {
            System.out.println("!!! Error loading history: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
