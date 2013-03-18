package krasa.mavenrun.model;

public class Goal {
	private String commandLine;

	public Goal() {
	}

	public Goal(String s) {
		commandLine = s;
	}

	public String getCommandLine() {
		return commandLine;
	}

	public void setCommandLine(String commandLine) {
		this.commandLine = commandLine;
	}
}
