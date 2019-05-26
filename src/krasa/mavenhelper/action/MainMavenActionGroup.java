package krasa.mavenhelper.action;

import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.util.PathUtil;
import krasa.mavenhelper.ApplicationComponent;
import krasa.mavenhelper.icons.MyIcons;
import krasa.mavenhelper.model.ApplicationSettings;
import krasa.mavenhelper.model.Goal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunConfiguration;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.project.actions.ReimportProjectAction;
import org.jetbrains.idea.maven.utils.MavenArtifactUtil;
import org.jetbrains.idea.maven.utils.MavenPluginInfo;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("ComponentNotRegistered")
public class MainMavenActionGroup extends ActionGroup implements DumbAware {
	static final Logger LOG = Logger.getInstance(MainMavenActionGroup.class);

	private Set<String> pluginGoalsSet = new HashSet<String>();

	public MainMavenActionGroup() {
	}

	public MainMavenActionGroup(String shortName, Icon icon) {
		super(shortName, true);
		getTemplatePresentation().setIcon(icon);
	}

	@NotNull
	@Override
	public AnAction[] getChildren(@Nullable AnActionEvent e) {
		if (e != null) {
			return getActions(e.getDataContext(), e.getProject());
		} else {
			return new AnAction[0];
		}
	}

	public AnAction[] getActions(DataContext dataContext, Project project) {
		List<AnAction> result = new ArrayList<AnAction>();
		MavenProjectInfo mavenProject = getMavenProject(dataContext);
		if (mavenProject.mavenProject != null) {
			addTestFile(result);
			separator(result);
			addRunConfigurations(result, project, mavenProject);
			separator(result);

			addGoals(result, mavenProject);
			separator(result);

			List<MavenActionGroup> mavenActionGroups = getPlugins(project, mavenProject);

			addPluginAwareActions(result, mavenActionGroups, mavenProject);
			separator(result);

			addPlugins(result, mavenActionGroups);

			separator(result);
			addReimport(result, mavenProject);
			result.add(getCreateCustomGoalAction(mavenProject));

		}
		return result.toArray(new AnAction[result.size()]);
	}

	protected MavenProjectInfo getMavenProject(DataContext dataContext) {
		return new MavenProjectInfo(MavenActionUtil.getMavenProject(dataContext), false);
	}

	private void addRunConfigurations(List<AnAction> result, Project project, final MavenProjectInfo mavenProject) {
		final List<RunnerAndConfigurationSettings> configurationSettings = RunManager.getInstance(project).getConfigurationSettingsList(
			MavenRunConfigurationType.getInstance());

		String directory = PathUtil.getCanonicalPath(mavenProject.mavenProject.getDirectory());

		for (RunnerAndConfigurationSettings cfg : configurationSettings) {
			MavenRunConfiguration mavenRunConfiguration = (MavenRunConfiguration) cfg.getConfiguration();
			if (directory.equals(PathUtil.getCanonicalPath(mavenRunConfiguration.getRunnerParameters().getWorkingDirPath()))) {
				result.add(getRunConfigurationAction(project, cfg));
			}
		}
	}

	protected RunConfigurationAction getRunConfigurationAction(Project project, RunnerAndConfigurationSettings cfg) {
		return new RunConfigurationAction(DefaultRunExecutor.getRunExecutorInstance(), true, project, cfg);
	}

	private void addReimport(List<AnAction> result, MavenProjectInfo mavenProject) {
		final ReimportProjectAction e = new MyReimportProjectAction(mavenProject);
		e.getTemplatePresentation().setText("Reimport");
		e.getTemplatePresentation().setIcon(AllIcons.Actions.Refresh);
		e.getTemplatePresentation().setDescription("Reimport selected Maven project");
		result.add(e);

	}

	protected CreateCustomGoalAction getCreateCustomGoalAction(MavenProjectInfo mavenProject) {
		return new CreateCustomGoalAction("New Goal...", mavenProject);
	}

	protected void addTestFile(List<AnAction> result) {
		result.add(new RunTestFileAction());
	}

	private void addPlugins(List<AnAction> anActions, List<MavenActionGroup> mavenActionGroups) {
		MavenActionGroup plugins = new MavenActionGroup("Plugins", true);
		anActions.add(plugins);
		for (MavenActionGroup mavenActionGroup : mavenActionGroups) {
			plugins.add(mavenActionGroup);
		}
	}

	private void separator(List<AnAction> anActions) {
		if (!anActions.isEmpty()) {
			AnAction anAction = anActions.get(anActions.size() - 1);
			if (!(anAction instanceof Separator)) {
				anActions.add(new Separator());
			}
		}
	}

	private void addGoals(List<AnAction> anActions, MavenProjectInfo mavenProject) {
		for (Goal goal : getState().getGoals().getGoals()) {
			anActions.add(createGoalRunAction(goal, getRunIcon(), false, mavenProject));
		}
	}


	private ApplicationSettings getState() {
		return ApplicationComponent.getInstance().getState();
	}

	private void addPluginAwareActions(List<AnAction> anActions, List<MavenActionGroup> mavenActionGroups, MavenProjectInfo mavenProject) {
		assert mavenActionGroups != null; // just to be sure that pluginGoalsSet was initialized
		for (Goal goal : getState().getPluginAwareGoals().getGoals()) {
			if (pluginGoalsSet.contains(goal.getCommandLine())) {
				anActions.add(createGoalRunAction(goal, getRunIcon(), false, mavenProject));
			}
		}
	}

	private List<MavenActionGroup> getPlugins(Project project, MavenProjectInfo mavenProject) {
		List<MavenActionGroup> mavenActionGroups = new ArrayList<MavenActionGroup>();
		for (MavenPlugin mavenPlugin : mavenProject.mavenProject.getDeclaredPlugins()) {
			MavenActionGroup plugin = new MavenActionGroup(mavenPlugin.getArtifactId(), true);
			plugin.getTemplatePresentation().setIcon(getIcon());
			addPluginGoals(project, mavenPlugin, plugin, mavenProject);
			mavenActionGroups.add(plugin);
		}
		return mavenActionGroups;
	}

	protected Icon getRunIcon() {
		return MyIcons.RUN_MAVEN_ICON;
	}

	protected Icon getIcon() {
		return MyIcons.PHASES_CLOSED;
	}

	private void addPluginGoals(Project project, MavenPlugin mavenPlugin, MavenActionGroup pluginGroup, MavenProjectInfo mavenProject) {
		MavenPluginInfo pluginInfo = MavenArtifactUtil.readPluginInfo(
			MavenProjectsManager.getInstance(project).getLocalRepository(), mavenPlugin.getMavenId());
		if (pluginInfo != null) {
			for (MavenPluginInfo.Mojo mojo : pluginInfo.getMojos()) {
				pluginGoalsSet.add(mojo.getDisplayName());
				pluginGroup.add(createGoalRunAction(new Goal(mojo.getDisplayName()), MyIcons.PLUGIN_GOAL, true, mavenProject));
			}
		}
	}

	protected AnAction createGoalRunAction(Goal goal, final Icon icon, boolean plugin, MavenProjectInfo mavenProject) {
		return RunGoalAction.create(goal, icon, true, mavenProject);
	}

}
