package com.tuandev.qrscanner;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    public TableView<String> qrCodeDataTable;
    public TableColumn<String, Integer> qrCodeDataCol1;
    public TableColumn<String, String> qrCodeDataTable2;
    public TextField filePDFField;

    private FileChooser fileChooser;

    public void openFilePDF(ActionEvent actionEvent) {
        fileChooser.setTitle("Open PDF");
        fileChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File filePath = fileChooser.showOpenDialog(null);
        if (filePath != null) {
            filePDFField.setText(filePath.getAbsolutePath());
            fileChooser.setInitialDirectory(filePath.getParentFile());

            QRService.scannerQRCode(filePath, qrCodeDataTable);
        }
    }

    public void onExport(ActionEvent actionEvent) {
        ObservableList<String> qrCodeDataList = qrCodeDataTable.getItems();
        if (qrCodeDataList.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("QR Code Data is empty");
            alert.showAndWait();
        } else {
            fileChooser.setTitle("Save CSV File");
            fileChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
            File excelFile = fileChooser.showSaveDialog(null);
            if (excelFile != null) {
                CSVService.writeCSVFile(excelFile, qrCodeDataList);
                fileChooser.setInitialDirectory(excelFile.getParentFile());
            }
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileChooser = new FileChooser();

        qrCodeDataCol1.setCellValueFactory(data -> {
            int index = qrCodeDataTable.getItems().indexOf(data.getValue());
            return new SimpleIntegerProperty(index + 1).asObject();
        });
        qrCodeDataTable2.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
    }
}
