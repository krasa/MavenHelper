package krasa.mavenhelper.analyzer;

import com.jgoodies.common.collect.ArrayListModel;
import org.jetbrains.idea.maven.model.MavenArtifact;

import java.util.Comparator;

public class MyDefaultListModel extends ArrayListModel<MyListNode> {
	public static final Comparator<MyListNode> DEEP_SIZE = new Comparator<MyListNode>() {
		@Override
		public int compare(MyListNode t0, MyListNode t1) {
			return Long.compare(t1.getTotalSize(), t0.getTotalSize());
		}
	};
	public static final Comparator<MyListNode> GROUP_ID = new Comparator<MyListNode>() {
		@Override
		public int compare(MyListNode t0, MyListNode t1) {
			MavenArtifact a0 = t0.getRightArtifact().getArtifact();
			MavenArtifact a1 = t1.getRightArtifact().getArtifact();
			int i = a0.getGroupId().compareTo(a1.getGroupId());
			if (i == 0) {
				i = a0.getArtifactId().compareTo(a1.getArtifactId());
			}
			return i;
		}
	};
	public static final Comparator<MyListNode> ARTIFACT_ID = new Comparator<MyListNode>() {
		@Override
		public int compare(MyListNode t0, MyListNode t1) {
			MavenArtifact a0 = t0.getRightArtifact().getArtifact();
			MavenArtifact a1 = t1.getRightArtifact().getArtifact();
			return a0.getArtifactId().compareTo(a1.getArtifactId());
		}
	};
	public static final Comparator<MyListNode> SHALLOW_SIZE = new Comparator<MyListNode>() {
		@Override
		public int compare(MyListNode t0, MyListNode t1) {
			return Long.compare(t1.getSize(), t0.getSize());
		}
	};
}
