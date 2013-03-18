package krasa.mavenrun.action;

import static com.intellij.openapi.ui.Messages.*;

import java.util.List;

import krasa.mavenrun.ApplicationComponent;
import krasa.mavenrun.model.ApplicationSettings;
import krasa.mavenrun.model.Goal;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;

public class CreateCustomGoalAction extends AnAction implements DumbAware {
	public CreateCustomGoalAction(@Nullable String text) {
		super(text);
	}

	public void actionPerformed(AnActionEvent e) {
		ApplicationSettings state = ApplicationComponent.getInstance().getState();
		List<String> goals = state.getGoalsAsStrings();

		String s = Messages.showEditableChooseDialog("Command line:", "New Goal", getQuestionIcon(),
				goals.toArray(goals.toArray(new String[goals.size()])), "", new NonEmptyInputValidator());
		if (StringUtils.isNotBlank(s)) {
			state.add(new Goal(s.trim()));
			new MavenGoalRunAction(s).actionPerformed(e);
		}
	}

}
