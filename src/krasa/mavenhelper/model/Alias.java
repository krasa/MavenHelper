package krasa.mavenhelper.model;

public class Alias extends DomainObject {
	private String from;
	private String to;

	public Alias() {
	}

	public Alias(String from, String to) {
		this.from = from;
		this.to = to;
	}

	public static Alias of(String name, String value) {
		return new Alias(name, value);
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String applyTo(String commandLine) {
		if (from != null && to != null) {
			return commandLine.replace(from, to);
		}
		return commandLine;
	}

}
