package krasa.mavenhelper.analyzer;

import org.jetbrains.idea.maven.model.MavenArtifactNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.*;

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

	public static void selectRows(MyHighlightingTree leftTree, MyDefaultMutableTreeNode root, MavenArtifactNode myArtifact) {
		List<MavenArtifactNode> path = new ArrayList<>();
		MavenArtifactNode node = myArtifact;
		while (node != null) {
			path.add(node);
			node = node.getParent();
			if (path.size() > 1000) {
				throw new RuntimeException(path.toString());
			}
		}
		Collections.reverse(path);
		MyDefaultMutableTreeNode matchingNode = getMatchingNode(path, root, 0);
		if (matchingNode != null && matchingNode != root) {
			leftTree.getSelectionModel().addSelectionPath(new TreePath(matchingNode.getPath()));
			leftTree.scrollPathToVisible(new TreePath(matchingNode.getPath()));
		}
	}


	public static MyDefaultMutableTreeNode getMatchingNode(List<MavenArtifactNode> path, MyDefaultMutableTreeNode root, int i) {
		if (path.size() <= i) {
			return root;
		}
		MavenArtifactNode old = path.get(i);

		Enumeration<MyDefaultMutableTreeNode> children1 = root.getChildren();
		while (children1.hasMoreElements()) {
			MyDefaultMutableTreeNode currentNode = children1.nextElement();
			MyTreeUserObject userObject = currentNode.getUserObject();
			if (userObject.getMavenArtifactNode().equals(old)) {
				return getMatchingNode(path, currentNode, i + 1);
			}
		}
		return null;
	}


	public static MyDefaultMutableTreeNode getMatchingNode(Object[] path, MyDefaultMutableTreeNode root, int i) {
		if (path.length <= i) {
			return root;
		}
		MyDefaultMutableTreeNode old = (MyDefaultMutableTreeNode) path[i];

		Enumeration<MyDefaultMutableTreeNode> children1 = root.getChildren();
		while (children1.hasMoreElements()) {
			MyDefaultMutableTreeNode currentNode = children1.nextElement();
			MyTreeUserObject userObject = currentNode.getUserObject();
			MyTreeUserObject userObject1 = old.getUserObject();
			if (userObject.getMavenArtifactNode().equals(userObject1.getMavenArtifactNode())) {
				return getMatchingNode(path, currentNode, i + 1);
			}
		}
		return null;
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
