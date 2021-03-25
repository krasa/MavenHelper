package krasa.mavenhelper.analyzer.action;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.ui.PopupHandler;
import krasa.mavenhelper.analyzer.MyTreeUserObject;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;

/**
 * @author Vojtech Krasa
 */
public class RightTreePopupHandler extends PopupHandler {
	private final Project project;
	private final MavenProject mavenProject;
	protected final JTree tree;
	private JPopupMenu popup;

	public RightTreePopupHandler(Project project, MavenProject mavenProject, JTree tree) {
		this.project = project;
		this.mavenProject = mavenProject;
		this.tree = tree;
	}

	private DefaultMutableTreeNode getRoot() {
		return (DefaultMutableTreeNode) getModel().getRoot();
	}

	private DefaultTreeModel getModel() {
		return (DefaultTreeModel) tree.getModel();
	}

	@SuppressWarnings("Duplicates")
	public void invokePopup(final Component comp, final int x, final int y) {
		final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (selectedNode == null) {
			return;
		}
		final MyTreeUserObject myTreeUserObject = (MyTreeUserObject) selectedNode.getUserObject();
		final MavenArtifactNode mavenArtifactNode = myTreeUserObject.getMavenArtifactNode();
		DefaultActionGroup actionGroup = new DefaultActionGroup();

		if (myTreeUserObject.getMavenArtifactNode().getParent() == null) {
			actionGroup.add(new JumpToSourceAction(project, mavenProject, mavenArtifactNode));
		} else {
			actionGroup.add(new JumpToSourceAction(project, mavenProject, mavenArtifactNode));
			actionGroup.add(getExcludeAction(selectedNode, mavenArtifactNode));
		}

		popup = ActionManager.getInstance().createActionPopupMenu("", actionGroup).getComponent();
		popup.show(comp, x, y);
	}

	public void hidePopup() {
		if (popup != null && popup.isVisible()) {
			popup.setVisible(false);
			popup = null;
		}
	}

	private ExcludeDependencyAction getExcludeAction(final DefaultMutableTreeNode selectedNode,
													 MavenArtifactNode mavenArtifactNode) {
		return new ExcludeDependencyAction(project, mavenProject, mavenArtifactNode) {
			@Override
			public void dependencyExcluded() {
				removeTreeNodes();
			}

			private void removeTreeNodes() {
				// imagine conflict for d3
				// root
				// L d1
				// | L d2
				// | | L d3
				// | L d3
				// L ... something else containing d3
				// After d3 is excluded; must remove d2 and also d1 from the tree. But when d2 is excluded, remove only
				// d2.

				if (selectedNode.getParent() == null) {
					throw new IllegalStateException("selectedNode.getParent() == null " + selectedNode);
				}

				if (selectedNode.getParent() != getRoot()) {
					removeNodeNearestToRoot(selectedNode);
					return;
				}

				// find d1
				DefaultMutableTreeNode oldestParentDependency = (DefaultMutableTreeNode) selectedNode.getFirstChild();
				while (oldestParentDependency.getChildCount() > 0) {
					oldestParentDependency = (DefaultMutableTreeNode) oldestParentDependency.getFirstChild();
				}
				// find d1, d2
				java.util.List<DefaultMutableTreeNode> leafsForRemoval = findAllLeafs((MyTreeUserObject) oldestParentDependency.getUserObject());
				// remove both d3s for d1 and d2
				for (DefaultMutableTreeNode defaultMutableTreeNode : leafsForRemoval) {
					removeNodeNearestToRoot(defaultMutableTreeNode);
				}
			}

			private void removeNodeNearestToRoot(DefaultMutableTreeNode nodeForRemoval) {
				TreeNode nodeForRemovalNearestToRoot = nodeForRemoval;
				while (nodeForRemovalNearestToRoot.getParent() != null
						&& nodeForRemovalNearestToRoot.getParent().getParent() != null //https://github.com/krasa/MavenHelper/issues/58
						&& nodeForRemovalNearestToRoot.getParent() != getRoot()) {
					nodeForRemovalNearestToRoot = nodeForRemovalNearestToRoot.getParent();
				}
				getModel().removeNodeFromParent((MutableTreeNode) nodeForRemovalNearestToRoot);
			}

			private java.util.List<DefaultMutableTreeNode> findAllLeafs(MyTreeUserObject userObject) {
				final ArrayList<DefaultMutableTreeNode> result = new ArrayList<DefaultMutableTreeNode>();
				visitAllNodes(getRoot(), userObject, result);
				return result;
			}

			private void visitAllNodes(DefaultMutableTreeNode node, MyTreeUserObject lookedUpObject,
									   ArrayList<DefaultMutableTreeNode> result) {

				if (node.getChildCount() > 0) {
					for (Enumeration e = node.children(); e.hasMoreElements(); ) {
						DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
						visitAllNodes(n, lookedUpObject, result);
					}
				} else {
					// only leafs
					process(node, lookedUpObject, result);
				}
			}

			private void process(DefaultMutableTreeNode node, MyTreeUserObject lookedUpObject,
								 ArrayList<DefaultMutableTreeNode> result) {
				final MyTreeUserObject userObject = (MyTreeUserObject) node.getUserObject();
				if (userObject != null && lookedUpObject.getArtifact().equals(userObject.getArtifact())) {
					result.add(node);
				}
			}
		};
	}
}
