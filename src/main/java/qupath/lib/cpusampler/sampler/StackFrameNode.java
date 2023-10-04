package qupath.lib.cpusampler.sampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p>
 *     A node of a tree that can represent a thread or a stack frame.
 * </p>
 */
public class StackFrameNode {

    private final List<StackFrameNode> children = new ArrayList<>();
    private final String name;
    private final int samplingDelay;
    private Thread.State state = null;
    private int timeSpent = 0;

    /**
     * Creates a new stack frame node.
     *
     * @param name  the name of this node
     * @param samplingDelay  the delay (in ms) between each sampling
     */
    public StackFrameNode(String name, int samplingDelay) {
        this.name = name;
        this.samplingDelay = samplingDelay;
    }

    @Override
    public String toString() {
        return String.format("Node %s spent %d ms busy with %d children", name, timeSpent, children.size());
    }

    /**
     * @return the name of this node
     */
    public String getName() {
        return name;
    }

    /**
     * @return the total time this node was spent busy
     */
    public synchronized int getTimeSpent() {
        return timeSpent;
    }

    /**
     * @return an unmodifiable list containing the children of this node
     */
    public synchronized List<StackFrameNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * @return the current thread state of this stack frame
     */
    public synchronized Thread.State getState() {
        return state;
    }

    /**
     * Set the thread state of this node
     *
     * @param state  the new thread state of this node
     */
    public synchronized void setState(Thread.State state) {
        this.state = state;
    }

    /**
     * Indicate that a child of this node (with the provided name) was sampled.
     * If no child with such name exists, it is first created and added to this
     * node's children.
     *
     * @param childName  the name of the child that was sampled
     * @return the child that was sampled
     */
    public synchronized StackFrameNode getOrCreateChildWithNameAndAddUsage(String childName) {
        var existingNode = children.stream()
                .filter(item -> item.getName().equals(childName))
                .findAny();

        StackFrameNode stackFrameNode;
        if (existingNode.isPresent()) {
            stackFrameNode = existingNode.get();
        } else {
            stackFrameNode = new StackFrameNode(childName, samplingDelay);
            children.add(stackFrameNode);
        }
        stackFrameNode.addUsage();

        return stackFrameNode;
    }

    private synchronized void addUsage() {
        timeSpent += samplingDelay;
    }
}
