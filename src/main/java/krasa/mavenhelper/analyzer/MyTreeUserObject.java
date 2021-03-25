package krasa.mavenhelper.analyzer;

import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Enumeration;

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

	public long getTotalSize(DefaultMutableTreeNode artifact) {
		if (totalSize == null) {
			Long size = getSize();
			Enumeration<TreeNode> children = artifact.children();
			while (children.hasMoreElements()) {
				DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) children.nextElement();
				MyTreeUserObject child = (MyTreeUserObject) childNode.getUserObject();
				size += child.getTotalSize(childNode);
			}
			totalSize = size;
		}
		return totalSize;
	}
}
