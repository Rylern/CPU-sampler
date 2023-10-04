package qupath.lib.cpusampler.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.cpusampler.CpuSamplerExtension;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

public class SamplerStarterAction implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(SamplerStarterAction.class);
    private static final ResourceBundle resources = CpuSamplerExtension.getResources();
    private CPUSamplerViewer cpuSamplerViewer;

    @Override
    public void run() {
        if (cpuSamplerViewer == null) {
            try {
                cpuSamplerViewer = new CPUSamplerViewer(List.of("Reference Handler"), List.of(Thread.State.RUNNABLE));
                cpuSamplerViewer.show();
                cpuSamplerViewer.setOnCloseRequest(event -> {
                    cpuSamplerViewer.close();
                    cpuSamplerViewer = null;
                });
            } catch (IOException e) {
                logger.error("Error while creating the CPU sampler viewer window", e);
            }
        } else {
            cpuSamplerViewer.show();
            cpuSamplerViewer.requestFocus();
        }
    }

    public static String getMenuTitle() {
        return resources.getString("SamplerStarterAction.title");
    }
}
