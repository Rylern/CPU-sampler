package qupath.ui.cpusampler;

import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckComboBox;

import java.io.IOException;
import java.util.*;

public class CPUSamplerViewer extends VBox {

    private static final ResourceBundle resources = ResourceBundle.getBundle("qupath.ui.cpusampler.strings");
    private final CPUSampler cpuSampler;
    @FXML private Button pausePlay;
    @FXML private CheckComboBox<Thread.State> threadStatus;
    @FXML private TreeTableView<Node> tree;
    @FXML private TreeTableColumn<Node, String> stackTraceColumn;
    @FXML private TreeTableColumn<Node, Thread.State> stateColumn;
    @FXML private TreeTableColumn<Node, String> totalTimeColumn;
    private Thread thread;

    public CPUSamplerViewer(List<String> threadsToNotTrack, List<Thread.State> statusToTrack) throws IOException {
        cpuSampler = new CPUSampler(threadsToNotTrack);

        var url = CPUSamplerViewer.class.getResource("cpusampler.fxml");
        FXMLLoader loader = new FXMLLoader(url, resources);
        loader.setRoot(this);
        loader.setController(this);
        loader.load();

        pausePlay.textProperty().bind(Bindings.when(cpuSampler.isRunning())
                .then("Pause")
                .otherwise("Unpause")
        );

        threadStatus.getItems().setAll(Thread.State.values());
        for (Thread.State state: statusToTrack) {
            threadStatus.getCheckModel().check(state);
        }

        HierarchyNode rootItem = new HierarchyNode(
                cpuSampler.getRoot(),
                threadStatus.getCheckModel().getCheckedItems()
        );
        tree.setRoot(rootItem);
        rootItem.setExpanded(true);
        stackTraceColumn.setCellValueFactory(node -> new SimpleStringProperty(node.getValue().getValue().getName()));
        stateColumn.setCellValueFactory(node -> node.getValue().getValue().getState());
        totalTimeColumn.setCellValueFactory(node -> node.getValue().getValue().getTimeSpent());
    }

    @FXML
    private void onPausePlayClicked() {
        cpuSampler.changeRunningState();
    }

    @FXML
    private void onTest() {
        if (thread == null) {
            thread = new Thread(() -> {
                while (true) {
                    System.out.println("Infinite loop");
                }
            });
            thread.setName("Infinite loop");
            thread.start();
        } else {
            thread.stop();
            thread = null;
        }
    }
}
