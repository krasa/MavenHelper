package krasa.mavenhelper.action;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import krasa.mavenhelper.model.ApplicationSettings;
import krasa.mavenhelper.model.Goal;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class RunGoalAction extends AnAction implements DumbAware {

	private final Goal goal;
	@Nullable
	private final MavenProjectInfo mavenProject;
	String commandLine;

	protected RunGoalAction(Goal goal, String text, String description, Icon icon, @Nullable MavenProjectInfo mavenProject) {
		super(text, description, icon);
		commandLine = goal.getCommandLine();
		this.goal = goal;
		this.mavenProject = mavenProject;
	}

	public static RunGoalAction create(Goal goal, Icon icon, boolean popupAction, MavenProjectInfo mavenProject) {
		if (popupAction) {
			return new RunGoalAction(goal, goal.getPresentableName(), goal.getCommandLine(), icon, mavenProject);
		} else {
			return new RunGoalAction(goal, "Run: " + goal.getPresentableName(), "Run: " + goal.getCommandLine(), icon, mavenProject);
		}
	}

	public Goal getGoal() {
		return goal;
	}

	private List<String> parse(String goal, PsiFile psiFile, ConfigurationContext configurationContext) {
		goal = ApplicationSettings.get().applyAliases(goal, psiFile, configurationContext);
		
		List<String> strings = new ArrayList<String>();
		String[] split = goal.split("\\s");
		for (String s : split) {
			if (StringUtils.isNotBlank(s)) {
				strings.add(s);
			}
		}
		return strings;
	}

	public void actionPerformed(AnActionEvent e) {
		final DataContext context = e.getDataContext();

		Project project = MavenActionUtil.getProject(context);
		String pomDir = Utils.getPomDirAsString(context, mavenProject);
		MavenProjectsManager projectsManager = MavenActionUtil.getProjectsManager(context);
		PsiFile data = LangDataKeys.PSI_FILE.getData(e.getDataContext());
		ConfigurationContext configurationContext = ConfigurationContext.getFromContext(e.getDataContext());

		actionPerformed(project, pomDir, projectsManager, data, configurationContext);
	}

	public void actionPerformed(Project project, String pomDir, MavenProjectsManager projectsManager, PsiFile psiFile, ConfigurationContext configurationContext) {
		if (pomDir != null) {
			List<String> goalsToRun = parse(commandLine, psiFile, configurationContext);
			MavenRunnerParameters params = new MavenRunnerParameters(true, pomDir, null, goalsToRun, projectsManager.getExplicitProfiles());
			run(params, project);
		}
	}

	protected void run(MavenRunnerParameters params, Project project) {
		MavenRunConfigurationType.runConfiguration(project, params, null);
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
