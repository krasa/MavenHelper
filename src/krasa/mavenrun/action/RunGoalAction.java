package krasa.mavenrun.action;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import krasa.mavenrun.model.Goal;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;

public class RunGoalAction extends AnAction implements DumbAware {

	protected List<String> goalsToRun;

	public RunGoalAction() {
	}

	public RunGoalAction(String goal) {
		this.goalsToRun = parse(goal);
	}

	public RunGoalAction(String goal, Icon icon) {
		super(goal, goal, icon);
		this.goalsToRun = parse(goal);
	}

	public RunGoalAction(Goal goal, Icon icon) {
		super(goal.getCommandLine(), goal.getCommandLine(), icon);
		this.goalsToRun = parse(goal.getCommandLine());
	}

	public RunGoalAction(Goal goal) {
		this.goalsToRun = parse(goal.getCommandLine());
	}

	protected List<String> parse(String goal) {
		List<String> strings = new ArrayList<String>();
		String[] split = goal.split(" ");
		for (String s : split) {
			if (StringUtils.isNotBlank(s)) {
				strings.add(s);
			}
		}
		return strings;
	}

	public void actionPerformed(AnActionEvent e) {
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		if (mavenProject != null) {
			final DataContext context = e.getDataContext();
			MavenRunnerParameters params = new MavenRunnerParameters(true, mavenProject.getDirectory(), goalsToRun,
					MavenActionUtil.getProjectsManager(context).getExplicitProfiles());
			run(context, params);
		}
	}

	protected void run(DataContext context, MavenRunnerParameters params) {
		MavenRunConfigurationType.runConfiguration(MavenActionUtil.getProject(context), params, null);
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
