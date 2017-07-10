package krasa.mavenhelper.model;

public class Goal extends DomainObject {
	private String commandLine;

	public Goal() {
	}

	public Goal(String s) {
		commandLine = s.trim();
	}

	public String getCommandLine() {
		return commandLine;
	}

	public void setCommandLine(String commandLine) {
		this.commandLine = commandLine;
	}
}
