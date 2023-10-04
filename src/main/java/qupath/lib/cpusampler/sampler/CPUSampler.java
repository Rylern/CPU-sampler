package qupath.lib.cpusampler.sampler;

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
    private final Collection<String> threadsToNotTrack = new HashSet<>();
    private final BooleanProperty running = new SimpleBooleanProperty(true);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, SAMPLING_THREAD_NAME));
    private final Node root = new Node("root", SAMPLING_DELAY_MILLISECONDS);

    public CPUSampler(Collection<String> threadsToNotTrack) {
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
        executor.shutdown();
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
                Node node = root.getOrCreateChildWithName(entry.getKey().getName());

                for (int i=entry.getValue().length-1; i>=0; i--) {
                    node = node.getOrCreateChildWithName(entry.getValue()[i].toString());
                }
            }

            root.getChildren().stream()
                    .filter(item -> item.getName().equals(entry.getKey().getName()))
                    .findAny()
                    .ifPresent(node -> node.setState(entry.getKey().getState()));
        }
    }
}
