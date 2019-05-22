package krasa.mavenhelper.action.debug;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClassOwner;
import krasa.mavenhelper.action.RunTestFileAction;
import krasa.mavenhelper.icons.MyIcons;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
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
		goals.addAll(Debug.DEBUG_FORK_MODE);
		return goals;
	}

	@Override
	protected void run(final DataContext context, final MavenRunnerParameters params) {
		runInternal(MavenActionUtil.getProject(context), params);
	}

	private void runInternal(final Project project, final MavenRunnerParameters params) {
		MavenDebugConfigurationType.debugConfiguration(project, params, null);
	}
}
