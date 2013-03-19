package krasa.mavenrun.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.*;

import krasa.mavenrun.ApplicationComponent;
import krasa.mavenrun.model.ApplicationSettings;
import krasa.mavenrun.model.Goal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.navigator.MavenProjectsNavigator;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenArtifactUtil;
import org.jetbrains.idea.maven.utils.MavenPluginInfo;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import icons.MavenIcons;

@SuppressWarnings("ComponentNotRegistered")
public class MainMavenActionGroup extends ActionGroup implements DumbAware {
	private Set<String> pluginGoalsSet = new HashSet<String>();

	public MainMavenActionGroup(String shortName, Icon icon) {
		super(shortName, true);
		getTemplatePresentation().setIcon(icon);
	}

	@NotNull
	@Override
	public AnAction[] getChildren(@Nullable AnActionEvent e) {
		List<AnAction> result = new ArrayList<AnAction>();
		if (e != null && MavenActionUtil.getMavenProject(e.getDataContext()) != null) {
			addTestFile(result);
			separator(result);

			addGoals(result);
			separator(result);

			List<MavenActionGroup> mavenActionGroups = getPlugins(e);

			addPluginAwareActions(result, mavenActionGroups);
			separator(result);

			addPlugins(result, mavenActionGroups);

			separator(result);
			result.add(new CreateCustomGoalAction("New Goal..."));

		}
		return result.toArray(new AnAction[result.size()]);
	}

	private void addTestFile(List<AnAction> result) {
		result.add(new RunTestFileAction());
	}

	private void addPlugins(List<AnAction> anActions, List<MavenActionGroup> mavenActionGroups) {
		for (MavenActionGroup mavenActionGroup : mavenActionGroups) {
			anActions.add(mavenActionGroup);
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

	private void addGoals(List<AnAction> anActions) {
		for (Goal goal : getState().getGoals().getGoals()) {
			anActions.add(createGoalRunAction(goal.getCommandLine()));
		}
	}

	private ApplicationSettings getState() {
		return ApplicationComponent.getInstance().getState();
	}

	private void addPluginAwareActions(List<AnAction> anActions, List<MavenActionGroup> mavenActionGroups) {
		assert mavenActionGroups != null; // just to be sure that pluginGoalsSet was initialized
		for (Goal goal : getState().getPluginAwareGoals().getGoals()) {
			if (pluginGoalsSet.contains(goal.getCommandLine())) {
				anActions.add(createGoalRunAction(goal.getCommandLine()));
			}
		}
	}

	private List<MavenActionGroup> getPlugins(AnActionEvent e) {
		List<MavenActionGroup> mavenActionGroups = new ArrayList<MavenActionGroup>();
		Project project = e.getProject();
		MavenProjectsNavigator.getInstance(project).getState();
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		if (mavenProject != null) {
			for (MavenPlugin mavenPlugin : mavenProject.getDeclaredPlugins()) {
				MavenActionGroup plugin = new MavenActionGroup(mavenPlugin.getArtifactId(), true);
				plugin.getTemplatePresentation().setIcon(MavenIcons.PhasesClosed);
				addPluginGoals(project, mavenPlugin, plugin);
				mavenActionGroups.add(plugin);
			}
		}
		return mavenActionGroups;
	}

	private void addPluginGoals(Project project, MavenPlugin mavenPlugin, MavenActionGroup pluginGroup) {
		MavenPluginInfo pluginInfo = MavenArtifactUtil.readPluginInfo(
				MavenProjectsManager.getInstance(project).getLocalRepository(), mavenPlugin.getMavenId());
		if (pluginInfo != null) {
			for (MavenPluginInfo.Mojo mojo : pluginInfo.getMojos()) {
				pluginGoalsSet.add(mojo.getDisplayName());
				pluginGroup.add(new RunGoalAction(mojo.getDisplayName(), MavenIcons.PluginGoal));
			}
		}
	}

	protected RunGoalAction createGoalRunAction(String basicPhase) {
		return new RunGoalAction(basicPhase, MavenIcons.Phase);
	}
}
