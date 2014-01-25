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

	public MyListNode(Map.Entry<String, List<MavenArtifactNode>> s) {
		key = s.getKey();
		value = s.getValue();
	}

	@Override
	public String toString() {
		return key;
	}

}
