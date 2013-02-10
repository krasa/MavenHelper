package krasa.mavenrun.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import java.util.Arrays;
import javax.swing.*;
import krasa.mavenrun.model.Goal;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

public class MavenGoalRunAction extends AnAction implements DumbAware {

	private String myGoal;

	public MavenGoalRunAction() {
	}

	public MavenGoalRunAction(String myGoal, Icon icon) {
		super(myGoal, myGoal, icon);
		this.myGoal = myGoal;
	}

	public MavenGoalRunAction(Goal goal, Icon icon) {
		super(goal.getCommandLine(), goal.getCommandLine(), icon);
		this.myGoal = goal.getCommandLine();
	}

	public void actionPerformed(AnActionEvent e) {
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		if (mavenProject != null) {
			final DataContext context = e.getDataContext();
			MavenRunnerParameters params = new MavenRunnerParameters(true,
					mavenProject.getDirectory(),
					Arrays.asList(myGoal),
					MavenActionUtil.getProjectsManager(context).getExplicitProfiles());
			MavenRunConfigurationType.runConfiguration(MavenActionUtil.getProject(context), params, null);
		}
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		Presentation p = e.getPresentation();
		p.setEnabled(isAvailable(e));
		p.setVisible(isVisible(e));
	}

	protected boolean isAvailable(AnActionEvent e) {
		return MavenActionUtil.hasProject(e.getDataContext());
	}

	protected boolean isVisible(AnActionEvent e) {
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		return mavenProject != null;
	}
}
