package example;

/**
 * Edited from https://coderanch.com/t/346509/java/JTree-drag-drop-tree-Java by
 * Craig Wood and mentioned on
 * https://stackoverflow.com/questions/4588109/drag-and-drop-nodes-in-jtree
 */
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

public class DandDPanel {

    private JScrollPane getContent() {
        JTree tree = new JTree();
        tree.setDragEnabled(true);
        tree.setRootVisible(false);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new DandDHandler());
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
        expandTree(tree);
        return new JScrollPane(tree);
    }

    private void expandTree(JTree tree) {
        DefaultMutableTreeNode root
                = (DefaultMutableTreeNode) tree.getModel().getRoot();
        Enumeration e = root.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node
                    = (DefaultMutableTreeNode) e.nextElement();
            if (node.isLeaf()) {
                continue;
            }
            int row = tree.getRowForPath(new TreePath(node.getPath()));
            tree.expandRow(row);
        }
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.add(new DandDPanel().getContent());
        f.setSize(400, 400);
        f.setLocation(200, 200);
        f.setVisible(true);
    }
}
