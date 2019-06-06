package krasa.mavenhelper.analyzer;

import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;

/**
 * @author Vojtech Krasa
 */
public class MyTreeUserObject {

	private MavenArtifactNode mavenArtifactNode;

	boolean showOnlyVersion = false;
	boolean highlight;

	public MyTreeUserObject(MavenArtifactNode mavenArtifactNode) {
		this.mavenArtifactNode = mavenArtifactNode;
	}


    public MavenArtifact getArtifact() {
		return mavenArtifactNode.getArtifact();
	}

	public MavenArtifactNode getMavenArtifactNode() {
		return mavenArtifactNode;
	}

	public boolean isHighlight() {
		return highlight;
	}

	@Override
	public String toString() {
		return mavenArtifactNode.getArtifact().getArtifactId();
	}

}
