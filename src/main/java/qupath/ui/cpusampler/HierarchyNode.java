package qupath.ui.cpusampler;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class HierarchyNode extends TreeItem<Node> {

    private final ObservableList<HierarchyNode> children = FXCollections.observableArrayList();
    private final ObservableList<Thread.State> statesToDisplay;

    public HierarchyNode(
            Node node,
            ObservableList<Thread.State> statesToDisplay
    ) {
        super(node);

        this.statesToDisplay = statesToDisplay;

        statesToDisplay.addListener((ListChangeListener<? super Thread.State>) change -> filterChildren());
    }

    @Override
    public boolean isLeaf() {
        return getValue().getChildren().isEmpty();
    }

    public void update() {
        for (Node node: getValue().getChildren()) {
            boolean alreadyCreated = false;
            for (HierarchyNode child: children) {
                if (child.getValue().equals(node)) {
                    alreadyCreated = true;
                }
            }

            if (!alreadyCreated) {
                children.add(new HierarchyNode(node, statesToDisplay));
            }
        }

        filterChildren();

        for (HierarchyNode hierarchyNode: children) {
            hierarchyNode.update();
        }
    }

    private void filterChildren() {
        getChildren().setAll(children.stream().filter(item -> item.getValue().getState() == null || statesToDisplay.contains(item.getValue().getState())).toList());
    }
}
