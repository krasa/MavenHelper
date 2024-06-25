package krasa.mavenhelper.model;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.CommandLineTokenizer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import krasa.mavenhelper.action.MavenProjectInfo;
import krasa.mavenhelper.action.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.util.List;

public class Goal extends DomainObject {
	private String commandLine;

	public Goal() {
	}

	public Goal(@NotNull String s) {
		commandLine = s.trim();
	}

	public String getCommandLine() {
		return commandLine;
	}

	public void setCommandLine(String commandLine) {
		this.commandLine = commandLine;
	}

	public String getPresentableName() {
		return Utils.limitLength(commandLine);
	}

	public List<String> parse(PsiFile psiFile, ConfigurationContext configurationContext, @NotNull MavenProjectInfo mavenProjectInfo, MavenProjectsManager manager) {
		String cmd = getCommandLine();
		cmd = ApplicationSettings.get().applyAliases(cmd, psiFile, configurationContext, mavenProjectInfo, manager);

		if (ApplicationSettings.get().isUseTerminalCommand()) {
			return List.of(cmd);
		}

		return ContainerUtil.newArrayList(StringUtil.tokenize(new CommandLineTokenizer(cmd)));
	}
}
