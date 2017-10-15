package krasa.mavenhelper.model;

import com.rits.cloning.Cloner;
import org.jetbrains.idea.maven.model.MavenConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ApplicationSettings extends DomainObject implements Cloneable {
	private static final Collection<String> BASIC_PHASES = MavenConstants.BASIC_PHASES;
	public static final int ACTUAL_VERSION = 1;

	int version = 0;
	private boolean findNearbyPom = false;
	private Goals goals = new Goals();
	private Goals pluginAwareGoals = new Goals();

	public boolean isFindNearbyPom() {
		return findNearbyPom;
	}

	public void setFindNearbyPom(boolean findNearbyPom) {
		this.findNearbyPom = findNearbyPom;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Goals getPluginAwareGoals() {
		return pluginAwareGoals;
	}

	public void setPluginAwareGoals(Goals pluginAwareGoals) {
		this.pluginAwareGoals = pluginAwareGoals;
	}

	public Goals getGoals() {
		return goals;
	}

	public void setGoals(Goals goals) {
		this.goals = goals;
	}

	public static ApplicationSettings defaultApplicationSettings() {
		ApplicationSettings applicationSettings = new ApplicationSettings();
		Goals pluginAwareGoals = new Goals();
		pluginAwareGoals.add("jetty:run");
		pluginAwareGoals.add("tomcat:run");
		pluginAwareGoals.add("tomcat5:run");
		pluginAwareGoals.add("tomcat6:run");
		pluginAwareGoals.add("tomcat7:run");
		applicationSettings.setPluginAwareGoals(pluginAwareGoals);

		Goals goals = new Goals();
		for (String basicPhase : BASIC_PHASES) {
			goals.add(new Goal(basicPhase));
		}
		goals.add(new Goal("clean install"));
		applicationSettings.setGoals(goals);
		applicationSettings.setVersion(ACTUAL_VERSION);
		applicationSettings.setFindNearbyPom(false);
		return applicationSettings;
	}

	public List<Goal> getAllGoals() {
		List<Goal> allGoals = new ArrayList<Goal>(goals.size() + pluginAwareGoals.size());
		allGoals.addAll(goals.getGoals());
		allGoals.addAll(pluginAwareGoals.getGoals());
		return allGoals;
	}

	public List<String> getAllGoalsAsString() {
		List<String> strings = new ArrayList<String>();
		List<Goal> allGoals = getAllGoals();
		for (Goal allGoal : allGoals) {
			strings.add(allGoal.getCommandLine());
		}
		return strings;
	}

	@Override
	public ApplicationSettings clone() {
		Cloner cloner = new Cloner();
		cloner.nullInsteadOfClone();
		return cloner.deepClone(this);
	}

	public String[] getAllGoalsAsStringArray() {
		return toArray(getAllGoalsAsString());
	}

	private String[] toArray(List<String> goalsAsStrings) {
		return goalsAsStrings.toArray(new String[goalsAsStrings.size()]);
	}

	public boolean removeGoal(Goal goal) {
		boolean remove = goals.remove(goal);
		if (!remove) {
			remove = pluginAwareGoals.remove(goal);
		}
		return remove;
	}
}
