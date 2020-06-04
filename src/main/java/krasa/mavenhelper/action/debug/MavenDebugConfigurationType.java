package krasa.mavenhelper.action.debug;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.utils.MavenUtil;

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
		final Executor executor = DefaultDebugExecutor.getDebugExecutorInstance();

		debugConfiguration(project, callback, configSettings, executor);
	}

	public static void debugConfiguration(Project project, ProgramRunner.Callback callback,
			RunnerAndConfigurationSettings configSettings, Executor executor) {
		ProgramRunner runner = ProgramRunner.findRunnerById(DefaultDebugExecutor.EXECUTOR_ID);
		ExecutionEnvironment env = new ExecutionEnvironment(executor, runner, configSettings, project);

		try {
			runner.execute(env, callback);
		} catch (ExecutionException e) {
			MavenUtil.showError(project, "Failed to execute Maven goal", e);
		}
	}

}
