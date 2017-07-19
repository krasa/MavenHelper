package krasa.mavenhelper.action.debug;

import org.jetbrains.annotations.Nullable;

import krasa.mavenhelper.action.CreateCustomGoalAction;
import krasa.mavenhelper.action.RunGoalAction;
import krasa.mavenhelper.model.Goal;

public class CreateCustomDebugGoalAction extends CreateCustomGoalAction {
	private boolean runGoal = true;

	public CreateCustomDebugGoalAction(boolean runGoal) {
		super(runGoal);
		this.runGoal = runGoal;
	}

	public CreateCustomDebugGoalAction(@Nullable String text) {
		super(text);
	}

	@Override
	protected RunGoalAction getRunGoalAction(Goal goal) {
		return DebugGoalAction.createDebug(goal, Debug.ICON, false);
	}

}
