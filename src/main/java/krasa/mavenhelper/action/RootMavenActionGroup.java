package krasa.mavenhelper.action;

import com.intellij.openapi.actionSystem.DataContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import java.util.List;

@SuppressWarnings("ComponentNotRegistered")
public class RootMavenActionGroup extends MainMavenActionGroup {
	@NotNull
	@Override
	protected MavenProjectInfo getMavenProject(DataContext dataContext) {
		MavenProject childProject = Utils.getMavenProject(dataContext);
		MavenProject mavenProject = childProject;
		if (mavenProject == null) {
			return new MavenProjectInfo((MavenProject) null);
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

		return new MavenProjectInfo(root, childProject);
	}
}
