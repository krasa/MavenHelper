package krasa.mavenrun.model;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSettings {
	private List<Profile> goals = new ArrayList<Profile>();

	public boolean remove(Profile o) {
		return goals.remove(o);
	}

	public boolean add(Profile o) {
		return goals.add(o);
	}

	public List<Profile> getGoals() {
		return goals;
	}
}
