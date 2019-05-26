package krasa.mavenhelper.action;

import org.jetbrains.idea.maven.project.MavenProject;

public class MavenProjectInfo {

	MavenProject mavenProject;
	boolean root;

	public MavenProjectInfo(MavenProject mavenProject, boolean root) {
		this.mavenProject = mavenProject;
		this.root = root;
	}

	@Override
	public String toString() {
		return "MavenProjectInfo{" +
			"mavenProject=" + mavenProject +
			", root=" + root +
			'}';
	}
}
