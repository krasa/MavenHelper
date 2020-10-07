package krasa.mavenhelper.action.debug;

import com.intellij.execution.Executor;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import krasa.mavenhelper.action.RunConfigurationAction;
import org.jetbrains.idea.maven.execution.MavenRunConfiguration;

/**
 * @author Vojtech Krasa
 */
public class DebugConfigurationAction extends RunConfigurationAction {
	public DebugConfigurationAction(Executor executor, boolean enabled, Project project, RunnerAndConfigurationSettings settings) {
		super(executor, enabled, project, settings);
	}

	@Override
	public void actionPerformed(final AnActionEvent event) {
		if (!myEnabled) return;

		final RunnerAndConfigurationSettings configurationSettings = clone(mySettings);
		addForkMode(configurationSettings);

		MavenDebugConfigurationType.debugConfiguration(myProject, null, configurationSettings, myExecutor);
	}

	private RunnerAndConfigurationSettings clone(RunnerAndConfigurationSettings configSettings) {
		RunnerAndConfigurationSettingsImpl runnerAndConfigurationSettings = (RunnerAndConfigurationSettingsImpl) configSettings;
		return runnerAndConfigurationSettings.clone();
	}

	private void addForkMode(RunnerAndConfigurationSettings configSettings) {
		MavenRunConfiguration mavenRunConfiguration = (MavenRunConfiguration) configSettings.getConfiguration();
		mavenRunConfiguration.getRunnerParameters().getGoals().addAll(Debug.DEBUG_FORK_MODE);
	}
}
