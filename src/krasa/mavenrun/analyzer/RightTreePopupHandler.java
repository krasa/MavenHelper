package krasa.mavenrun.analyzer;

import java.awt.*;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import krasa.mavenrun.analyzer.action.ExcludeDependencyAction;
import krasa.mavenrun.analyzer.action.JumpToSourceAction;

import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.ui.PopupHandler;

/**
 * @author Vojtech Krasa
 */
class RightTreePopupHandler extends PopupHandler {
	private final Project project;
	private final MavenProject mavenProject;
	protected final DefaultTreeModel treeModel;
	protected final DefaultMutableTreeNode treeRoot;
	protected final JTree tree;

	public RightTreePopupHandler(Project project, MavenProject mavenProject, JTree tree) {
		this.project = project;
		this.mavenProject = mavenProject;
		this.tree = tree;
		this.treeModel = (DefaultTreeModel) tree.getModel();
		treeRoot = (DefaultMutableTreeNode) treeModel.getRoot();
	}

	public void invokePopup(final Component comp, final int x, final int y) {
		final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (selectedNode == null) {
			return;
		}
		final MyTreeUserObject myTreeUserObject = (MyTreeUserObject) selectedNode.getUserObject();
		final MavenArtifactNode mavenArtifactNode = myTreeUserObject.getMavenArtifactNode();
		if (myTreeUserObject.getMavenArtifactNode().getParent() == null) {
			final DefaultActionGroup actionGroup = new DefaultActionGroup(new JumpToSourceAction(project, mavenProject,
					mavenArtifactNode));
			ActionManager.getInstance().createActionPopupMenu("", actionGroup).getComponent().show(comp, x, y);
		} else {
			showExcludableActionGroup(comp, x, y, selectedNode, mavenArtifactNode);
		}

	}

	private void showExcludableActionGroup(Component comp, int x, int y, final DefaultMutableTreeNode selectedNode,
			final MavenArtifactNode mavenArtifactNode) {
		ExcludeDependencyAction excludeAction = getExcludeAction(selectedNode, mavenArtifactNode);
		JumpToSourceAction jumpToSourceAction = new JumpToSourceAction(project, mavenProject, mavenArtifactNode);
		DefaultActionGroup actionGroup = new DefaultActionGroup(excludeAction, jumpToSourceAction);
		ActionManager.getInstance().createActionPopupMenu("", actionGroup).getComponent().show(comp, x, y);
	}

	private ExcludeDependencyAction getExcludeAction(final DefaultMutableTreeNode selectedNode,
			MavenArtifactNode mavenArtifactNode) {
		return new ExcludeDependencyAction(project, mavenProject, mavenArtifactNode) {
			@Override
			public void dependencyExcluded() {
				removeTreeNodes();
			}

			/**
			 * imagine pom: root -> d1 ; d1 -> d2 ; d1 -> d3 ; d2 -> d3. After d3 is excluded; must remove d1 and also
			 * d2 from the tree. But when d2 is excluded, remove only d2.
			 */
			private void removeTreeNodes() {
				// when d2 is excluded, remove d3 for d2, but not d3 for d1
				if (selectedNode.getParent() != treeRoot) {
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
						&& nodeForRemovalNearestToRoot.getParent() != treeRoot) {
					nodeForRemovalNearestToRoot = nodeForRemovalNearestToRoot.getParent();
				}
				treeModel.removeNodeFromParent((MutableTreeNode) nodeForRemovalNearestToRoot);
			}

			private java.util.List<DefaultMutableTreeNode> findAllLeafs(MyTreeUserObject userObject) {
				final ArrayList<DefaultMutableTreeNode> result = new ArrayList<DefaultMutableTreeNode>();
				visitAllNodes(treeRoot, userObject, result);
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
