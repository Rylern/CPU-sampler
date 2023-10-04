package qupath.lib.cpusampler.sampler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Node {

    private final List<Node> children = new ArrayList<>();
    private final String name;
    private final int samplingDelay;
    private Thread.State state = null;
    private int timeSpent = 0;

    public Node(String name, int samplingDelay) {
        this.name = name;
        this.samplingDelay = samplingDelay;
    }

    @Override
    public String toString() {
        return String.format("Node %s spent %d ms busy with %d children", name, timeSpent, children.size());
    }

    public String getName() {
        return name;
    }

    public synchronized int getTimeSpentMs() {
        return timeSpent;
    }

    public synchronized void addUsage() {
        timeSpent += samplingDelay;
    }

    public synchronized List<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public synchronized Thread.State getState() {
        return state;
    }

    public synchronized void setState(Thread.State state) {
        this.state = state;
    }

    public synchronized Node getOrCreateChildWithName(String childName) {
        var existingNode = children.stream()
                .filter(item -> item.getName().equals(childName))
                .findAny();

        Node node;
        if (existingNode.isPresent()) {
            node = existingNode.get();
        } else {
            node = new Node(childName, samplingDelay);
            children.add(node);
        }
        node.addUsage();

        return node;
    }
}
