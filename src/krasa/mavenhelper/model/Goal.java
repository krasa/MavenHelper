package krasa.mavenhelper.model;

import krasa.mavenhelper.action.Utils;
import org.jetbrains.annotations.NotNull;

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
}
