module com.tuandev.qrscanner {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.zxing;
    requires java.desktop;
    requires org.apache.pdfbox;
    requires org.apache.pdfbox.io;
    requires org.apache.logging.log4j;
    requires org.apache.fontbox;
    requires com.zaxxer.sparsebitset;
    requires commons.math3;

    opens com.tuandev.qrscanner to javafx.fxml;
    exports com.tuandev.qrscanner;
}