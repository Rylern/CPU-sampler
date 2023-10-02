package qupath.ui.cpusampler;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.controlsfx.control.CheckComboBox;

import java.io.IOException;
import java.util.*;

public class CPUSamplerViewer extends VBox implements AutoCloseable {

    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ui.cpusampler.strings");
    private static final int REFRESH_RATE_SECONDS = 1;
    private final CPUSampler cpuSampler;
    private final HierarchyNode rootItem;
    @FXML private Button pausePlay;
    @FXML private CheckComboBox<Thread.State> threadStates;
    @FXML private TreeTableView<Node> tree;
    @FXML private TreeTableColumn<Node, String> stackTraceColumn;
    @FXML private TreeTableColumn<Node, Thread.State> stateColumn;
    @FXML private TreeTableColumn<Node, String> totalTimeColumn;

    public CPUSamplerViewer(List<String> threadsToNotTrack, List<Thread.State> statusToTrack) throws IOException {
        cpuSampler = new CPUSampler(threadsToNotTrack);

        var url = CPUSamplerViewer.class.getResource("cpusampler.fxml");
        FXMLLoader loader = new FXMLLoader(url, resources);
        loader.setRoot(this);
        loader.setController(this);
        loader.load();

        pausePlay.setText(cpuSampler.isRunning().get() ? "Pause" : "Unpause");
        cpuSampler.isRunning().addListener((p, o, n) -> Platform.runLater(() ->
                pausePlay.setText(n ? "Pause" : "Unpause")
        ));

        threadStates.getItems().setAll(Thread.State.values());
        for (Thread.State state: statusToTrack) {
            threadStates.getCheckModel().check(state);
        }

        rootItem = new HierarchyNode(
                cpuSampler.getRoot(),
                threadStates.getCheckModel().getCheckedItems()
        );
        tree.setRoot(rootItem);
        rootItem.setExpanded(true);
        stackTraceColumn.setCellValueFactory(node -> new SimpleStringProperty(node.getValue().getValue().getName()));
        stateColumn.setCellValueFactory(node -> new SimpleObjectProperty<>(node.getValue().getValue().getState()));
        totalTimeColumn.setCellValueFactory(node -> new SimpleStringProperty(node.getValue().getValue().getTimeSpent()));

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(REFRESH_RATE_SECONDS), e -> update()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.playFromStart();
        cpuSampler.isRunning().addListener((p, o, n) -> Platform.runLater(() -> {
            if (n) {
                timeline.play();
            } else {
                timeline.pause();
            }
        }));

        update();
    }

    @Override
    public void close() {
        cpuSampler.close();
    }

    @FXML
    private void onPausePlayClicked() {
        cpuSampler.changeRunningState();
    }

    private void update() {
        rootItem.update();
        tree.refresh();
    }
}
