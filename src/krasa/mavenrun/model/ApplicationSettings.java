package krasa.mavenrun.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.idea.maven.model.MavenConstants;

public class ApplicationSettings implements Cloneable {
	private static final Collection<String> BASIC_PHASES = MavenConstants.BASIC_PHASES;

	int version = 0;
	private List<Goal> goals = new ArrayList<Goal>();
	private List<Goal> pluginAwareGoals = new ArrayList<Goal>();

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public boolean remove(Goal o) {
		return goals.remove(o);
	}

	public boolean add(Goal o) {
		return goals.add(o);
	}

	public void setGoals(List<Goal> goals) {
		this.goals = goals;
	}

	public void setPluginAwareGoals(List<Goal> pluginAwareGoals) {
		this.pluginAwareGoals = pluginAwareGoals;
	}

	public List<Goal> getGoals() {
		return goals;
	}

	public static ApplicationSettings defaultApplicationSettings() {
		ApplicationSettings applicationSettings = new ApplicationSettings();
		applicationSettings.addPluginAwareGoals("jetty:run");
		applicationSettings.addPluginAwareGoals("tomcat:run");
		applicationSettings.addPluginAwareGoals("tomcat5:run");
		applicationSettings.addPluginAwareGoals("tomcat6:run");
		applicationSettings.addPluginAwareGoals("tomcat7:run");

		for (String basicPhase : BASIC_PHASES) {
			applicationSettings.add(new Goal(basicPhase));
		}
		return applicationSettings;

	}

	public List<Goal> getPluginAwareGoals() {
		return pluginAwareGoals;
	}

	private void addPluginAwareGoals(String s) {
		pluginAwareGoals.add(new Goal(s));
	}

	public List<String> getGoalsAsStrings() {
		List<String> strings = new ArrayList<String>();
		for (Goal goal : goals) {
			strings.add(goal.getCommandLine());
		}
		return strings;
	}
}
