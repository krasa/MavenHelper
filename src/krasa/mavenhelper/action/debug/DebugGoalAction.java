package krasa.mavenhelper.action.debug;

import com.intellij.openapi.project.Project;
import krasa.mavenhelper.action.RunGoalAction;
import krasa.mavenhelper.model.Goal;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;

import javax.swing.*;

public class DebugGoalAction extends RunGoalAction {

	private DebugGoalAction(Goal goal, String text, String description, Icon icon) {
		super(goal, text, description, icon);
	}

	public static DebugGoalAction createDebug(Goal goal, Icon icon, boolean popupAction) {
		if (popupAction) {
			return new DebugGoalAction(goal, goal.getPresentableName(), goal.getCommandLine(), icon);
		} else {
			return new DebugGoalAction(goal, "debug: " + goal.getPresentableName(), "debug: " + goal.getCommandLine(), icon);
		}
	}

	@Override
	protected void run(final MavenRunnerParameters params, Project project) {
		params.getGoals().addAll(Debug.DEBUG_FORK_MODE);
		runInternal(project, params);
	}

	private void runInternal(final Project project, final MavenRunnerParameters params) {
		MavenDebugConfigurationType.debugConfiguration(project, params, null);
	}
}
