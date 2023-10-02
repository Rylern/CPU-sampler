package qupath.ui.cpusampler;

import java.util.ArrayList;
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

    public synchronized String getTimeSpent() {
        return timeSpent + " ms";
    }

    public synchronized void addUsage() {
        timeSpent += samplingDelay;
    }

    public synchronized List<Node> getChildren() {
        return children;
    }

    public synchronized Thread.State getState() {
        return state;
    }

    public synchronized void setState(Thread.State state) {
        this.state = state;
    }
}
