package qupath.ui.cpusampler;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.util.Duration;

import java.util.*;

public class CPUSampler {

    private static final int SAMPLING_DELAY_MILLISECONDS = 10;
    private final List<String> threadsToNotTrack;
    private final BooleanProperty running = new SimpleBooleanProperty(true);
    private final Timeline timeline;
    private final Node root = new Node("root", SAMPLING_DELAY_MILLISECONDS);

    public CPUSampler(List<String> threadsToNotTrack) {
        this.threadsToNotTrack = threadsToNotTrack;

        timeline = new Timeline(new KeyFrame(Duration.millis(SAMPLING_DELAY_MILLISECONDS), e -> updateNodes()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public Node getRoot() {
        return root;
    }

    public void changeRunningState() {
        running.set(!running.get());

        if (running.get()) {
            timeline.play();
        } else {
            timeline.pause();
        }
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
