package krasa.mavenrun.action.debug;

import java.util.List;

import krasa.mavenrun.action.RunTestFileAction;

import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiJavaFile;

public class DebugTestFileAction extends RunTestFileAction {

	public DebugTestFileAction() {
		super("Debug file", "Debug current File with Maven", Debug.ICON);
	}

	@Override
	protected String getText(String s) {
		return "Debug " + s;
	}

	@Override
	protected List<String> getGoals(AnActionEvent e, PsiJavaFile psiFile, MavenProject mavenProject) {
		List<String> goals = super.getGoals(e, psiFile, mavenProject);
		goals.addAll(Debug.DEBUG_FORK_MODE);
		return goals;
	}

	@Override
	protected void run(final DataContext context, final MavenRunnerParameters params) {
		runInternal(MavenActionUtil.getProject(context), params);
	}

	private void runInternal(final Project project, final MavenRunnerParameters params) {
		MavenDebugConfigurationType.debugConfiguration(project, params, new ProgramRunner.Callback() {
			@Override
			public void processStarted(RunContentDescriptor descriptor) {
				descriptor.setRestarter(new Runnable() {
					@Override
					public void run() {
						DebugTestFileAction.this.runInternal(project, params);
					}
				});
			}
		});
	}
}
