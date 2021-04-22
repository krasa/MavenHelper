package krasa.mavenhelper.analyzer.action;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.components.JBList;
import krasa.mavenhelper.analyzer.GuiForm;
import krasa.mavenhelper.analyzer.MyListNode;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;

import javax.swing.*;
import java.awt.*;

/**
 * @author Vojtech Krasa
 */
public class ListPopupHandler extends PopupHandler {
	private final Project project;
	private final MavenProject mavenProject;
	protected final JBList list;
	private final GuiForm guiForm;
	private JPopupMenu popup;

	public ListPopupHandler(Project project, MavenProject mavenProject, JBList list, GuiForm guiForm) {
		this.project = project;
		this.mavenProject = mavenProject;
		this.list = list;
		this.guiForm = guiForm;
	}

	@Override
	@SuppressWarnings("Duplicates")
	public void invokePopup(final Component comp, final int x, final int y) {
		final MyListNode selectedNode = (MyListNode) list.getSelectedValue();
		if (selectedNode == null) {
			return;
		}
		final MavenArtifactNode mavenArtifactNode = selectedNode.getRightArtifact();
		if (mavenArtifactNode == null) {
			return;
		}

		DefaultActionGroup actionGroup = new DefaultActionGroup();
		actionGroup.add(new JumpToLeftTreeListAction(project, mavenProject, mavenArtifactNode, guiForm));
		popup = ActionManager.getInstance().createActionPopupMenu("", actionGroup).getComponent();
		popup.show(comp, x, y);
	}

	public void hidePopup() {
		if (popup != null && popup.isVisible()) {
			popup.setVisible(false);
			popup = null;
		}
	}


}
