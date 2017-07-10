package krasa.mavenhelper.action.debug;

import krasa.mavenhelper.action.CreateCustomGoalAction;
import krasa.mavenhelper.action.RunGoalAction;
import krasa.mavenhelper.model.Goal;

import org.jetbrains.annotations.Nullable;

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
		return new DebugGoalAction(goal, Debug.ICON);
	}

}
