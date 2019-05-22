package krasa.mavenhelper.model;

import com.intellij.openapi.diagnostic.Logger;

public class Alias {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(Alias.class);
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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Alias alias = (Alias) o;

		if (from != null ? !from.equals(alias.from) : alias.from != null) return false;
		return to != null ? to.equals(alias.to) : alias.to == null;

	}

	@Override
	public int hashCode() {
		int result = from != null ? from.hashCode() : 0;
		result = 31 * result + (to != null ? to.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "Alias{" +
			"from='" + from + '\'' +
			", to='" + to + '\'' +
			'}';
	}
}
