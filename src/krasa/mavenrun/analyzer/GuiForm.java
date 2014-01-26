package krasa.mavenrun.analyzer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBList;
import com.intellij.ui.treeStructure.Tree;

/**
 * @author Vojtech Krasa
 */
public class GuiForm {
	private static final Logger LOG = Logger.getInstance("#krasa.mavenrun.analyzer.GuiForm");

	private final Project project;
	private final VirtualFile file;
	private MavenProject mavenProject;
	private JBList list;
	private JTree tree;
	private JPanel rootPanel;
	private JRadioButton allDependenciesRadioButton;
	private JRadioButton conflictsRadioButton;
	private JLabel noConflictsLabel;
	private JButton refreshButton;
	private JSplitPane splitPane;
	protected DefaultListModel listDataModel;
	protected Map<String, List<MavenArtifactNode>> allArtifactsMap;
	protected DefaultTreeModel treeModel;
	protected DefaultMutableTreeNode treeRoot;

	public GuiForm(final Project project, VirtualFile file, final MavenProject mavenProject) {
		this.project = project;
		this.file = file;
		this.mavenProject = mavenProject;
		final ActionListener l = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateListModel(allArtifactsMap);
			}
		};
		conflictsRadioButton.addActionListener(l);
		allDependenciesRadioButton.addActionListener(l);
		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initializeModel();
				rootPanel.requestFocus();
			}
		});
		tree.addMouseListener(new TreePopupHandler(project, mavenProject, tree));
	}

	private void createUIComponents() {
		listDataModel = new DefaultListModel();
		list = createJBList(listDataModel);
		tree = new Tree();
		treeRoot = new DefaultMutableTreeNode();
		treeModel = new DefaultTreeModel(treeRoot);
		tree.setModel(treeModel);

		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.expandPath(new TreePath(treeRoot.getPath()));
		tree.setCellRenderer(new TreeRenderer());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}

	private class MyListSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (listDataModel.isEmpty() || list.getSelectedValue() == null) {
				return;
			}
			treeRoot.removeAllChildren();

			final MyListNode myListNode = (MyListNode) list.getSelectedValue();
			final List<MavenArtifactNode> value = myListNode.value;

			fillTree(value);
			expandAll(tree, new TreePath(treeRoot.getPath()));
		}

		private void fillTree(List<MavenArtifactNode> mavenArtifactNodes) {
			String maxVersion = sortByVersion(mavenArtifactNodes);
			for (MavenArtifactNode mavenArtifactNode : mavenArtifactNodes) {
				final DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(MyTreeUserObject.create(
						mavenArtifactNode, maxVersion));
				fill(mavenArtifactNode, newNode);
				treeRoot.add(newNode);
	}
			treeModel.nodeStructureChanged(treeRoot);
		}

		private String sortByVersion(List<MavenArtifactNode> value) {
			Collections.sort(value, new Comparator<MavenArtifactNode>() {
				@Override
				public int compare(MavenArtifactNode o1, MavenArtifactNode o2) {
					DefaultArtifactVersion version = new DefaultArtifactVersion(o1.getArtifact().getVersion());
					DefaultArtifactVersion version1 = new DefaultArtifactVersion(o2.getArtifact().getVersion());
					return version1.compareTo(version);
				}
			});
			return value.get(0).getArtifact().getVersion();
		}

		private void fill(MavenArtifactNode mavenArtifactNode, DefaultMutableTreeNode node) {
			final MavenArtifactNode parent = mavenArtifactNode.getParent();
			if (parent == null) {
				return;
			}
			final DefaultMutableTreeNode parentDependencyNode = new DefaultMutableTreeNode(new MyTreeUserObject(parent));
			node.add(parentDependencyNode);
			parentDependencyNode.setParent(node);
			fill(parent, parentDependencyNode);
		}

	}

	private void expandAll(JTree tree, TreePath parent) {
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path);
			}
		}
		tree.expandPath(parent);
	}

	private JBList createJBList(DefaultListModel model) {
		JBList jbList = new JBList(model);
		jbList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				final Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				MyListNode listNode = (MyListNode) value;
				setText(listNode.toString());
				return comp;
			}
		});
		jbList.addListSelectionListener(new MyListSelectionListener());
		return jbList;
	}

	private void initializeModel() {
		final Object selectedValue = list.getSelectedValue();

		final List<MavenArtifactNode> dependencyTree = mavenProject.getDependencyTree();
		allArtifactsMap = createAllArtifactsMap(dependencyTree);
		updateListModel(allArtifactsMap);

		treeRoot.removeAllChildren();
		treeModel.reload();

		if (selectedValue != null) {
			list.setSelectedValue(selectedValue, true);
		}
	}

	private void updateListModel(Map<String, List<MavenArtifactNode>> allArtifactsMap) {
		listDataModel.clear();
		if (conflictsRadioButton.isSelected()) {
			for (Map.Entry<String, List<MavenArtifactNode>> s : allArtifactsMap.entrySet()) {
				final List<MavenArtifactNode> nodes = s.getValue();
				if (nodes.size() > 1 && hasConflicts(nodes)) {
					listDataModel.addElement(new MyListNode(s));
				}
				noConflictsLabel.setVisible(listDataModel.isEmpty());
			}
		} else {
			for (Map.Entry<String, List<MavenArtifactNode>> s : allArtifactsMap.entrySet()) {
				listDataModel.addElement(new MyListNode(s));
			}
			noConflictsLabel.setVisible(false);
		}
	}

	private boolean hasConflicts(List<MavenArtifactNode> nodes) {
		String version = null;
		for (MavenArtifactNode node : nodes) {
			if (version != null && !version.equals(node.getArtifact().getVersion())) {
				return true;
			}
			version = node.getArtifact().getVersion();
		}
		return false;
	}

	private Map<String, List<MavenArtifactNode>> createAllArtifactsMap(List<MavenArtifactNode> dependencyTree) {
		final Map<String, List<MavenArtifactNode>> map = new TreeMap<String, List<MavenArtifactNode>>();
		addAll(map, dependencyTree, 0);
		return map;
	}

	private void addAll(Map<String, List<MavenArtifactNode>> map, List<MavenArtifactNode> artifactNodes, int i) {
		if (map == null) {
			return;
		}
		if (i > 100) {
			final StringBuilder stringBuilder = new StringBuilder();
			for (MavenArtifactNode s : artifactNodes) {
				final String s1 = s.getArtifact().toString();
				stringBuilder.append(s1);
				stringBuilder.append(" ");
			}
			LOG.error("Recursion aborted, artifactNodes = [" + stringBuilder + "]");
			return;
		}
		for (MavenArtifactNode mavenArtifactNode : artifactNodes) {
			final MavenArtifact artifact = mavenArtifactNode.getArtifact();

			final String key = artifact.getGroupId() + ":" + artifact.getArtifactId();
			final List<MavenArtifactNode> mavenArtifactNodes = map.get(key);
			if (mavenArtifactNodes == null) {
				final ArrayList<MavenArtifactNode> value = new ArrayList<MavenArtifactNode>(1);
				value.add(mavenArtifactNode);
				map.put(key, value);
			} else {
				mavenArtifactNodes.add(mavenArtifactNode);
			}
			addAll(map, mavenArtifactNode.getDependencies(), i + 1);
		}
	}

	public JComponent getRootComponent() {
		return rootPanel;
	}

	public JComponent getPreferredFocusedComponent() {
		return rootPanel;
	}

	public void selectNotify() {
		initializeModel();
		splitPane.setDividerLocation(0.5);
	}

}
