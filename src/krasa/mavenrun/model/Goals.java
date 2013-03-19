package krasa.mavenrun.model;

import java.util.ArrayList;
import java.util.List;

public class Goals extends DomainObject {
	private String commandLine;
	private List<Goal> goals = new ArrayList<Goal>();

	public String getCommandLine() {
		return commandLine;
	}

	public void setCommandLine(String commandLine) {
		this.commandLine = commandLine;
	}

	public List<Goal> getGoals() {
		return goals;
	}

	public void setGoals(List<Goal> goals) {
		this.goals = goals;
	}

	public List<String> asStrings() {
		return getStrings(goals);
	}

	public boolean remove(Goal o) {
		return goals.remove(o);
	}

	public boolean add(Goal o) {
		return goals.add(o);
	}

	private List<String> getStrings(final List<Goal> goals1) {
		List<String> strings = new ArrayList<String>();
		for (Goal goal : goals1) {
			strings.add(goal.getCommandLine());
		}
		return strings;
	}

}
