package krasa.mavenhelper.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.project.actions.ReimportProjectAction;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import java.util.List;

/**
 * @author Vojtech Krasa
 */
class MyReimportProjectAction extends ReimportProjectAction {

	private final MavenProjectInfo mavenProjectInfo;

	public MyReimportProjectAction(@NotNull MavenProjectInfo mavenProjectInfo) {
		this.mavenProjectInfo = mavenProjectInfo;
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		final DataContext context = e.getDataContext();
		MavenProjectsManager projectsManager = MavenActionUtil.getProjectsManager(context);
		if (projectsManager != null) {
			perform(projectsManager, List.of(mavenProjectInfo.mavenProject), e);
		}
	}

	@Override
	protected boolean isAvailable(AnActionEvent e) {
		return mavenProjectInfo.mavenProject != null;
	}

	@Override
	public boolean isVisible(AnActionEvent e) {
		return mavenProjectInfo.mavenProject != null;
	}
}
