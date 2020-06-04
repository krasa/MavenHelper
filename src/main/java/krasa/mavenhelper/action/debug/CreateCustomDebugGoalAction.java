package krasa.mavenhelper.action.debug;

import krasa.mavenhelper.action.CreateCustomGoalAction;
import krasa.mavenhelper.action.MavenProjectInfo;
import krasa.mavenhelper.action.RunGoalAction;
import krasa.mavenhelper.icons.MyIcons;
import krasa.mavenhelper.model.Goal;
import org.jetbrains.annotations.Nullable;

public class CreateCustomDebugGoalAction extends CreateCustomGoalAction {

	public CreateCustomDebugGoalAction(@Nullable String text, MavenProjectInfo mavenProject) {
		super(text, mavenProject);
	}

	@Override
	protected RunGoalAction getRunGoalAction(Goal goal) {
		return DebugGoalAction.createDebug(goal, MyIcons.ICON, false, mavenProject);
	}

}
