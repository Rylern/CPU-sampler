module qupath.ui.cpusampler {
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires org.controlsfx.controls;
    requires org.slf4j;
    requires io.github.qupath.gui.fx;
    requires io.github.qupath.core;

    opens qupath.lib.cpusampler to javafx.graphics, javafx.fxml;
    opens qupath.lib.cpusampler.gui to javafx.fxml, javafx.graphics;
    opens qupath.lib.cpusampler.sampler to javafx.fxml, javafx.graphics;
}
