package com.tuandev.qrscanner;

import java.awt.*;
import java.io.*;
import java.util.List;

public class CSVService {
    public static void writeCSVFile(File file, List<String> dataList) {
        try(FileOutputStream fileOut = new FileOutputStream(file);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOut))) {
            writer.write("Data");
            writer.newLine();
            for (String data : dataList) {
                writer.write(data);
                writer.newLine();
            }
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
