package krasa.mavenhelper.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.project.actions.ReimportProjectAction;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import java.util.Arrays;

/**
 * @author Vojtech Krasa
 */
class MyReimportProjectAction extends ReimportProjectAction {

	private final MavenProjectInfo mavenProject;

	public MyReimportProjectAction(@NotNull MavenProjectInfo mavenProject) {
		this.mavenProject = mavenProject;
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		final DataContext context = e.getDataContext();
		MavenProjectsManager projectsManager = MavenActionUtil.getProjectsManager(context);
		if (projectsManager != null) {
			perform(projectsManager, Arrays.asList(mavenProject.mavenProject), e);
		}
	}

	@Override
	protected boolean isAvailable(AnActionEvent e) {
		return MavenActionUtil.hasProject(e.getDataContext());
	}

	@Override
	protected boolean isVisible(AnActionEvent e) {
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		return mavenProject != null;
	}
}
