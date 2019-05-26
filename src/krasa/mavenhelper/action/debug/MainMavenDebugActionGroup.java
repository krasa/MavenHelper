package krasa.mavenhelper.action.debug;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.project.Project;
import krasa.mavenhelper.action.CreateCustomGoalAction;
import krasa.mavenhelper.action.MainMavenActionGroup;
import krasa.mavenhelper.action.MavenProjectInfo;
import krasa.mavenhelper.action.RunConfigurationAction;
import krasa.mavenhelper.icons.MyIcons;
import krasa.mavenhelper.model.Goal;

import javax.swing.*;
import java.util.List;

@SuppressWarnings("ComponentNotRegistered")
public class MainMavenDebugActionGroup extends MainMavenActionGroup {

	public MainMavenDebugActionGroup(String shortName, final Icon pluginGoal) {
		super(shortName, pluginGoal);
	}

	@Override
	protected CreateCustomGoalAction getCreateCustomGoalAction(MavenProjectInfo mavenProject) {
		return new CreateCustomDebugGoalAction("New Goal...", mavenProject);
	}

	@Override
	protected void addTestFile(List<AnAction> result) {
		result.add(new DebugTestFileAction());
	}

	@Override
	protected Icon getRunIcon() {
		return MyIcons.ICON;
	}

	@Override
	protected AnAction createGoalRunAction(Goal basicPhase, final Icon icon, boolean plugin, MavenProjectInfo mavenProject) {
		return DebugGoalAction.createDebug(basicPhase, icon, true, mavenProject);
	}

	@Override
	protected RunConfigurationAction getRunConfigurationAction(Project project, RunnerAndConfigurationSettings cfg) {
		return new DebugConfigurationAction(DefaultDebugExecutor.getDebugExecutorInstance(), true, project, cfg);
	}
}
