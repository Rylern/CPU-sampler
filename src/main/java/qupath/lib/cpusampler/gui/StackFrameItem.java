package qupath.lib.cpusampler.gui;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import qupath.lib.cpusampler.sampler.StackFrameNode;

/**
 * <p>
 *     A {@link TreeItem} displaying a {@link StackFrameNode}.
 * </p>
 * <p>
 *     This item doesn't automatically retrieve the latest data from its corresponding
 *     StackFrameNode, the {@link #update()} function must be called for it to happen.
 * </p>
 */
class StackFrameItem extends TreeItem<StackFrameNode> {

    private final ObservableList<StackFrameItem> children = FXCollections.observableArrayList();
    private final ObservableList<Thread.State> statesToDisplay;

    /**
     * Creates a new StackFrameItem.
     *
     * @param stackFrameNode  the corresponding StackFrameNode
     * @param statesToDisplay  an observable list indicating the thread states to display. The children
     *                         of this item with a thread status not contained in this list won't be
     *                         displayed
     */
    public StackFrameItem(StackFrameNode stackFrameNode, ObservableList<Thread.State> statesToDisplay) {
        super(stackFrameNode);

        this.statesToDisplay = statesToDisplay;
        statesToDisplay.addListener((ListChangeListener<? super Thread.State>) change -> filterChildren());
    }

    private StackFrameItem(StackFrameNode stackFrameNode) {
        super(stackFrameNode);

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

    @Override
    public boolean isLeaf() {
        return getValue().getChildren().isEmpty();
    }

    /**
     * Update this item's data with the information of the corresponding StackFrameNode.
     * This will also update every child of this item.
     */
    public void update() {
        if (isExpanded()) {
            for (StackFrameNode stackFrameNode : getValue().getChildren()) {
                if (children.stream().noneMatch(child -> child.getValue().equals(stackFrameNode))) {
                    children.add(new StackFrameItem(stackFrameNode));
                }
            }

            filterChildren();

            for (StackFrameItem stackFrameItem : children) {
                stackFrameItem.update();
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
