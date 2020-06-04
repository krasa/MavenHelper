package krasa.mavenhelper.analyzer;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.model.MavenArtifactState;

public class Utils {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(Utils.class);

	//  myRelatedArtifact is a conflict winner, null means there was no conflict (probably) 
	public static boolean isVersionMatch(MavenArtifactNode mavenArtifactNode) {
		return mavenArtifactNode.getRelatedArtifact() != null && mavenArtifactNode.getRelatedArtifact().getVersion().equals(mavenArtifactNode.getArtifact().getVersion());
	}

	public static boolean isVersionMismatch(MavenArtifactNode mavenArtifactNode) {
		return mavenArtifactNode.getRelatedArtifact() != null && !mavenArtifactNode.getRelatedArtifact().getVersion().equals(mavenArtifactNode.getArtifact().getVersion());
	}

	static boolean isOmitted(MavenArtifactNode mavenArtifactNode) {
		return mavenArtifactNode.getState() == MavenArtifactState.CONFLICT || isVersionMismatch(mavenArtifactNode);
	}

	static boolean isConflictAlternativeMethod(MavenArtifactNode mavenArtifactNode) {
		return mavenArtifactNode.getState() != MavenArtifactState.ADDED && mavenArtifactNode.getState() != MavenArtifactState.EXCLUDED && !isVersionMatch(mavenArtifactNode);
	}
}
