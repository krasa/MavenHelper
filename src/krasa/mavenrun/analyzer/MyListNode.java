package krasa.mavenrun.analyzer;

import java.util.List;
import java.util.Map;

import org.jetbrains.idea.maven.model.MavenArtifactNode;

/**
 * @author Vojtech Krasa
 */
public class MyListNode {

	protected final String key;
	protected final List<MavenArtifactNode> value;
	protected String maxVersion;

	public MyListNode(Map.Entry<String, List<MavenArtifactNode>> s) {
		key = s.getKey();
		value = s.getValue();
		maxVersion = GuiForm.sortByVersion(value);
	}

	public String getMaxVersion() {
		return maxVersion;
	}

	@Override
	public String toString() {
		return key;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		MyListNode that = (MyListNode) o;

		if (key != null ? !key.equals(that.key) : that.key != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return key != null ? key.hashCode() : 0;
	}
}
