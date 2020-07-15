package krasa.mavenhelper.model;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.CommandLineTokenizer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import krasa.mavenhelper.action.Utils;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.StringTokenizer;

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

	public List<String> parse(PsiFile psiFile, ConfigurationContext configurationContext) {
		String cmd = getCommandLine();
		cmd = ApplicationSettings.get().applyAliases(cmd, psiFile, configurationContext);

		return ContainerUtil.newArrayList(StringUtil.tokenize(new CommandLineTokenizer(cmd)));
	}
}
