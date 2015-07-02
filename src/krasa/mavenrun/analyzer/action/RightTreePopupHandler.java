package krasa.mavenrun.analyzer.action;

import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import krasa.mavenrun.analyzer.MyTreeUserObject;

import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.ui.PopupHandler;

/**
 * @author Vojtech Krasa
 */
public class RightTreePopupHandler extends PopupHandler {
	private final Project project;
	private final MavenProject mavenProject;
	protected final JTree tree;

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

		ActionManager.getInstance().createActionPopupMenu("", actionGroup).getComponent().show(comp, x, y);

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
				// ?? d1
				// ? ?? d2
				// ? ? ?? d3
				// ? ?? d3
				// ?? ... something else containing d3
				// After d3 is excluded; must remove d2 and also d1 from the tree. But when d2 is excluded, remove only
				// d2.

				// when d2 is excluded, remove d3 for d2, but not d3 for d1
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
					for (Enumeration e = node.children(); e.hasMoreElements();) {
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
