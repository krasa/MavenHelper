package krasa.mavenhelper.action.debug;

import javax.swing.*;

import krasa.mavenhelper.action.RunGoalAction;
import krasa.mavenhelper.model.Goal;

import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;

public class DebugGoalAction extends RunGoalAction {

	public DebugGoalAction(String goal, Icon icon) {
		super(goal, icon);
	}

	public DebugGoalAction(Goal goal, Icon icon) {
		super(goal, icon);
		String description = "debug: " + goal.getCommandLine();
		getTemplatePresentation().setText(description);
		getTemplatePresentation().setDescription(description);
	}

	@Override
	protected void run(final DataContext context, final MavenRunnerParameters params) {
		params.getGoals().addAll(Debug.DEBUG_FORK_MODE);
		runInternal(MavenActionUtil.getProject(context), params);
	}

	private void runInternal(final Project project, final MavenRunnerParameters params) {
		MavenDebugConfigurationType.debugConfiguration(project, params, null);
	}
}
