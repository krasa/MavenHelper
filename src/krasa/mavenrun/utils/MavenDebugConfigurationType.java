package krasa.mavenrun.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.utils.MavenUtil;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.RunnerRegistry;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMExternalizable;

public class MavenDebugConfigurationType {

	public static void debugConfiguration(Project project, MavenRunnerParameters params,
										  @Nullable ProgramRunner.Callback callback) {
		debugConfiguration(project, params, null, null, callback);
	}

	public static void debugConfiguration(Project project, @NotNull MavenRunnerParameters params,
										  @Nullable MavenGeneralSettings settings, @Nullable MavenRunnerSettings runnerSettings,
										  @Nullable ProgramRunner.Callback callback) {

		RunnerAndConfigurationSettings configSettings = MavenRunConfigurationType.createRunnerAndConfigurationSettings(
				settings, runnerSettings, params, project);

		ProgramRunner runner = RunnerRegistry.getInstance().findRunnerById(DefaultDebugExecutor.EXECUTOR_ID);
		ExecutionEnvironment env = new ExecutionEnvironment(runner, configSettings, project);
		Executor executor = DefaultDebugExecutor.getDebugExecutorInstance();

		try {
			runner.execute(executor, env, callback);
		} catch (ExecutionException e) {
			MavenUtil.showError(project, "Failed to execute Maven goal", e);
		}
	}

}
