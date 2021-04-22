package krasa.mavenhelper.analyzer;

import com.jgoodies.common.collect.ArrayListModel;

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
			int i = t0.getGroupId().compareTo(t1.getGroupId());
			if (i == 0) {
				i = t0.getArtifactId().compareTo(t1.getArtifactId());
			}
			return i;
		}
	};
	public static final Comparator<MyListNode> ARTIFACT_ID = new Comparator<MyListNode>() {
		@Override
		public int compare(MyListNode t0, MyListNode t1) {
			return t0.getArtifactId().compareTo(t1.getArtifactId());
		}
	};
	public static final Comparator<MyListNode> SHALLOW_SIZE = new Comparator<MyListNode>() {
		@Override
		public int compare(MyListNode t0, MyListNode t1) {
			return Long.compare(t1.getSize(), t0.getSize());
		}
	};
}
