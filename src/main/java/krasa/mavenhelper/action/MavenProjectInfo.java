package krasa.mavenhelper.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;

public class MavenProjectInfo {

	MavenProject mavenProject;
	MavenProject childProject;
	boolean root;

	public MavenProjectInfo(MavenProject mavenProject) {
		this.mavenProject = mavenProject;
	}

	public MavenProjectInfo(DataContext dataContext) {
		this(Utils.getMavenProject(dataContext));
	}

	public MavenProjectInfo(MavenProject root, MavenProject child) {
		this.mavenProject = root;
		this.childProject = child;
		this.root = true;
	}

	@NotNull
	public static MavenProjectInfo get(MavenProjectInfo info, AnActionEvent e) {
		MavenProjectInfo mavenProjectInfo = info;
		if (mavenProjectInfo == null) {
			mavenProjectInfo = new MavenProjectInfo(e.getDataContext());
		}
		return mavenProjectInfo;
	}

	public MavenProject getCurrentOrRootMavenProject() {
		return childProject != null ? childProject : mavenProject;
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
