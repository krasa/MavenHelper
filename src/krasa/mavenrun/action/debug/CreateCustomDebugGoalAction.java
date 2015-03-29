package krasa.mavenrun.action.debug;

import krasa.mavenrun.action.CreateCustomGoalAction;
import krasa.mavenrun.action.RunGoalAction;
import krasa.mavenrun.model.Goal;

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
