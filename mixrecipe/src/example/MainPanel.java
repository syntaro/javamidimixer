// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// https://ateraimemo.com/Swing/DnDBetweenTrees.html
package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.tree.TreeSelectionModel;

public final class MainPanel extends JPanel {

    private MainPanel() {
        super(new GridLayout(1, 2));
        MainPanelHandler handler = new MainPanelHandler();
        add(new JScrollPane(makeTree(handler)));
        add(new JScrollPane(makeTree(handler)));
        setPreferredSize(new Dimension(320, 240));
    }

    private static JTree makeTree(TransferHandler handler) {
        JTree tree = new JTree();
        tree.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        tree.setRootVisible(false);
        tree.setDragEnabled(true);
        tree.setTransferHandler(handler);
        tree.setDropMode(DropMode.INSERT);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Disable node Cut action
        Action dummy = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /* Dummy action */
            }
        };
        tree.getActionMap().put(TransferHandler.getCutAction().getValue(Action.NAME), dummy);

        expandTree(tree);
        return tree;
    }

    private static void expandTree(JTree tree) {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(MainPanel::createAndShowGui);
    }

    private static void createAndShowGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException ignored) {
            Toolkit.getDefaultToolkit().beep();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            ex.printStackTrace();
            return;
        }
        JFrame frame = new JFrame("DnDBetweenTrees");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
