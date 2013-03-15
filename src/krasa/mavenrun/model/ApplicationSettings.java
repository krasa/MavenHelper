package krasa.mavenrun.model;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSettings {
	private List<Goal> goals = new ArrayList<Goal>();
	private List<Goal> smartGoals = new ArrayList<Goal>();

	public boolean remove(Goal o) {
		return goals.remove(o);
	}

	public boolean add(Goal o) {
		return goals.add(o);
	}

	public List<Goal> getGoals() {
		return goals;
	}


	public static ApplicationSettings defaultApplicationSettings() {
		ApplicationSettings applicationSettings = new ApplicationSettings();
		applicationSettings.addSmartGoal("jetty:run");
		applicationSettings.addSmartGoal("tomcat:run");
		applicationSettings.addSmartGoal("tomcat5:run");
		applicationSettings.addSmartGoal("tomcat6:run");
		applicationSettings.addSmartGoal("tomcat7:run");
		return applicationSettings;

	}

	public List<Goal> getSmartGoals() {
		return smartGoals;
	}

	private void addSmartGoal(String s) {
		smartGoals.add(new Goal(s));
	}

}
