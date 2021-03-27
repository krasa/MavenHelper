package krasa.mavenhelper.analyzer;

import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.Enumeration;

public class RestoreSelection {
	private MyListNode selectedListNode;
	@NotNull
	private final JBList leftPanelList;
	@NotNull
	private final MyHighlightingTree leftTree;
	private TreePath selectionPath;

	public RestoreSelection(JBList leftPanelList, MyHighlightingTree leftTree) {
		selectedListNode = (MyListNode) leftPanelList.getSelectedValue();
		this.leftPanelList = leftPanelList;
		selectionPath = leftTree.getSelectionPath();
		this.leftTree = leftTree;
	}

	public void restore() {
		if (selectedListNode != null) {
			MyDefaultListModel model = (MyDefaultListModel) leftPanelList.getModel();
			for (MyListNode o : model) {
				if (o.getRightArtifact().equals(selectedListNode.rightArtifact)) {
					leftPanelList.setSelectedValue(o, true);
					break;
				}
			}
		}

		if (selectionPath != null) {
			Object[] path = selectionPath.getPath();
			MyDefaultMutableTreeNode root = (MyDefaultMutableTreeNode) leftTree.getModel().getRoot();
			MyDefaultMutableTreeNode matchingNode = TreeUtils.getMatchingNode(path, root, 1);
			if (matchingNode != null && matchingNode != root) {
				leftTree.getSelectionModel().addSelectionPath(new TreePath(matchingNode.getPath()));
				leftTree.scrollPathToVisible(new TreePath(matchingNode.getPath()));
			}
		}
	}

	private void matches(MyDefaultMutableTreeNode o, MyDefaultMutableTreeNode root) {
		Enumeration<MyDefaultMutableTreeNode> children1 = root.getChildren();
		while (children1.hasMoreElements()) {
			MyDefaultMutableTreeNode myDefaultMutableTreeNode = children1.nextElement();
			MyTreeUserObject userObject = myDefaultMutableTreeNode.getUserObject();
			MyTreeUserObject userObject1 = o.getUserObject();
			if (userObject.getMavenArtifactNode().equals(userObject1.getMavenArtifactNode())) {

			}
		}
	}
}
