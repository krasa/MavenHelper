package krasa.mavenhelper.analyzer;

import com.jgoodies.common.collect.ArrayListModel;

import java.util.Comparator;

public class MyDefaultListModel extends ArrayListModel {
	public static final Comparator<MyListNode> DEEP_SIZE = new Comparator<MyListNode>() {
		@Override
		public int compare(MyListNode t0, MyListNode t1) {
			return Long.compare(t1.getTotalSize(), t0.getTotalSize());
		}
	};
	public static final Comparator<MyListNode> SHALLOW_SIZE = new Comparator<MyListNode>() {
		@Override
		public int compare(MyListNode t0, MyListNode t1) {
			return Long.compare(t1.getSize(), t0.getSize());
		}
	};
}
