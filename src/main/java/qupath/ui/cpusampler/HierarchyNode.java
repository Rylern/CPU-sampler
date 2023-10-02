package qupath.ui.cpusampler;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class HierarchyNode extends TreeItem<Node> {

    private final ObservableList<HierarchyNode> children = FXCollections.observableArrayList();
    private final ObservableList<Thread.State> statesToDisplay;

    private HierarchyNode(Node node) {
        super(node);

        statesToDisplay = null;

        Bindings.bindContent(getChildren(), children);

        expandedProperty().addListener((p, o, n) -> {
            if (n) {
                update();

                if (children.size() == 1) {
                    children.get(0).setExpanded(true);
                }
            }
        });
    }

    public HierarchyNode(Node node, ObservableList<Thread.State> statesToDisplay) {
        super(node);

        this.statesToDisplay = statesToDisplay;
        statesToDisplay.addListener((ListChangeListener<? super Thread.State>) change -> filterChildren());
    }

    @Override
    public boolean isLeaf() {
        return getValue().getChildren().isEmpty();
    }

    public void update() {
        if (isExpanded()) {
            for (Node node: getValue().getChildren()) {
                if (children.stream().noneMatch(child -> child.getValue().equals(node))) {
                    children.add(new HierarchyNode(node));
                }
            }

            filterChildren();

            for (HierarchyNode hierarchyNode: children) {
                hierarchyNode.update();
            }
        }
    }

    private void filterChildren() {
        if (statesToDisplay != null) {
            getChildren().removeIf(item -> !statesToDisplay.contains(item.getValue().getState()));

            getChildren().addAll(children.stream().filter(item ->
                    statesToDisplay.contains(item.getValue().getState()) && !getChildren().contains(item)
            ).toList());
        }
    }
}
