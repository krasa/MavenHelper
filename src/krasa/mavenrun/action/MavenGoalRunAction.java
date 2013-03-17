package krasa.mavenrun.action;

import java.util.Arrays;
import java.util.List;

import javax.swing.*;

import krasa.mavenrun.model.Goal;

import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;

public class MavenGoalRunAction extends AnAction implements DumbAware {

	private List<String> goalsToRun;

	public MavenGoalRunAction() {
	}

	public MavenGoalRunAction(String goal) {
		this.goalsToRun = Arrays.asList(goal);
	}

	public MavenGoalRunAction(String goal, Icon icon) {
		super(goal, goal, icon);
		this.goalsToRun = Arrays.asList(goal);
	}

	public MavenGoalRunAction(Goal goal, Icon icon) {
		super(goal.getCommandLine(), goal.getCommandLine(), icon);
		this.goalsToRun = Arrays.asList(goal.getCommandLine());
	}

	public void actionPerformed(AnActionEvent e) {
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		if (mavenProject != null) {
			final DataContext context = e.getDataContext();
			MavenRunnerParameters params = new MavenRunnerParameters(true, mavenProject.getDirectory(), goalsToRun,
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
