package qupath.lib.cpusampler.sampler;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *     A class that repetitively samples the status of every thread in a background thread.
 * </p>
 * <p>
 *     The sampling information is returned through a tree of {@link StackFrameNode}. Each direct
 *     child of the tree's root represents a thread, and each descendant of each thread represents
 *     a stack frame.
 * </p>
 * <p>
 *     The thread performing the sampling is not considered by the sampling process.
 * </p>
 * <p>
 *     An instance of this class must be {@link #close() closed} once no longer used.
 * </p>
 */
public class CPUSampler implements AutoCloseable {

    private static final int SAMPLING_DELAY_MILLISECONDS = 50;
    private static final String SAMPLING_THREAD_NAME = "CPU sampler";
    private final Collection<String> threadsToNotTrack = new HashSet<>();
    private final BooleanProperty running = new SimpleBooleanProperty(true);
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, SAMPLING_THREAD_NAME));
    private final StackFrameNode root = new StackFrameNode("root", SAMPLING_DELAY_MILLISECONDS);

    /**
     * Creates a new CPU sampler.
     *
     * @param threadsToNotTrack  a list of thread names not to track
     */
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

    /**
     * Get the root of the tree of {@link StackFrameNode} of this sampler (as explained in
     * the class declaration). The root has no useful data except for its children which represent
     * threads.
     *
     * @return the root of the tree
     */
    public StackFrameNode getRoot() {
        return root;
    }

    /**
     * Change the running state of this sampler (resume if sampling is paused,
     * pause otherwise).
     */
    public void changeRunningState() {
        running.set(!running.get());
    }

    /**
     * @return whether the sampling is active
     */
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
                StackFrameNode stackFrameNode = root.getOrCreateChildWithNameAndAddUsage(entry.getKey().getName());

                for (int i=entry.getValue().length-1; i>=0; i--) {
                    stackFrameNode = stackFrameNode.getOrCreateChildWithNameAndAddUsage(entry.getValue()[i].toString());
                }
            }

            root.getChildren().stream()
                    .filter(item -> item.getName().equals(entry.getKey().getName()))
                    .findAny()
                    .ifPresent(stackFrameNode -> stackFrameNode.setState(entry.getKey().getState()));
        }
    }
}
