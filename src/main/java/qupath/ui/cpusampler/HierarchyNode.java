package qupath.ui.cpusampler;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.TreeItem;

import java.util.function.Predicate;

public class HierarchyNode extends TreeItem<Node> {

    private final ObservableList<HierarchyNode> children = FXCollections.observableArrayList();
    private final FilteredList<HierarchyNode> filteredChildren = new FilteredList<>(children);

    public HierarchyNode(
            Node node,
            ObservableList<Thread.State> statusToDisplay
    ) {
        super(node);

        Bindings.bindContent(getChildren(), filteredChildren);

        ChangeListener<Thread.State> listener = (p, o, n) ->
                filteredChildren.setPredicate(item -> statusToDisplay.contains(item.getValue().getState().get()));

        children.addListener((ListChangeListener<? super HierarchyNode>) change -> {
            for (HierarchyNode hierarchyNode: children) {
                hierarchyNode.getValue().getState().removeListener(listener);   // Ensure that only one instance of the listener is added
                hierarchyNode.getValue().getState().addListener(listener);
            }
        });

        statusToDisplay.addListener((ListChangeListener<? super Thread.State>) change -> {
            filteredChildren.setPredicate(item -> item.getValue().getState().get() == null || statusToDisplay.contains(item.getValue().getState().get()));
        });

        children.setAll(getValue().getChildren().stream().map(n -> new HierarchyNode(n, statusToDisplay)).toList());
        getValue().getChildren().addListener((ListChangeListener<? super Node>) change ->
                children.setAll(change.getList().stream().map(n -> new HierarchyNode(n, statusToDisplay)).toList())
        );
    }

    @Override
    public boolean isLeaf() {
        return getValue().getChildren().isEmpty();
    }
}
