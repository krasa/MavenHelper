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
	private Long size;
	private Long totalSize;

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

	public long getSize() {
		if (size == null) {
			size = getArtifact().getFile().length() / 1024;
		}
		return size;
	}

	public long getTotalSize() {
		if (totalSize == null) {
			totalSize = getTotalSize(mavenArtifactNode);
		}
		return totalSize;
	}

	private long getTotalSize(MavenArtifactNode current) {
		long size = current.getArtifact().getFile().length() / 1024;
		for (MavenArtifactNode dependency : current.getDependencies()) {
			size += getTotalSize(dependency);
		}
		return size;
	}
}
