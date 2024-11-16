package com.tuandev.qrscanner;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    public TableView<String> qrCodeDataTable;
    public TableColumn<String, Integer> qrCodeDataCol1;
    public TableColumn<String, String> qrCodeDataTable2;
    public TextField filePDFField;
    public Button removeAuthBtn;

    private FileChooser fileChooser;

    public void openFilePDF(ActionEvent actionEvent) {
        if (!AuthController.getInstance().isLoggedIn) {
            this.showLoginPop();
        } else {
            fileChooser.setTitle("Open PDF");
            fileChooser.getExtensionFilters().setAll(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File filePath = fileChooser.showOpenDialog(null);
            if (filePath != null) {
                filePDFField.setText(filePath.getAbsolutePath());
                fileChooser.setInitialDirectory(filePath.getParentFile());

                QRService.scannerQRCode(filePath, qrCodeDataTable);
            }
        }
    }

    public void onExport(ActionEvent actionEvent) {
        if (!AuthController.getInstance().isLoggedIn) {
            this.showLoginPop();
        } else {
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
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        try {
            String accessKey = AuthController.getInstance().readKey();
            if (accessKey != null && AuthController.getInstance().verifyAccessKey(accessKey)) {
                AuthController.getInstance().isLoggedIn = true;
                removeAuthBtn.setDisable(true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fileChooser = new FileChooser();

        qrCodeDataCol1.setCellValueFactory(data -> {
            int index = qrCodeDataTable.getItems().indexOf(data.getValue());
            return new SimpleIntegerProperty(index + 1).asObject();
        });
        qrCodeDataTable2.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()));
    }

    private void showLoginPop() {
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(10.0, 10.0, 10.0, 10.0));
        vBox.setSpacing(10.0);
        vBox.setPrefWidth(300.0);
        Label keyLabel = new Label("Access Key:");
        vBox.getChildren().add(keyLabel);
        PasswordField keyField = new PasswordField();
        keyField.setPromptText("••••••••••");
        vBox.getChildren().add(keyField);
        CheckBox rememberCheckBox = new CheckBox("Remember me");
        vBox.getChildren().add(rememberCheckBox);
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().setContent(vBox);
        alert.setTitle("Login");
        Objects.requireNonNull(keyField);
        Platform.runLater(keyField::requestFocus);
        Optional<ButtonType> result = alert.showAndWait();
        result.ifPresent((buttonType) -> {
            if (buttonType == ButtonType.OK) {
                String key = keyField.getText();
                boolean remember = rememberCheckBox.isSelected();
                if (key.equals("HongRancho")) {
                    AuthController.getInstance().isLoggedIn = true;
                    removeAuthBtn.setVisible(true);
                    if (remember) {
                        try {
                            AuthController.getInstance().writeKey(key);
                        } catch (IOException var7) {
                            this.showErrorDialog(var7.getMessage());
                        }
                    }

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("You have successfully logged in!");
                    successAlert.showAndWait();
                } else {
                    this.showErrorDialog("Access Key Invalid");
                }
            }

        });
    }

    private void showErrorDialog(String message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Error");
        errorAlert.setHeaderText((String)null);
        errorAlert.setContentText(message);
        errorAlert.showAndWait();
    }

    public void onLogout(ActionEvent actionEvent) {
        try {
            AuthController.getInstance().isLoggedIn = false;
            removeAuthBtn.setVisible(true);
            AuthController.getInstance().writeKey("");
            this.showLoginPop();
        } catch (IOException var3) {
            this.showErrorDialog(var3.getMessage());
        }

    }
}
