package krasa.mavenhelper.analyzer;

import java.util.Enumeration;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class TreeUtils {
	public static void expandAll(JTree rightTree) {
		expandAll(rightTree, new TreePath(((DefaultMutableTreeNode) rightTree.getModel().getRoot()).getPath()));
	}

	public static void expandAll(JTree tree, TreePath parent) {
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path);
			}
		}
		tree.expandPath(parent);
	}

	public static void nodesChanged(DefaultTreeModel rightTreeModel) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) rightTreeModel.getRoot();
		Enumeration enumeration = root.breadthFirstEnumeration();
		while (enumeration.hasMoreElements()) {
			DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) enumeration.nextElement();
			rightTreeModel.nodeChanged(defaultMutableTreeNode);
		}
	}
}
