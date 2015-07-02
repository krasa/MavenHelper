package krasa.mavenrun.analyzer;

import java.awt.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

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
class LeftTreePopupHandler extends PopupHandler {
	private final Project project;
	private final MavenProject mavenProject;
	protected final DefaultTreeModel treeModel;
	protected final DefaultMutableTreeNode treeRoot;
	protected final JTree tree;

	public LeftTreePopupHandler(Project project, MavenProject mavenProject, JTree tree) {
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
				treeModel.removeNodeFromParent(selectedNode);
			}
		};
	}
}
