module qupath.ui.cpusampler {
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires org.controlsfx.controls;

    opens qupath.ui.cpusampler to javafx.graphics, javafx.fxml;
}
