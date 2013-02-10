package krasa.mavenrun.model;

import java.util.ArrayList;
import java.util.List;

public class Profile {
	private List<Goal> goals = new ArrayList<Goal>();
	
		public boolean remove(Goal o) {
			return goals.remove(o);
		}
	
		public boolean add(Goal o) {
			return goals.add(o);
		}
	
		public List<Goal> getGoals() {
			return goals;
		}
}
