package krasa.mavenhelper.action;

import com.intellij.execution.*;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.sh.run.ShConfigurationType;
import com.intellij.sh.run.ShRunConfiguration;
import krasa.mavenhelper.model.ApplicationSettings;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunConfiguration;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenGeneralSettings;
import org.jetbrains.idea.maven.utils.MavenUtil;

import java.util.Map;

public class ProgramRunnerUtils {
	static void run(Project project, MavenRunnerParameters params) {
		ApplicationSettings applicationSettings = ApplicationSettings.get();
		if (applicationSettings.isUseTerminalCommand()) {
			executeInTerminal(params, project, applicationSettings);
		} else {
			MavenRunConfigurationType.runConfiguration(project, params, null);
		}
	}

	public static void debugConfiguration(Project project, MavenRunnerParameters params,
										  @Nullable ProgramRunner.Callback callback) {
		debugConfiguration(project, params, null, null, callback);
	}

	private static void debugConfiguration(Project project, @NotNull MavenRunnerParameters params,
										   @Nullable MavenGeneralSettings settings, @Nullable MavenRunnerSettings runnerSettings,
										   @Nullable ProgramRunner.Callback callback) {

		RunnerAndConfigurationSettings configSettings = MavenRunConfigurationType.createRunnerAndConfigurationSettings(
				settings, runnerSettings, params, project);
		final Executor executor = DefaultDebugExecutor.getDebugExecutorInstance();

		ApplicationSettings applicationSettings = ApplicationSettings.get();
		if (applicationSettings.isUseTerminalCommand()) {
			executeInTerminal(params, project, applicationSettings);
		} else {
			debugConfiguration(project, callback, configSettings, executor);
		}
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

	public static void executeInTerminal(MavenRunnerParameters params, Project project, ApplicationSettings applicationSettings) {
		if (!isShellEnabled()) {
			com.intellij.openapi.ui.Messages.showErrorDialog(project, "Shell Script plugin not enabled, enable it and restart the IDE (or disable using Terminal in Settings | Maven Helper)", "Maven Helper");
			return;
		}
		if (!OpenTerminalAction.isTerminalEnabled()) {
			com.intellij.openapi.ui.Messages.showErrorDialog(project, "Terminal plugin not enabled, enable it and restart the IDE (or disable using Terminal in Settings | Maven Helper)", "Maven Helper");
			return;
		}

		String commandLine = params.getCommandLine();
		Map<String, Boolean> profilesMap = params.getProfilesMap();
		if (!profilesMap.isEmpty()) {
			commandLine += " -P " + encodeProfiles(profilesMap);
		}

		RunnerAndConfigurationSettings configurationSettings = RunManager.getInstance(project).createConfiguration(applicationSettings.getTerminalCommand() + " " + commandLine, ShConfigurationType.class);
		RunConfiguration configuration = configurationSettings.getConfiguration();
		if (!(configuration instanceof ShRunConfiguration)) {
			PluginDescriptor pluginByClass = PluginManager.getPluginByClass(configuration.getClass());
			String name = pluginByClass == null ? "unknown" : pluginByClass.getName();
			com.intellij.openapi.ui.Messages.showErrorDialog(project, "Conflict detected with plugin: " + name + " (disable using Terminal in Settings | Maven Helper)", "Maven Helper");
			return;
		}
		ShRunConfiguration runConfiguration = (ShRunConfiguration) configurationSettings.getConfiguration();
		runConfiguration.setScriptPath(applicationSettings.getTerminalCommand());
		runConfiguration.setScriptOptions(commandLine);
		runConfiguration.setExecuteScriptFile(true);
		runConfiguration.setScriptWorkingDirectory(params.getWorkingDirPath());
//    if (file instanceof ShFile) {
//      @NlsSafe String defaultShell = ObjectUtils.notNull(ShConfigurationType.getDefaultShell(), "/bin/sh");
//      String shebang = ShShebangParserUtil.getShebangExecutable((ShFile)file);
//      if (shebang != null) {
//        Pair<String, String> result = parseInterpreterAndOptions(shebang);
//        runConfiguration.setInterpreterPath(result.first);
//        runConfiguration.setInterpreterOptions(result.second);
//      } else {
//        runConfiguration.setInterpreterPath(defaultShell);
//      }
//    }
//    else {
		runConfiguration.setInterpreterPath("");
//    }

		ExecutionEnvironmentBuilder builder =
				ExecutionEnvironmentBuilder.createOrNull(DefaultRunExecutor.getRunExecutorInstance(), runConfiguration);
		if (builder != null) {
			ExecutionManager.getInstance(project).restartRunProfile(builder.build());
		}
	}

	private static boolean isShellEnabled() {
		IdeaPluginDescriptor plugin = PluginManager.getInstance().findEnabledPlugin(PluginId.getId("com.jetbrains.sh"));
		return plugin != null && plugin.isEnabled();
	}

//	private static @NotNull
//	Pair<String, String> parseInterpreterAndOptions(@NotNull String shebang) {
//		String[] splitShebang = shebang.split(" ");
//		if (splitShebang.length > 1) {
//			String shebangParam = splitShebang[splitShebang.length - 1];
//			if (!shebangParam.contains("/") && !shebangParam.contains("\\")) {
//				return Pair.create(shebang.substring(0, shebang.length() - shebangParam.length() - 1), shebangParam);
//			}
//		}
//		return Pair.create(shebang, "");
//	}

	public static void executeDebugConfiguration(Project myProject, Executor myExecutor, RunnerAndConfigurationSettings mySettings) {
		ApplicationSettings applicationSettings = ApplicationSettings.get();
		if (applicationSettings.isUseTerminalCommand()) {
			MavenRunConfiguration configuration = (MavenRunConfiguration) mySettings.getConfiguration();
			MavenRunnerParameters params = configuration.getRunnerParameters();
			//TODO add debug remote debug params?
			executeInTerminal(params, myProject, applicationSettings);
		} else {
			ProgramRunnerUtils.debugConfiguration(myProject, null, mySettings, myExecutor);
		}
	}

	static void executeConfiguration(Project myProject, Executor myExecutor, RunnerAndConfigurationSettings mySettings) {
		ApplicationSettings applicationSettings = ApplicationSettings.get();
		if (applicationSettings.isUseTerminalCommand()) {
			MavenRunConfiguration configuration = (MavenRunConfiguration) mySettings.getConfiguration();
			MavenRunnerParameters params = configuration.getRunnerParameters();
			executeInTerminal(params, myProject, applicationSettings);
		} else {
			ProgramRunnerUtil.executeConfiguration(mySettings, myExecutor);
		}
	}

	@ApiStatus.Internal
	public static String encodeProfiles(Map<String, Boolean> profiles) {
		StringBuilder stringBuilder = new StringBuilder();
		for (Map.Entry<String, Boolean> entry : profiles.entrySet()) {
			if (stringBuilder.length() != 0) {
				stringBuilder.append(",");
			}
			if (!entry.getValue()) {
				stringBuilder.append("!");
			}
			stringBuilder.append(entry.getKey());
		}
		return stringBuilder.toString();
	}


}
