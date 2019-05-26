package krasa.mavenhelper.model;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.psi.PsiFile;
import krasa.mavenhelper.action.Utils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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

	public List<String> parse(PsiFile psiFile, ConfigurationContext configurationContext) {
		String cmd = getCommandLine();
		cmd = ApplicationSettings.get().applyAliases(cmd, psiFile, configurationContext);

		List<String> strings = new ArrayList<String>();
		String[] split = cmd.split("\\s");
		for (String s : split) {
			if (StringUtils.isNotBlank(s)) {
				strings.add(s);
			}
		}
//		return ContainerUtil.newArrayList(StringUtil.tokenize(new StringTokenizer(cmd)));
		return strings;
	}
}
