package krasa.mavenhelper.action.debug;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClassOwner;
import krasa.mavenhelper.action.RunTestFileAction;
import krasa.mavenhelper.analyzer.ComparableVersion;
import krasa.mavenhelper.icons.MyIcons;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import java.util.List;

public class DebugTestFileAction extends RunTestFileAction {

	public DebugTestFileAction() {
		super("Debug file", "Debug current File with Maven", MyIcons.ICON);
	}

	@Override
	protected String getText(String s) {
		return "Debug " + s;
	}

	@Override
	protected List<String> getGoals(AnActionEvent e, PsiClassOwner psiFile, MavenProject mavenProject) {
		List<String> goals = super.getGoals(e, psiFile, mavenProject);
		addParams(mavenProject, goals);
		return goals;
	}

	private void addParams(MavenProject mavenProject, List<String> goals) {
		MavenPlugin surefire = mavenProject.findPlugin("org.apache.maven.plugins", "maven-surefire-plugin");
		if (surefire != null) {
			ComparableVersion version = new ComparableVersion(surefire.getVersion());
			if (new ComparableVersion("2.14").compareTo(version) >= 1) {
				goals.addAll(Debug.DEBUG_FORK_MODE_LEGACY);
			} else {
				goals.addAll(Debug.DEBUG_FORK_MODE);
			}
		} else {
			goals.addAll(Debug.DEBUG_FORK_MODE);
		}
	}

	@Override
	protected void run(final DataContext context, final MavenRunnerParameters params) {
		runInternal(MavenActionUtil.getProject(context), params);
	}

	private void runInternal(final Project project, final MavenRunnerParameters params) {
		MavenDebugConfigurationType.debugConfiguration(project, params, null);
	}
}
