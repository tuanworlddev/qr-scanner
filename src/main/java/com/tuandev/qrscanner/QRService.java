package com.tuandev.qrscanner;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class QRService {
    private static final Logger logger = LogManager.getLogger();
    private static Dialog<Void> dialog;

    public static void scannerQRCode(File pdfFile, TableView<String> tableView) {

        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> result = new ArrayList<>();
                PDDocument pdfDocument = Loader.loadPDF(pdfFile);
                PDFRenderer pdfRenderer = new PDFRenderer(pdfDocument);
                int pageCount = pdfDocument.getNumberOfPages();

                for (int i = 0; i < pageCount; i++) {
                    if (isCancelled()) {
                        updateMessage("Cancelled");
                    }
                    BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(i, 300);
                    String qrCodeData = scanQRCodeImage(bufferedImage);
                    if (qrCodeData != null) {
                        result.add(qrCodeData);
                    }
                    updateProgress(i + 1, pageCount);
                    int pagesScanned = i + 1;
                    Platform.runLater(() -> dialog.setHeaderText("Scanning QR Code (" + pagesScanned + "/" + pageCount + ")"));
                }
                pdfDocument.close();
                return result;
            }
        };
        createProgressDialog(task);
        task.setOnSucceeded(e -> {
            dialog.close();
            ObservableList<String> data = FXCollections.observableArrayList(task.getValue());
            tableView.setItems(data);
        });
        task.setOnFailed(e -> {
            dialog.close();
            Throwable exception = task.getException();
            Alert alert = new Alert(Alert.AlertType.ERROR, exception.getMessage());
            alert.showAndWait();
        });

        dialog.setOnCloseRequest(e -> {
            task.cancel();
        });
        dialog.show();

        new Thread(task).start();
    }

    private static String scanQRCodeImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        try {
            String result = decodeQRCode(bufferedImage);
            if (result != null) {
                return result;
            }
        } catch (Exception e) {
            logger.warn("Error decoding QR code from full image", e);
        }

        int horizontal = 2;
        for (int i = 0; i < horizontal; i++) {
            int startX = i * (width / horizontal);
            int regionWidth = (i == horizontal - 1) ? (width - startX) : (width / horizontal);
            BufferedImage region = bufferedImage.getSubimage(startX, 0, regionWidth, height);
            try {
                String result = decodeQRCode(region);
                if (result != null) {
                    return result;
                }
            } catch (Exception e) {
                logger.warn("Error decoding QR code from 1/2 width image.", e);
            }
        }

        for (int i = 0; i < horizontal; i++) {
            int startY = i * (height / horizontal);
            int regionHeight = (i == horizontal - 1) ? (height - startY) : (height / horizontal);
            BufferedImage region = bufferedImage.getSubimage(0, startY, width, regionHeight);
            try {
                String result = decodeQRCode(region);
                if (result != null) {
                    return result;
                }
            } catch (Exception e) {
                logger.warn("Error decoding QR code from 1/2 height image.", e);
            }
        }

        int numRegions = 4;
        for (int i = 0; i < numRegions; i++) {
            for (int j = 0; j < numRegions; j++) {
                int startX = i * (width / numRegions);
                int startY = j * (height / numRegions);
                int regionWidth = (i == numRegions - 1) ? (width - startX) : (width / numRegions);
                int regionHeight = (j == numRegions - 1) ? (height - startY) : (height / numRegions);

                BufferedImage region = bufferedImage.getSubimage(startX, startY, regionWidth, regionHeight);
                try {
                    String result = decodeQRCode(region);
                    if (result != null) {
                        return result;
                    }
                } catch (Exception e) {
                    logger.warn("Error decoding QR code from 1/4 image", e);
                }
            }
        }

        return null;
    }

    private static String decodeQRCode(BufferedImage bufferedImage) throws ChecksumException, NotFoundException, FormatException {
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        Reader reader = new MultiFormatReader();
        Result result = reader.decode(binaryBitmap);
        return result.getText();
    }

    private static void createProgressDialog(Task<?> task) {
        dialog = new Dialog<>();
        dialog.setTitle("QR Code Scanner");
        dialog.setHeaderText("Scanning QR Code...");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(400);
        progressBar.setPrefHeight(30);
        progressBar.progressProperty().bind(task.progressProperty());

        VBox dialogContent = new VBox(progressBar);
        dialog.getDialogPane().setContent(dialogContent);

        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setDisable(true);

        task.setOnSucceeded(e -> dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setDisable(false));
        task.setOnFailed(e -> dialog.getDialogPane().lookupButton(ButtonType.CLOSE).setDisable(false));
    }
}
