package krasa.mavenhelper.analyzer;

import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.model.MavenArtifactState;

import java.util.List;
import java.util.Map;

/**
 * @author Vojtech Krasa
 */
public class MyListNode {

	protected final String key;
	protected final List<MavenArtifactNode> value;
	protected String rightVersion;
	protected boolean conflict;

	public MyListNode(Map.Entry<String, List<MavenArtifactNode>> s) {
		key = s.getKey();
		value = s.getValue();
		initRightVersion();
		initConflict();
	}

	private void initRightVersion() {
		if (value != null && !value.isEmpty()) {
			for (MavenArtifactNode mavenArtifactNode : value) {
				if (mavenArtifactNode.getState() == MavenArtifactState.ADDED) {
					rightVersion = mavenArtifactNode.getArtifact().getVersion();
					break;
				}
			}
		}
	}

	private void initConflict() {
		if (value != null && !value.isEmpty()) {
			for (MavenArtifactNode mavenArtifactNode : value) {
				if (Utils.isOmitted(mavenArtifactNode) || Utils.isConflictAlternativeMethod(mavenArtifactNode)) {
					conflict = true;
					break;
				}
			}
		}
	}

	public boolean isConflict() {
		return conflict;
	}

	public String getRightVersion() {
		return rightVersion;
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
