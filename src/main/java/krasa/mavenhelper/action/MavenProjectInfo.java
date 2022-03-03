package krasa.mavenhelper.action;

import com.intellij.openapi.actionSystem.DataContext;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

public class MavenProjectInfo {

	MavenProject mavenProject;
	boolean root;

	public MavenProjectInfo(MavenProject mavenProject, boolean root) {
		this.mavenProject = mavenProject;
		this.root = root;
	}

	public MavenProjectInfo(DataContext dataContext) {
		this(MavenActionUtil.getMavenProject(dataContext), false);
	}

	public MavenProject getMavenProject() {
		return mavenProject;
	}

	public boolean isRoot() {
		return root;
	}

	@Override
	public String toString() {
		return "MavenProjectInfo{" +
				"mavenProject=" + mavenProject +
				", root=" + root +
				'}';
	}
}
