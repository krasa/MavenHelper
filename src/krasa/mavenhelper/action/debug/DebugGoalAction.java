package krasa.mavenhelper.action.debug;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;

import krasa.mavenhelper.action.RunGoalAction;
import krasa.mavenhelper.model.Goal;

public class DebugGoalAction extends RunGoalAction {

	private DebugGoalAction(Goal goal, String text, Icon icon) {
		super(goal, text, icon);
	}

	public static DebugGoalAction createDebug(Goal goal, Icon icon, boolean popupAction) {
		if (popupAction) {
			String text = getText(goal);
			return new DebugGoalAction(goal, text, icon);
		} else {
			return new DebugGoalAction(goal, goal.getCommandLine(), icon);
		}
	}

	@NotNull
	private static String getText(Goal goal) {
		return "debug: " + goal.getCommandLine();
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
