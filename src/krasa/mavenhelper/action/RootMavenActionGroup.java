package krasa.mavenhelper.action;

import com.intellij.openapi.actionSystem.DataContext;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import java.util.List;

@SuppressWarnings("ComponentNotRegistered")
public class RootMavenActionGroup extends MainMavenActionGroup {
	@Override
	protected MavenProjectInfo getMavenProject(DataContext dataContext) {
		MavenProject mavenProject = MavenActionUtil.getMavenProject(dataContext);
		if (mavenProject == null) {
			return null;
		}
		MavenProjectsManager projectsManager = MavenActionUtil.getProjectsManager(dataContext);
		List<MavenProject> rootProjects = projectsManager.getRootProjects();

		MavenProject root = null;
		if (rootProjects.contains(mavenProject)) {
			root = mavenProject;
		} else {
			MavenId parentId = mavenProject.getParentId();
			while (parentId != null) {
				mavenProject = projectsManager.findProject(parentId);
				if (mavenProject == null) {
					break;
				}
				if (rootProjects.contains(mavenProject)) {
					root = mavenProject;
					break;
				}
				parentId = mavenProject.getParentId();
			}
		}

		return new MavenProjectInfo(root, true);
	}
}
