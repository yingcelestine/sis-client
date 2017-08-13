package org.apache.sis.desktop;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyEvent;
import org.apache.sis.util.collection.TableColumn;
import static org.apache.sis.util.collection.TableColumn.IDENTIFIER;
import static org.apache.sis.util.collection.TableColumn.NAME;
import static org.apache.sis.util.collection.TableColumn.VALUE;
import static org.apache.sis.util.collection.TableColumn.VALUE_AS_TEXT;
import org.apache.sis.util.collection.TreeTable;

/**
 *
 * @author Siddhesh Rane
 */
public class NodeTreeTable extends TreeTableView<TreeTable.Node> {

    public static final Predicate<TreeTable.Node> NON_EMPTY_LEAF = t -> !t.isLeaf() || t.getValue(VALUE) != null;
    public static final Predicate<TreeTable.Node> EXPAND_SINGLE_CHILD = node -> node.getChildren().size() == 1 || node.getParent() == null;

    private TreeTable treeTable;
    protected TreeTableColumn<TreeTable.Node, String> idColumn;
    protected TreeTableColumn<TreeTable.Node, String> nameColumn;
    protected TreeTableColumn<TreeTable.Node, TreeTable.Node> valueColumn;
    protected TreeTableColumn<TreeTable.Node, String> textColumn;

    private final ObjectProperty<Predicate<TreeTable.Node>> createTreeItemForNodeProperty = new SimpleObjectProperty<>(NON_EMPTY_LEAF);
    /**
     * A property containing predicate that returns true if the given
     * {@link TreeTable.Node} must be shown as a {@link TreeItem} in the
     * {@link TreeTableView}. By default return true for all nodes unless
     * preferences are loaded
     *
     * @return The property containing the expansion predicate
     */
    public ObjectProperty<Predicate<TreeTable.Node>> createTreeItemForNodeProperty() {
        return createTreeItemForNodeProperty;
    }
    public Predicate<TreeTable.Node> getCreateTreeItemForNode() {
        return createTreeItemForNodeProperty.get();
    }
    public void setCreateTreeItemForNode(Predicate<TreeTable.Node> createTreeItemForNode) {
        createTreeItemForNodeProperty.set(createTreeItemForNode);
    }

    private final SimpleObjectProperty<Predicate<TreeTable.Node>> expandNodeProperty = new SimpleObjectProperty<>(EXPAND_SINGLE_CHILD);
    /**
     * A property containing predicate that returns true if the given
     * {@link TreeTable.Node} must be expanded in the {@link TreeTableView} to
     * show its children by default.
     *
     * @return The property containing the expansion predicate
     */
    public ObjectProperty<Predicate<TreeTable.Node>> expandNodeProperty() {
        return expandNodeProperty;
    }
    public Predicate<TreeTable.Node> getExpandNode() {
        return expandNodeProperty.get();
    }
    public void setExpandNode(Predicate<TreeTable.Node> expandNode) {
        expandNodeProperty.set(expandNode);
    }
    private void expandNodes(TreeItem<TreeTable.Node> root) {
        if (root == null || root.isLeaf()) {
            return;
        }
        root.setExpanded(getExpandNode().test(root.getValue()));
        for (TreeItem<TreeTable.Node> child : root.getChildren()) {
            expandNodes(child);
        }
    }

    public NodeTreeTable() {
        this(null);
    }
    public NodeTreeTable(TreeTable treeTable) {
        this.treeTable = treeTable;
        expandNodeProperty.addListener(ob -> expandNodes(getRoot()));
        addEventHandler(KeyEvent.KEY_PRESSED, shitfUpDown);
        createTableColumns();
        setTreeTable(treeTable);
    }

    /**
     * Creates {@link TableColumn}s for name, identifier, value valueastext, but
     * doesnt add them to the table. These columns can later be added according
     * what all is contained by the TreeTable.
     */
    private void createTableColumns() {
        //IDENTIFIER column
        idColumn = new TreeTableColumn<>(IDENTIFIER.getHeader().toString());
        idColumn.setCellValueFactory((param) -> {
            String value = param.getValue().getValue().getValue(IDENTIFIER);
            return new SimpleStringProperty(Objects.toString(value, ""));
        });

        //NAME column
        nameColumn = new TreeTableColumn<>(NAME.getHeader().toString());
        nameColumn.setCellValueFactory((param) -> {
            String value = param.getValue().getValue().getValue(NAME).toString();
            return new SimpleStringProperty(Objects.toString(value, ""));
        });

        //TEXT column
        textColumn = new TreeTableColumn<>(VALUE_AS_TEXT.getHeader().toString());
        textColumn.setCellValueFactory((param) -> {
            CharSequence value = param.getValue().getValue().getValue(VALUE_AS_TEXT);
            return new SimpleStringProperty(Objects.toString(value, ""));
        });

        //VALUE column
        valueColumn = new TreeTableColumn<>(VALUE.getHeader().toString());
        valueColumn.setCellValueFactory((param) -> {
            TreeTable.Node value = param.getValue().getValue();
            return new SimpleObjectProperty(value);
        });
    }

    public TreeTable getTreeTable() {
        return treeTable;
    }
    public void setTreeTable(TreeTable treeTable) {
        this.treeTable = treeTable;
        if (treeTable == null) {
            setRoot(null);
            return;
        }

        List<TableColumn<?>> columns = treeTable.getColumns();
        if (columns.contains(NAME) && nameColumn.getTreeTableView() == null) {
            getColumns().add(nameColumn);
        } else {
            getColumns().remove(nameColumn);
        }
        if (columns.contains(IDENTIFIER) && idColumn.getTreeTableView() == null) {
            getColumns().add(idColumn);
        } else {
            getColumns().remove(idColumn);
        }
        if (columns.contains(VALUE) && valueColumn.getTreeTableView() == null) {
            getColumns().add(valueColumn);
        } else {
            getColumns().remove(valueColumn);
        }
        if (columns.contains(VALUE_AS_TEXT) && textColumn.getTreeTableView() == null) {
            getColumns().add(textColumn);
        } else {
            getColumns().remove(textColumn);
        }
        TreeItem<TreeTable.Node> rootItem = new TreeItem<>(treeTable.getRoot());
        createTreeItems(rootItem, treeTable.getRoot().getChildren());
        rootItem.setExpanded(true);
        setRoot(rootItem);
    }

    private void createTreeItems(TreeItem<TreeTable.Node> rootItem, Collection<TreeTable.Node> children) {
        for (TreeTable.Node node : children) {
            TreeItem parent = rootItem;
            //include this node in the tree table view?
            if (getCreateTreeItemForNode().test(node)) {
                parent = new TreeItem(node);
                parent.setExpanded(getExpandNode().test(node));
                rootItem.getChildren().add(parent);
            }
            if (!node.isLeaf()) {
                createTreeItems(parent, node.getChildren());
            }
        }
    }

    /*
     * Modification operations
     */
    public void moveChildDown(TreeItem<TreeTable.Node> item) {
        TreeItem<TreeTable.Node> parent = item.getParent();
        TreeItem<TreeTable.Node> nextSibling = item.nextSibling();
        if (parent == null || nextSibling == null) {
            return;
        }
        parent.getChildren().remove(item);
        int index = parent.getChildren().indexOf(nextSibling);
        parent.getChildren().add(index + 1, item);
        getFocusModel().focusNext();
    }
    public void moveChildUp(TreeItem<TreeTable.Node> item) {
        TreeItem<TreeTable.Node> parent = item.getParent();
        TreeItem<TreeTable.Node> previousSibling = item.previousSibling();
        if (parent == null || previousSibling == null) {
            return;
        }
        parent.getChildren().remove(previousSibling);
        int insertHere = parent.getChildren().indexOf(item);
        parent.getChildren().add(insertHere + 1, previousSibling);
        getFocusModel().focusPrevious();
    }
    public void flattenSubTree(TreeItem<TreeTable.Node> selectedItem) {
        if (selectedItem.getParent() == null || selectedItem.getChildren().isEmpty()) {
            return;
        }
        int indexOf = selectedItem.getParent().getChildren().indexOf(selectedItem);
        selectedItem.getParent().getChildren().addAll(indexOf, selectedItem.getChildren());
        selectedItem.getParent().getChildren().remove(selectedItem);
    }
    EventHandler<KeyEvent> shitfUpDown = ke -> {
        //Row reordering by shift+arrow keys
        if (!ke.isShiftDown() || !ke.getCode().isArrowKey()) {
            return;
        }
        TreeItem<TreeTable.Node> item = getSelectionModel().getSelectedItem();
        switch (ke.getCode()) {
            case UP:
            case KP_UP:
                moveChildUp(item);
                break;
            case DOWN:
            case KP_DOWN:
                moveChildDown(item);
                break;
        }
        ke.consume();
    };
}
