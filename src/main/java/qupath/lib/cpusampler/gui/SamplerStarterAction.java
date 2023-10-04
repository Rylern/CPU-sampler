package qupath.lib.cpusampler.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qupath.lib.cpusampler.CpuSamplerExtension;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;


/**
 * <p>
 *     Action that starts a {@link CPUSamplerViewer}.
 * </p>
 * <p>
 *     If the CPU sampler viewer doesn't exist, this action creates it. Otherwise,
 *     the action will simply set the focus input to the viewer.
 * </p>
 * <p>
 *     When started, the CPU sampler viewer will only show {@code RUNNABLE} threads and
 *     ignore the {@code Reference Handler} thread.
 * </p>
 * <p>
 *     When the user closes the CPU sampler viewer, this action will automatically
 *     {@link AutoCloseable#close() close} it.
 * </p>
 */
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

    /**
     * @return the localized title of this action
     */
    public static String getActionTitle() {
        return resources.getString("SamplerStarterAction.title");
    }
}
