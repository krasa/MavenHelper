package krasa.mavenrun.action;

import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.ProjectRunConfigurationManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import icons.MavenIcons;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunConfiguration;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.model.MavenConstants;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.navigator.MavenProjectsNavigator;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenArtifactUtil;
import org.jetbrains.idea.maven.utils.MavenPluginInfo;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

public class MainMavenActionGroup extends ActionGroup implements DumbAware {
	private static final Collection<String> BASIC_PHASES = MavenConstants.BASIC_PHASES;
	
	public MainMavenActionGroup(String shortName, Icon icon) {
		super(shortName, true);
		getTemplatePresentation().setIcon(icon);
	}


	@NotNull
	@Override
	public AnAction[] getChildren(@Nullable AnActionEvent e) {
		ArrayList<AnAction> anActions = new ArrayList<AnAction>();
		if (e != null && MavenActionUtil.getMavenProject(e.getDataContext()) != null) {
//			addCustomActions(e, anActions);
//			anActions.add(new Separator());
			addDefaultGoals(anActions);
			anActions.add(new Separator());
			addPlugins(e, anActions);

		}
		return anActions.toArray(new AnAction[anActions.size()]);
	}

	private void addCustomActions(AnActionEvent e, ArrayList<AnAction> anActions) {
		RunConfiguration[] configurations =  RunManager.getInstance(getEventProject(e)).getConfigurations(MavenRunConfigurationType.getInstance());
		for (RunConfiguration configuration : configurations) {
			final MavenRunConfiguration mavenRunConfiguration = (MavenRunConfiguration) configuration;
			anActions.add(new AnAction(mavenRunConfiguration.getName(),mavenRunConfiguration.getName(), MavenIcons.Phase) {
				@Override
				public void actionPerformed(AnActionEvent e) {
					MavenRunConfigurationType.runConfiguration(MavenActionUtil.getProject(e.getDataContext()), mavenRunConfiguration.getRunnerParameters(), null);

				}
			});
		}
	}

	private void addDefaultGoals(ArrayList<AnAction> anActions) {
		for (String basicPhase : BASIC_PHASES) {
			anActions.add(createGoalRunAction(basicPhase));
		}
	}

	private void addPlugins(AnActionEvent e, ArrayList<AnAction> anActions) {
		Project project = e.getProject();
		MavenProjectsNavigator.getInstance(project).getState();
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		for (MavenPlugin mavenPlugin : mavenProject.getDeclaredPlugins()) {
			MavenActionGroup plugin = new MavenActionGroup(mavenPlugin.getArtifactId(), true);
			plugin.getTemplatePresentation().setIcon(MavenIcons.PhasesClosed);
			addPluginGoals(project, mavenPlugin, plugin);
			anActions.add(plugin);
		}
	}

	private void addPluginGoals(Project project, MavenPlugin mavenPlugin, MavenActionGroup plugin) {
		MavenPluginInfo pluginInfo = MavenArtifactUtil.readPluginInfo(MavenProjectsManager.getInstance(project).getLocalRepository(), mavenPlugin.getMavenId());
		if (pluginInfo != null) {
			for (MavenPluginInfo.Mojo mojo : pluginInfo.getMojos()) {
				plugin.add(new MavenGoalRunAction(mojo.getDisplayName(), MavenIcons.PluginGoal));
			}
		}
	}

	protected MavenGoalRunAction createGoalRunAction(String basicPhase) {
		return new MavenGoalRunAction(basicPhase, MavenIcons.Phase);
	}
}
