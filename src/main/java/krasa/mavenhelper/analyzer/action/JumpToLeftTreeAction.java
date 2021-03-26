package krasa.mavenhelper.analyzer.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import krasa.mavenhelper.analyzer.GuiForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;

/**
 * @author Vojtech Krasa
 */
public class JumpToLeftTreeAction extends BaseAction {

	private final GuiForm guiForm;

	public JumpToLeftTreeAction(Project project, MavenProject mavenProject, MavenArtifactNode myTreeNode, GuiForm guiForm) {
		super(project, mavenProject, myTreeNode, getLabel());
		this.guiForm = guiForm;
	}

	@NotNull
	private static String getLabel() {
		return "Jump to Left Tree";
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		guiForm.switchToLeftTree(myArtifact);
	}

}
