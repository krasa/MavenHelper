package krasa.mavenhelper.analyzer;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.model.MavenArtifactState;

import java.util.List;
import java.util.Map;

/**
 * @author Vojtech Krasa
 */
public class MyListNode {
	private static final Logger LOG = Logger.getInstance(MyListNode.class);

	protected final String artifactKey;
	private List<MavenArtifactNode> artifacts;
	@Nullable
	protected MavenArtifactNode rightArtifact;
	protected boolean conflict;
	private Long size;
	private Long totalSize;
	private String groupId;
	private String artifactId;

	public MyListNode(Map.Entry<String, List<MavenArtifactNode>> s) {
		artifactKey = s.getKey();
		artifacts = s.getValue();
		initRightArtifact();
		initConflict();
	}

	public List<MavenArtifactNode> getArtifacts() {
		return artifacts;
	}

	@Nullable
	public MavenArtifactNode getRightArtifact() {
		return rightArtifact;
	}

	public long getSize() {
		if (size == null) {
			if (rightArtifact != null) {
				size = rightArtifact.getArtifact().getFile().length() / 1024;
			} else {
				size = -1L;
			}
		}
		return size;
	}

	public long getTotalSize() {
		if (totalSize == null) {
			totalSize = getTotalSize(rightArtifact);
		}
		return totalSize;
	}

	private long getTotalSize(MavenArtifactNode current) {
		if (current == null) {
			return -1;
		}
		long size = current.getArtifact().getFile().length() / 1024;
		for (MavenArtifactNode dependency : current.getDependencies()) {
			size += getTotalSize(dependency);
		}
		return size;
	}

	private void initRightArtifact() {
		if (artifacts != null && !artifacts.isEmpty()) {
			for (MavenArtifactNode mavenArtifactNode : artifacts) {

				groupId = mavenArtifactNode.getArtifact().getGroupId();
				artifactId = mavenArtifactNode.getArtifact().getArtifactId();

				if (mavenArtifactNode.getState() == MavenArtifactState.ADDED) {
					rightArtifact = mavenArtifactNode;
					break;
				}
			}
			if (LOG.isDebugEnabled()) {
				if (rightArtifact == null) {
					StringBuilder sb = new StringBuilder(artifactKey + "[");
					for (MavenArtifactNode artifact : artifacts) {
						sb.append(artifact.getArtifact());
						sb.append("-");
						sb.append(artifact.getState());
						sb.append(";");
					}
					sb.append("]");

					LOG.debug(sb.toString());
				}
			}

		}
	}

	private void initConflict() {
		if (artifacts != null && !artifacts.isEmpty()) {
			for (MavenArtifactNode mavenArtifactNode : artifacts) {
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

	@Nullable
	public String getRightVersion() {
		if (rightArtifact == null) {
			return null;
		}
		return rightArtifact.getArtifact().getVersion();
	}

	@Override
	public String toString() {
		return artifactKey;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		MyListNode that = (MyListNode) o;

		if (artifactKey != null ? !artifactKey.equals(that.artifactKey) : that.artifactKey != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return artifactKey != null ? artifactKey.hashCode() : 0;
	}

	public String getGroupId() {
		return groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}
}
