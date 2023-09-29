package qupath.ui.cpusampler;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class Node {

    private final ObservableList<Node> children = FXCollections.observableArrayList();
    private final StringProperty timeSpentProperty = new SimpleStringProperty("");
    private final ObjectProperty<Thread.State> state = new SimpleObjectProperty<>(null);
    private final String name;
    private final int samplingDelay;
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

    public ReadOnlyStringProperty getTimeSpent() {
        return timeSpentProperty;
    }

    public void addUsage() {
        timeSpent += samplingDelay;
        timeSpentProperty.set(timeSpent + " ms");
    }

    public ObservableList<Node> getChildren() {
        return children;
    }

    public ReadOnlyObjectProperty<Thread.State> getState() {
        return state;
    }

    public void setState(Thread.State state) {
        this.state.set(state);
    }
}
