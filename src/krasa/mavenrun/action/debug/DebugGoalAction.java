package krasa.mavenrun.action.debug;

import javax.swing.*;

import krasa.mavenrun.action.RunGoalAction;
import krasa.mavenrun.model.Goal;
import krasa.mavenrun.utils.MavenDebugConfigurationType;

import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import com.intellij.openapi.actionSystem.DataContext;

public class DebugGoalAction extends RunGoalAction {

	public DebugGoalAction(String goal, Icon icon) {
		super(goal, icon);
	}

	public DebugGoalAction(Goal goal, Icon icon) {
		super(goal, icon);
	}

	@Override
	protected void run(DataContext context, MavenRunnerParameters params) {
		params.getGoals().add("-DforkMode=never");
		MavenDebugConfigurationType.debugConfiguration(MavenActionUtil.getProject(context), params, null);
	}
}
