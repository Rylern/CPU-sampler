package qupath.ui.cpusampler;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class App extends Application {
    private CPUSamplerViewer cpuSamplerViewer = null;

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        cpuSamplerViewer = new CPUSamplerViewer(List.of("Reference Handler"), List.of(Thread.State.RUNNABLE));
        Scene scene = new Scene(cpuSamplerViewer);
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        if (cpuSamplerViewer != null) {
            cpuSamplerViewer.close();
        }
    }
}
