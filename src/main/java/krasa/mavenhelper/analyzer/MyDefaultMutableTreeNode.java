package krasa.mavenhelper.analyzer;

import org.jetbrains.idea.maven.model.MavenArtifact;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Vector;

public class MyDefaultMutableTreeNode extends DefaultMutableTreeNode {
	public static final Comparator<MyDefaultMutableTreeNode> DEEP_SIZE = new Comparator<MyDefaultMutableTreeNode>() {
		@Override
		public int compare(MyDefaultMutableTreeNode t0, MyDefaultMutableTreeNode t1) {
			MyTreeUserObject userObject0 = t0.getUserObject();
			MyTreeUserObject userObject1 = t1.getUserObject();
			return Long.compare(userObject1.getTotalSize(), userObject0.getTotalSize());
		}
	};

	public static final Comparator<MyDefaultMutableTreeNode> GROUP_ID = new Comparator<MyDefaultMutableTreeNode>() {
		@Override
		public int compare(MyDefaultMutableTreeNode t0, MyDefaultMutableTreeNode t1) {
			MavenArtifact a0 = t0.getUserObject().getArtifact();
			MavenArtifact a1 = t1.getUserObject().getArtifact();
			int i = a0.getGroupId().compareTo(a1.getGroupId());
			if (i == 0) {
				i = a0.getArtifactId().compareTo(a1.getArtifactId());
			}
			return i;
		}
	};
	public static final Comparator<MyDefaultMutableTreeNode> ARTIFACT_ID = new Comparator<MyDefaultMutableTreeNode>() {
		@Override
		public int compare(MyDefaultMutableTreeNode t0, MyDefaultMutableTreeNode t1) {
			MavenArtifact a0 = t0.getUserObject().getArtifact();
			MavenArtifact a1 = t1.getUserObject().getArtifact();
			return a0.getArtifactId().compareTo(a1.getArtifactId());
		}
	};
	public static final Comparator<MyDefaultMutableTreeNode> SHALLOW_SIZE = new Comparator<MyDefaultMutableTreeNode>() {
		@Override
		public int compare(MyDefaultMutableTreeNode t0, MyDefaultMutableTreeNode t1) {
			MyTreeUserObject userObject0 = t0.getUserObject();
			MyTreeUserObject userObject1 = t1.getUserObject();
			return Long.compare(userObject1.getSize(), userObject0.getSize());
		}
	};

	public MyDefaultMutableTreeNode() {
	}

	public MyDefaultMutableTreeNode(Object userObject) {
		super(userObject);
	}

	public MyDefaultMutableTreeNode(Object userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
	}

	@Override
	public MyTreeUserObject getUserObject() {
		return (MyTreeUserObject) this.userObject;
	}

	public void sortBySize(Comparator<MyDefaultMutableTreeNode> comparator) {
		Vector children1 = children;
		if (children1 != null) {
			Collections.sort(children1, comparator);
			for (Object child : children1) {
				MyDefaultMutableTreeNode t1 = (MyDefaultMutableTreeNode) child;
				t1.sortBySize(comparator);
			}
		}
	}

	public Enumeration<MyDefaultMutableTreeNode> getChildren() {
		return (Enumeration) super.children();
	}
}
