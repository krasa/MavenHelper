package krasa.mavenhelper.action.debug;

import krasa.mavenhelper.action.CreateCustomGoalAction;
import krasa.mavenhelper.action.RunGoalAction;
import krasa.mavenhelper.icons.MyIcons;
import krasa.mavenhelper.model.Goal;
import org.jetbrains.annotations.Nullable;

public class CreateCustomDebugGoalAction extends CreateCustomGoalAction {

	public CreateCustomDebugGoalAction(@Nullable String text) {
		super(text);
	}

	@Override
	protected RunGoalAction getRunGoalAction(Goal goal) {
		return DebugGoalAction.createDebug(goal, MyIcons.ICON, false);
	}

}
