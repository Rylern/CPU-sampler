package qupath.lib.cpusampler.gui;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.CheckComboBox;
import qupath.lib.cpusampler.CpuSamplerExtension;
import qupath.lib.cpusampler.sampler.CPUSampler;
import qupath.lib.cpusampler.sampler.StackFrameNode;

import java.io.IOException;
import java.util.*;

/**
 * <p>
 *     A window that displays all active / non active threads and stack traces of
 *     the current application.
 * </p>
 * <p>
 *     It is possible to filter threads by their name, status, and the total time each stack
 *     trace is in the {@code RUNNING} status is counted.
 * </p>
 * <p>
 *     The window displays a table in which cells are represented by the {@link StackFrameItem} class.
 * </p>
 * <p>
 *     This window must be {@link #close() closed} once no longer used.
 * </p>
 */
public class CPUSamplerViewer extends Stage implements AutoCloseable {

    private static final ResourceBundle resources = CpuSamplerExtension.getResources();
    private static final int REFRESH_RATE_MILLISECONDS = 1000;
    private final CPUSampler cpuSampler;
    private StackFrameItem rootItem;
    @FXML private Button pausePlay;
    @FXML private CheckComboBox<Thread.State> threadStates;
    @FXML private TreeTableView<StackFrameNode> tree;
    @FXML private TreeTableColumn<StackFrameNode, StackFrameNode> stackTraceColumn;
    @FXML private TreeTableColumn<StackFrameNode, Thread.State> stateColumn;
    @FXML private TreeTableColumn<StackFrameNode, Number> totalTimeColumn;

    /**
     * Creates the CPU sampler window. This also starts the sampling in a
     * background thread.
     *
     * @param threadsToNotTrack  a list of thread names not to track
     * @param statusToTrack  the initial list of thread status to track. This
     *                       may be updated later by the user interaction
     * @throws IOException when an error occurred while creating the window
     */
    public CPUSamplerViewer(Collection<String> threadsToNotTrack, Collection<Thread.State> statusToTrack) throws IOException {
        cpuSampler = new CPUSampler(threadsToNotTrack);

        var url = CPUSamplerViewer.class.getResource("cpusampler.fxml");
        FXMLLoader loader = new FXMLLoader(url, resources);
        loader.setRoot(this);
        loader.setController(this);
        loader.load();

        setUpTopBar(statusToTrack);
        setUpTree();
        setUpRegularUpdates();
    }

    @Override
    public void close() {
        cpuSampler.close();
    }

    @FXML
    private void onPausePlayClicked() {
        cpuSampler.changeRunningState();
    }

    private void setUpRegularUpdates() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(REFRESH_RATE_MILLISECONDS), e -> update()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.playFromStart();
        cpuSampler.isRunning().addListener((p, o, n) -> Platform.runLater(() -> {
            if (n) {
                timeline.play();
            } else {
                timeline.pause();
            }
        }));
    }

    private void setUpTopBar(Collection<Thread.State> statusToTrack) {
        pausePlay.setText(resources.getString(cpuSampler.isRunning().get() ? "SamplerViewer.pause" : "SamplerViewer.resume"));
        cpuSampler.isRunning().addListener((p, o, n) -> Platform.runLater(() ->
                pausePlay.setText(resources.getString(n ? "SamplerViewer.pause" : "SamplerViewer.resume"))
        ));

        threadStates.getItems().setAll(Thread.State.values());
        for (Thread.State state: statusToTrack) {
            threadStates.getCheckModel().check(state);
        }
    }

    private void setUpTree() {
        rootItem = new StackFrameItem(
                cpuSampler.getRoot(),
                threadStates.getCheckModel().getCheckedItems()
        );
        tree.setRoot(rootItem);
        rootItem.setExpanded(true);

        stackTraceColumn.setCellValueFactory(node -> new SimpleObjectProperty<StackFrameNode>(node.getValue().getValue()));
        stateColumn.setCellValueFactory(node -> new SimpleObjectProperty<>(node.getValue().getValue().getState()));
        totalTimeColumn.setCellValueFactory(node -> new SimpleIntegerProperty(node.getValue().getValue().getTimeSpent()));

        stackTraceColumn.setCellFactory(column -> new TreeTableCell<>() {
            @Override
            public void updateItem(StackFrameNode item, boolean empty) {
                super.updateItem(item, empty);

                setText(null);
                setTooltip(null);
                getStyleClass().remove("thread-item");

                if (!empty) {
                    setText(item.getName());
                    setTooltip(new Tooltip(item.getName()));

                    if (item.getState() != null) {
                        getStyleClass().add("thread-item");
                    }
                }
            }
        });
        totalTimeColumn.setCellFactory(column -> new TreeTableCell<>() {
            @Override
            public void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setText(null);
                } else {
                    setText(String.format("%,d ms", item.intValue()));
                }
            }
        });
    }

    private void update() {
        rootItem.update();
        tree.refresh();
    }
}
