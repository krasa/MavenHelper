package krasa.mavenrun.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.*;

import krasa.mavenrun.ApplicationComponent;
import krasa.mavenrun.model.Goal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenConstants;
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

public class MainMavenActionGroup extends ActionGroup implements DumbAware {
	private static final Collection<String> BASIC_PHASES = MavenConstants.BASIC_PHASES;
	private Set<String> pluginGoalsSet = new HashSet<String>();

	public MainMavenActionGroup(String shortName, Icon icon) {
		super(shortName, true);
		getTemplatePresentation().setIcon(icon);
	}

	@NotNull
	@Override
	public AnAction[] getChildren(@Nullable AnActionEvent e) {
		ArrayList<AnAction> anActions = new ArrayList<AnAction>();
		if (e != null && MavenActionUtil.getMavenProject(e.getDataContext()) != null) {
			addDefaultGoals(anActions);
			separator(anActions);

			List<MavenActionGroup> mavenActionGroups = getPlugins(e);

			addCustomActions(anActions);
			separator(anActions);

			addPlugins(anActions, mavenActionGroups);
		}
		return anActions.toArray(new AnAction[anActions.size()]);
	}

	private void addPlugins(ArrayList<AnAction> anActions, List<MavenActionGroup> mavenActionGroups) {
		for (MavenActionGroup mavenActionGroup : mavenActionGroups) {
			anActions.add(mavenActionGroup);
		}
	}

	private void separator(ArrayList<AnAction> anActions) {
		if (!anActions.isEmpty()) {
			AnAction anAction = anActions.get(anActions.size() - 1);
			if (!(anAction instanceof Separator)) {
				anActions.add(new Separator());
			}
		}
	}

	private void addDefaultGoals(ArrayList<AnAction> anActions) {
		for (String basicPhase : BASIC_PHASES) {
			anActions.add(createGoalRunAction(basicPhase));
		}
	}

	private void addCustomActions(ArrayList<AnAction> anActions) {
		for (Goal goal : ApplicationComponent.getInstance().getState().getSmartGoals()) {
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
		for (MavenPlugin mavenPlugin : mavenProject.getDeclaredPlugins()) {
			MavenActionGroup plugin = new MavenActionGroup(mavenPlugin.getArtifactId(), true);
			plugin.getTemplatePresentation().setIcon(MavenIcons.PhasesClosed);
			addPluginGoals(project, mavenPlugin, plugin);
			mavenActionGroups.add(plugin);
		}
		return mavenActionGroups;
	}

	private void addPluginGoals(Project project, MavenPlugin mavenPlugin, MavenActionGroup pluginGroup) {
		MavenPluginInfo pluginInfo = MavenArtifactUtil.readPluginInfo(
				MavenProjectsManager.getInstance(project).getLocalRepository(), mavenPlugin.getMavenId());
		if (pluginInfo != null) {
			for (MavenPluginInfo.Mojo mojo : pluginInfo.getMojos()) {
				pluginGoalsSet.add(mojo.getDisplayName());
				pluginGroup.add(new MavenGoalRunAction(mojo.getDisplayName(), MavenIcons.PluginGoal));
			}
		}
	}

	protected MavenGoalRunAction createGoalRunAction(String basicPhase) {
		return new MavenGoalRunAction(basicPhase, MavenIcons.Phase);
	}
}
