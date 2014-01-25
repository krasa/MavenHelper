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
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.SimpleTextAttributes;
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
		tree.addMouseListener(new PopupHandler() {
			public void invokePopup(final Component comp, final int x, final int y) {
				final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
				final MyTreeNode myTreeNode = (MyTreeNode) selectedNode.getUserObject();
				final MavenArtifactNode mavenArtifactNode = myTreeNode.getMavenArtifactNode();
				if (myTreeNode.getMavenArtifactNode().getParent() == null) {
					return;
				}

				final ActionManager actionManager = ActionManager.getInstance();
				final ActionGroup actionGroup = new ActionGroup() {
					@NotNull
					@Override
					public AnAction[] getChildren(@Nullable AnActionEvent e) {
						return new AnAction[] { new ExcludeAction(project, mavenProject, mavenArtifactNode) {
							@Override
							public void dependencyExcluded() {
								TreeNode nodeForRemoval = selectedNode;
								while (nodeForRemoval.getParent() != null && nodeForRemoval.getParent() != treeRoot) {
									nodeForRemoval = nodeForRemoval.getParent();
								}
								treeModel.removeNodeFromParent((MutableTreeNode) nodeForRemoval);
							}
						} };
					}
				};
				actionManager.createActionPopupMenu("", actionGroup).getComponent().show(comp, x, y);
			}
		});
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
		tree.setCellRenderer(new MyRenderer());
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
	}

	private class MyListSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (listDataModel.isEmpty() || list.getSelectedValue() == null) {
				return;
			}
			final MyListNode myListNode = (MyListNode) list.getSelectedValue();
			final List<MavenArtifactNode> value = myListNode.value;

			treeRoot.removeAllChildren();
			String maxVersion = sortByVersion(value);
			for (MavenArtifactNode mavenArtifactNode : value) {
				SimpleTextAttributes attributes = SimpleTextAttributes.ERROR_ATTRIBUTES;
				if (maxVersion.equals(mavenArtifactNode.getArtifact().getVersion())) {
					attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
				}
				final DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(new MyTreeNode(mavenArtifactNode,
						attributes));
				addAllParents(mavenArtifactNode, newChild);
				treeRoot.add(newChild);
			}
			treeModel.nodeStructureChanged(treeRoot);
			expandAll(tree, new TreePath(treeRoot.getPath()));
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

		private void addAllParents(MavenArtifactNode mavenArtifactNode, DefaultMutableTreeNode newChild) {
			final MavenArtifactNode parent = mavenArtifactNode.getParent();
			if (parent == null) {
				return;
			}
			final DefaultMutableTreeNode parentDependencyNode = new DefaultMutableTreeNode(new MyTreeNode(parent));
			newChild.add(parentDependencyNode);
			parentDependencyNode.setParent(newChild);
			addAllParents(parent, parentDependencyNode);
		}

	}

	private class MyTreeNode {
		private MavenArtifactNode mavenArtifactNode;
		protected SimpleTextAttributes attributes;

		public MyTreeNode(MavenArtifactNode mavenArtifactNode) {
			this.mavenArtifactNode = mavenArtifactNode;
			attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
		}

		public MyTreeNode(MavenArtifactNode mavenArtifactNode, final SimpleTextAttributes regularAttributes) {
			this.mavenArtifactNode = mavenArtifactNode;
			attributes = regularAttributes;
		}

		public MavenArtifact getArtifact() {
			return mavenArtifactNode.getArtifact();
		}

		public MavenArtifactNode getMavenArtifactNode() {
			return mavenArtifactNode;
		}

		@Override
		public String toString() {
			return mavenArtifactNode.getArtifact().getArtifactId();
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

	private static class MyRenderer extends ColoredTreeCellRenderer {

		public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
				int row, boolean hasFocus) {
			Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
			if (!(userObject instanceof MyTreeNode))
				return;

			MyTreeNode myTreeNode = (MyTreeNode) userObject;

			final MavenArtifact artifact = myTreeNode.getArtifact();
			append(artifact.getDisplayStringSimple(), myTreeNode.attributes);
		}

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
	}

}
