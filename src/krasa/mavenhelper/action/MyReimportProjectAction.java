package krasa.mavenhelper.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.actions.ReimportProjectAction;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import java.util.Arrays;

/**
 * @author Vojtech Krasa
 */
class MyReimportProjectAction extends ReimportProjectAction {

	@Override
	public void actionPerformed(AnActionEvent e) {
		final DataContext context = e.getDataContext();
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		if (mavenProject != null) {
			perform(MavenActionUtil.getProjectsManager(context), Arrays.asList(mavenProject), e);
		}
	}

	protected boolean isAvailable(AnActionEvent e) {
		return MavenActionUtil.hasProject(e.getDataContext());
	}

	protected boolean isVisible(AnActionEvent e) {
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		return mavenProject != null;
	}
}
