package krasa.mavenrun.action.debug;

import java.util.List;

import javax.swing.*;

import krasa.mavenrun.action.CreateCustomGoalAction;
import krasa.mavenrun.action.MainMavenActionGroup;
import krasa.mavenrun.action.RunGoalAction;

import com.intellij.openapi.actionSystem.AnAction;

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
		return DebugIcons.PluginGoal;
	}

	@Override
	protected RunGoalAction createGoalRunAction(String basicPhase, final Icon phase) {
		return new DebugGoalAction(basicPhase, phase);
	}

}
