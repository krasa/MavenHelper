package krasa.mavenhelper.analyzer;

import org.jetbrains.idea.maven.model.MavenArtifactNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

public class TreeUtils {
	public static void expandAll(JTree rightTree) {
		expandAll(rightTree, new TreePath(((DefaultMutableTreeNode) rightTree.getModel().getRoot()).getPath()));
	}

	public static void expandAll(JTree tree, TreePath parent) {
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements(); ) {
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

	public static boolean selectRows(MyHighlightingTree leftTree, DefaultMutableTreeNode root, MavenArtifactNode myArtifact) {
		MyTreeUserObject userObject = (MyTreeUserObject) root.getUserObject();
		if (userObject != null && userObject.getMavenArtifactNode().equals(myArtifact)) {
			leftTree.getSelectionModel().addSelectionPath(new TreePath(root.getPath()));
			return true;
		}

		Enumeration<TreeNode> children = root.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) children.nextElement();
			if (selectRows(leftTree, treeNode, myArtifact)) {
				return true;
			}
		}
		return false;
	}

	public static String sortByVersion(List<MavenArtifactNode> value) {
		Collections.sort(value, new Comparator<MavenArtifactNode>() {
			@Override
			public int compare(MavenArtifactNode o1, MavenArtifactNode o2) {
				DefaultArtifactVersion version = new DefaultArtifactVersion(o1.getArtifact().getVersion());
				DefaultArtifactVersion version1 = new DefaultArtifactVersion(o2.getArtifact().getVersion());
				return version1.compareTo(version);
			}
		});
		return value.get(0).getArtifact().getVersion();
	}
}
