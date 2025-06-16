package com.tuandev.qrscanner;

import java.awt.*;
import java.io.*;
import java.util.List;

import java.awt.Desktop;
import java.io.*;
import java.util.List;

public class CSVService {
    public static void writeCSVFile(File file, List<String> dataList) {
        try (FileOutputStream fileOut = new FileOutputStream(file);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOut))) {

            writer.write("Data");
            writer.newLine();

            for (String data : dataList) {
                writer.write(escapeCSV(data));
                writer.newLine();
            }

            writer.flush();
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi ghi file CSV: " + e.getMessage(), e);
        }
    }

    private static String escapeCSV(String value) {
        boolean containsSpecialChar = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");

        if (containsSpecialChar) {
            value = value.replace("\"", "\"\""); // Escape dấu "
            return "\"" + value + "\"";
        }
        return value;
    }
}

