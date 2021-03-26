package krasa.mavenhelper.analyzer;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class MyDefaultMutableTreeNode extends DefaultMutableTreeNode {
	public static final Comparator<MyDefaultMutableTreeNode> DEEP_SIZE = new Comparator<MyDefaultMutableTreeNode>() {
		@Override
		public int compare(MyDefaultMutableTreeNode t0, MyDefaultMutableTreeNode t1) {
			MyTreeUserObject userObject0 = (MyTreeUserObject) t0.getUserObject();
			MyTreeUserObject userObject1 = (MyTreeUserObject) t1.getUserObject();
			return Long.compare(userObject1.getTotalSize(), userObject0.getTotalSize());
		}
	};
	public static final Comparator<MyDefaultMutableTreeNode> SHALLOW_SIZE = new Comparator<MyDefaultMutableTreeNode>() {
		@Override
		public int compare(MyDefaultMutableTreeNode t0, MyDefaultMutableTreeNode t1) {
			MyTreeUserObject userObject0 = (MyTreeUserObject) t0.getUserObject();
			MyTreeUserObject userObject1 = (MyTreeUserObject) t1.getUserObject();
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

}
