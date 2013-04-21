package krasa.mavenrun.action.debug;

import java.util.List;

import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.project.Project;
import krasa.mavenrun.action.RunTestFileAction;
import krasa.mavenrun.utils.MavenDebugConfigurationType;

import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiJavaFile;

public class DebugTestFileAction extends RunTestFileAction {

	public DebugTestFileAction() {
		super("Debug file", "Debug current File with Maven", DebugIcons.PluginGoal);
	}

	@Override
	protected String getText(String s) {
		return "Debug " + s;
	}

	@Override
	protected List<String> getGoals(AnActionEvent e, PsiJavaFile psiFile) {
		List<String> goals = super.getGoals(e, psiFile);
		goals.add("-DforkMode=never");
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
