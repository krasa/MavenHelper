package krasa.mavenhelper.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Aliases extends DomainObject {
	private List<Alias> aliases = new ArrayList<Alias>();

	public Aliases() {
	}

	public Aliases(List<Alias> aliases) {
		this.aliases = aliases;
	}

	public List<Alias> getAliases() {
		return aliases;
	}

	public void setAliases(List<Alias> aliases) {
		this.aliases = aliases;
	}

	public boolean remove(Alias o) {
		return aliases.remove(o);
	}

	public boolean add(Alias o) {
		return aliases.add(o);
	}


	public void add(String s, String to) {
		aliases.add(new Alias(s, to));
	}

	public int size() {
		return aliases.size();
	}

	public boolean remove(Object goal) {
		return aliases.remove(goal);
	}

	public String applyAliases(String commandLine) {
		for (Alias alias : aliases) {
			commandLine = alias.applyTo(commandLine);
		}
		return commandLine;
	}

	public Map<String, String> asMap() {
		HashMap<String, String> stringStringHashMap = new HashMap<>();
		for (Alias alias : aliases) {
			stringStringHashMap.put(alias.getFrom(), alias.getTo());
		}
		return stringStringHashMap;
	}

}
