package qupath.ui.cpusampler;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CPUSampler implements AutoCloseable {

    private static final int SAMPLING_DELAY_MILLISECONDS = 50;
    private static final String SAMPLING_THREAD_NAME = "CPU sampler";
    private final List<String> threadsToNotTrack = new ArrayList<>();
    private final BooleanProperty running = new SimpleBooleanProperty(true);
    private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, r -> new Thread(r, SAMPLING_THREAD_NAME));
    private final Node root = new Node("root", SAMPLING_DELAY_MILLISECONDS);

    public CPUSampler(List<String> threadsToNotTrack) {
        this.threadsToNotTrack.add(SAMPLING_THREAD_NAME);
        this.threadsToNotTrack.addAll(threadsToNotTrack);

        executor.scheduleAtFixedRate(
                () -> {
                    if (running.get()) {
                        updateNodes();
                    }
                },
                0,
                SAMPLING_DELAY_MILLISECONDS,
                TimeUnit.MILLISECONDS
        );
    }

    @Override
    public void close() {
        executor.close();
    }

    public Node getRoot() {
        return root;
    }

    public void changeRunningState() {
        running.set(!running.get());
    }

    public ReadOnlyBooleanProperty isRunning() {
        return running;
    }

    private void updateNodes() {
        for (Map.Entry<Thread, StackTraceElement[]> entry: Thread.getAllStackTraces().entrySet()) {
            if (
                    entry.getValue().length > 0 &&
                            entry.getKey().getState().equals(Thread.State.RUNNABLE) &&
                            !this.threadsToNotTrack.contains(entry.getKey().getName())
            ) {
                Node node = updateNode(root.getChildren(), entry.getKey().getName());

                for (StackTraceElement stackTraceElement: entry.getValue()) {
                    node = updateNode(node.getChildren(), stackTraceElement.toString());
                }
            }

            root.getChildren().stream()
                    .filter(item -> item.getName().equals(entry.getKey().getName()))
                    .findAny()
                    .ifPresent(node -> node.setState(entry.getKey().getState()));
        }
    }

    private Node updateNode(List<Node> nodes, String elementName) {
        var existingNode = nodes.stream()
                .filter(item -> item.getName().equals(elementName))
                .findAny();

        Node node;
        if (existingNode.isPresent()) {
            node = existingNode.get();
        } else {
            node = new Node(elementName, SAMPLING_DELAY_MILLISECONDS);
            nodes.add(node);
        }
        node.addUsage();

        return node;
    }
}
