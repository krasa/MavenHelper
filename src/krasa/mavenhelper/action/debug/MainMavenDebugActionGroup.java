package krasa.mavenhelper.action.debug;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import krasa.mavenhelper.action.CreateCustomGoalAction;
import krasa.mavenhelper.action.MainMavenActionGroup;
import krasa.mavenhelper.action.RunConfigurationAction;
import krasa.mavenhelper.action.RunGoalAction;

import javax.swing.*;
import java.util.List;

@SuppressWarnings("ComponentNotRegistered")
public class MainMavenDebugActionGroup extends MainMavenActionGroup {

	public MainMavenDebugActionGroup(String shortName, final Icon pluginGoal) {
		super(shortName, pluginGoal);
	}

	@Override
	protected CreateCustomGoalAction getCreateCustomGoalAction() {
		return new CreateCustomDebugGoalAction("New Goal...");
	}

	@Override
	protected void addTestFile(List<AnAction> result) {
		result.add(new DebugTestFileAction());
	}

	@Override
	protected Icon getRunIcon() {
		return Debug.ICON;
	}

	@Override
	protected RunGoalAction createGoalRunAction(String basicPhase, final Icon phase) {
		return new DebugGoalAction(basicPhase, phase);
	}

	@Override
	protected RunConfigurationAction getRunConfigurationAction(Project project, RunnerAndConfigurationSettings cfg) {
		return new DebugConfigurationAction(DefaultDebugExecutor.getDebugExecutorInstance(), true, project, cfg);
	}
}
