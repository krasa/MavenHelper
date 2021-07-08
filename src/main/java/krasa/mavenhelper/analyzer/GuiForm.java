package krasa.mavenhelper.analyzer;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.CommonActionsManager;
import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.ex.ActionManagerEx;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import krasa.mavenhelper.ApplicationService;
import krasa.mavenhelper.Donate;
import krasa.mavenhelper.MyProjectService;
import krasa.mavenhelper.analyzer.action.LeftTreePopupHandler;
import krasa.mavenhelper.analyzer.action.ListKeyStrokeAdapter;
import krasa.mavenhelper.analyzer.action.ListPopupHandler;
import krasa.mavenhelper.analyzer.action.RightTreePopupHandler;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectChanges;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.server.NativeMavenProjectHolder;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.lang.reflect.Method;
import java.util.List;
import java.util.*;

/**
 * @author Vojtech Krasa
 */
public class GuiForm implements Disposable {
	private static final Logger LOG = Logger.getInstance("#krasa.mavenrun.analyzer.GuiForm");

	public static final String WARNING = "Your settings indicates, that conflicts will not be visible, see IDEA-133331\n"
		+ "If your project is Maven2 compatible, you could try one of the following:\n"
			+ "-use IJ 2016.1+ and configure it to use external Maven 3.1.1+ (File | Settings | Build, Execution, Deployment | Build Tools | Maven | Maven home directory)\n"
			+ "-press Apply Fix button to alter Maven VM options for importer (might cause trouble for IJ 2016.1+)\n"
			+ "-turn off File | Settings | Build, Execution, Deployment | Build Tools | Maven | Importing | Use Maven3 to import project setting\n";
	protected static final Comparator<MavenArtifactNode> BY_ARTIFACT_ID = new Comparator<MavenArtifactNode>() {
		@Override
		public int compare(MavenArtifactNode o1, MavenArtifactNode o2) {
			return o1.getArtifact().getArtifactId().compareToIgnoreCase(o2.getArtifact().getArtifactId());
		}
	};
	private static final String LAST_RADIO_BUTTON = "MavenHelper.lastRadioButton";
	public static final SimpleTextAttributes SIZE_ATTRIBUTES = SimpleTextAttributes.GRAY_ATTRIBUTES;

	private final Project project;
	private final VirtualFile file;
	private MavenProject mavenProject;
	protected JBList leftPanelList;
	private MyHighlightingTree rightTree;
	private JPanel rootPanel;

	private JRadioButton conflictsRadioButton;
	private JRadioButton allDependenciesAsListRadioButton;
	private JRadioButton allDependenciesAsTreeRadioButton;

	private JLabel noConflictsLabel;
	private JScrollPane noConflictsWarningLabelScrollPane;
	private JTextPane noConflictsWarningLabel;
	private JButton refreshButton;
	private JSplitPane splitPane;
	private SearchTextField searchField;
	private JPanel leftPanelWrapper;
	private MyHighlightingTree leftTree;
	private JCheckBox showGroupId;
	private JCheckBox showSize;
	private JPanel buttonsPanel;
	private JButton donate;
	private JButton reimport;
	protected JEditorPane intellijBugLabel;
	protected JEditorPane falsePositive;
	private JCheckBox filter;
	protected MyDefaultListModel listDataModel;
	protected Map<String, List<MavenArtifactNode>> allArtifactsMap;
	protected final DefaultTreeModel rightTreeModel;
	protected final DefaultTreeModel leftTreeModel;
	protected final MyDefaultMutableTreeNode rightTreeRoot;
	protected final MyDefaultMutableTreeNode leftTreeRoot;
	protected ListSpeedSearch myListSpeedSearch;
	protected List<MavenArtifactNode> dependencyTree;
	protected CardLayout leftPanelLayout;

	private boolean notificationShown;

	private final SimpleTextAttributes errorBoldAttributes;

	private MyProjectService.MyEventListener myEventListener;
	private MavenProjectsManager mavenProjectsManager;
	private MyProjectService myProjectService;

	private boolean manualReimport;
	private RightTreePopupHandler rightTreePopupHandler;
	private LeftTreePopupHandler leftTreePopupHandler;
	private ListPopupHandler leftPanelListPopupHandler;

	public GuiForm(final Project project, VirtualFile file, final MavenProject mavenProject) {
		this.project = project;
		this.file = file;
		mavenProjectsManager = MavenProjectsManager.getInstance(project);
		myProjectService = MyProjectService.getInstance(project);
		this.mavenProject = mavenProject;

		intellijBugLabel.setText("<html>\n" +
				"  <head>\n" +
			"\n" +
			"  </head>\n" +
			"  <body>\n" +
			"      1) An artifact is in conflict, its version is probably wrongly resolved due to a <a href=\"https://youtrack.jetbrains.com/issue/IDEA-215596\">bug in IntelliJ</a>." +
			"  </body>\n" +
			"</html>\n");
		intellijBugLabel.setBackground(rootPanel.getBackground());
		intellijBugLabel.setForeground(ApplicationService.getInstance().getState().getErrorAttributes().getFgColor());
		intellijBugLabel.setVisible(false);
		intellijBugLabel.addHyperlinkListener(e -> {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				BrowserUtil.browse(e.getURL());
			}
		});


		falsePositive.setText("<html>\n" +
			"  <head>\n" +
			"\n" +
			"  </head>\n" +
			"  <body>\n" +
			"      2) Probably a false positive, this should not happen, please report it at <a href=\"https://github.com/krasa/MavenHelper/issues/\">GitHub</a>." +
			"  </body>\n" +
			"</html>\n");
		falsePositive.setBackground(rootPanel.getBackground());
		falsePositive.setForeground(ApplicationService.getInstance().getState().getErrorAttributes().getFgColor());
		falsePositive.setVisible(false);
		falsePositive.addHyperlinkListener(e -> {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				BrowserUtil.browse(e.getURL());
			}
		});
		
		final ActionListener radioButtonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateLeftPanel();

				String value = null;
				if (allDependenciesAsListRadioButton.isSelected()) {
					value = "list";
				} else if (allDependenciesAsTreeRadioButton.isSelected()) {
					value = "tree";
				}
				PropertiesComponent.getInstance().setValue(LAST_RADIO_BUTTON, value);
			}
		};
		conflictsRadioButton.addActionListener(radioButtonListener);
		allDependenciesAsListRadioButton.addActionListener(radioButtonListener);
		allDependenciesAsTreeRadioButton.addActionListener(radioButtonListener);

		refreshButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshButton.setToolTipText(null);
				refreshButton.setIcon(null);

				initializeModel();
				rootPanel.requestFocus();
			}
		});

		myListSpeedSearch = new ListSpeedSearch(leftPanelList);
		searchField.addDocumentListener(new DocumentAdapter() {
			@Override
			protected void textChanged(DocumentEvent documentEvent) {
				updateLeftPanel();
			}
		});
		try {
			Method searchField = this.searchField.getClass().getMethod("getTextEditor");
			JTextField invoke = (JTextField) searchField.invoke(this.searchField);
			invoke.addFocusListener(new FocusAdapter() {
				@Override
				public void focusLost(FocusEvent e) {
					if (GuiForm.this.searchField.getText() != null) {
						GuiForm.this.searchField.addCurrentTextToHistory();
					}
				}
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		noConflictsWarningLabel.setBackground(null);
		noConflictsWarningLabel.setText(WARNING);
		noConflictsWarningLabel.setForeground(ApplicationService.getInstance().getState().getErrorAttributes().getFgColor());

		leftPanelLayout = (CardLayout) leftPanelWrapper.getLayout();

		rightTreeRoot = new MyDefaultMutableTreeNode();
		rightTreeModel = new DefaultTreeModel(rightTreeRoot);
		rightTree.setModel(rightTreeModel);
		rightTree.setRootVisible(false);
		rightTree.setShowsRootHandles(true);
		rightTree.expandPath(new TreePath(rightTreeRoot.getPath()));
		rightTree.setCellRenderer(new TreeRenderer(showGroupId, showSize, this));
		rightTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		rightTreePopupHandler = new RightTreePopupHandler(project, mavenProject, rightTree, this);
		rightTree.addMouseListener(rightTreePopupHandler);
		rightTree.setMavenProject(mavenProject);

		leftTree.addTreeSelectionListener(new LeftTreeSelectionListener());
		leftTreeRoot = new MyDefaultMutableTreeNode();
		leftTreeModel = new DefaultTreeModel(leftTreeRoot);
		leftTree.setModel(leftTreeModel);
		leftTree.setRootVisible(false);
		leftTree.setShowsRootHandles(true);
		leftTree.expandPath(new TreePath(leftTreeRoot.getPath()));
		leftTree.setCellRenderer(new TreeRenderer(showGroupId, showSize, this));
		leftTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		leftTreePopupHandler = new LeftTreePopupHandler(project, mavenProject, leftTree);
		leftTree.addMouseListener(leftTreePopupHandler);
		leftTree.setMavenProject(mavenProject);


		leftPanelListPopupHandler = new ListPopupHandler(project, mavenProject, leftPanelList, this);
		leftPanelList.addMouseListener(leftPanelListPopupHandler);
		leftPanelList.addKeyListener(new ListKeyStrokeAdapter(this));

		showGroupId.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RestoreSelection restoreSelection = new RestoreSelection(leftPanelList, leftTree);
				updateLeftPanel();
				restoreSelection.restore();
			}
		});

		showSize.addActionListener((event) -> {
			RestoreSelection restoreSelection = new RestoreSelection(leftPanelList, leftTree);
			updateLeftPanel();
			restoreSelection.restore();
		});

		filter.addActionListener((event) -> {
			RestoreSelection restoreSelection = new RestoreSelection(leftPanelList, leftTree);
			updateLeftPanel();
			restoreSelection.restore();
		});

		final DefaultTreeExpander treeExpander = new DefaultTreeExpander(leftTree);
		DefaultActionGroup actionGroup = new DefaultActionGroup();
		actionGroup.add(CommonActionsManager.getInstance().createExpandAllAction(treeExpander, leftTree));
		actionGroup.add(CommonActionsManager.getInstance().createCollapseAllAction(treeExpander, leftTree));
		ActionToolbar actionToolbar = ActionManagerEx.getInstance().createActionToolbar("krasa.MavenHelper.buttons", actionGroup, true);
		buttonsPanel.add(actionToolbar.getComponent(), "1");
		errorBoldAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, ApplicationService.getInstance().getState().getErrorAttributes().getFgColor());

		String lastRadioButton = PropertiesComponent.getInstance().getValue(LAST_RADIO_BUTTON);
		if ("tree".equals(lastRadioButton)) {
			allDependenciesAsTreeRadioButton.setSelected(true);
		} else if ("list".equals(lastRadioButton)) {
			allDependenciesAsListRadioButton.setSelected(true);
		} else {
			conflictsRadioButton.setSelected(true);
		}
		Donate.init(rootPanel, donate);


		myEventListener = new MyProjectService.MyEventListener() {

			@Override
			public void projectResolved(@NotNull Pair<MavenProject, MavenProjectChanges> projectWithChanges, @Nullable NativeMavenProjectHolder nativeMavenProject) {
				if (projectWithChanges.first == mavenProject) {
					if (refreshButton.isShowing() && manualReimport) {
						manualReimport = false;
						refreshButton.doClick();
					} else {
						refreshButton.setIcon(AllIcons.General.BalloonWarning);
						refreshButton.setToolTipText("Maven model changed, refresh UI");
					}
				}
			}
		};
		myProjectService.register(myEventListener);
		reimport.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				manualReimport = true;
				mavenProjectsManager.forceUpdateAllProjectsOrFindAllAvailablePomFiles();
			}
		});
	}

	private void createUIComponents() {
		listDataModel = new MyDefaultListModel();
		leftPanelList = new JBList((ListModel) listDataModel);
		leftPanelList.addListSelectionListener(new MyListSelectionListener());
		// no generics in IJ12
		leftPanelList.setCellRenderer(new ColoredListCellRenderer() {
			@Override
			protected void customizeCellRenderer(JList jList, Object o, int i, boolean b, boolean b2) {
				MyListNode value = (MyListNode) o;
				String rightVersion = value.getRightVersion();
				final String[] split = value.artifactKey.split(":");
				boolean conflict = value.isConflict();

				SimpleTextAttributes attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
				SimpleTextAttributes boldAttributes = SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES;
				if (conflict && allDependenciesAsListRadioButton.isSelected()) {
					attributes = ApplicationService.getInstance().getState().getErrorAttributes();
					boldAttributes = errorBoldAttributes;
				}
				if (showSize.isSelected()) {
					Utils.appendSize(this, value.getSize(), value.getTotalSize());
				}
				if (showGroupId.isSelected()) {
					append(split[0] + " : ", attributes);
				}
				append(split[1], boldAttributes);
				if (rightVersion != null) {
					append(" : " + rightVersion, attributes);
				}
			}

		});
		rightTree = new MyHighlightingTree(project);
		leftTree = new MyHighlightingTree(project);
	}

	@Override
	public void dispose() {
		myProjectService.unregister(myEventListener);
	}

	public void switchToLeftTree(MavenArtifactNode myArtifact) {
		allDependenciesAsTreeRadioButton.setSelected(true);
		searchField.setText(myArtifact.getArtifact().getArtifactId());
		updateLeftPanel();

		TreeUtils.selectRows(leftTree, leftTreeRoot, myArtifact);
		leftTree.requestFocus();
	}

	public void switchToLeftTree() {
		MyListNode selectedValue = (MyListNode) leftPanelList.getSelectedValue();
		MavenArtifactNode rightArtifact = selectedValue.getRightArtifact();
		if (rightArtifact != null) {
			switchToLeftTree(rightArtifact);
		}
	}

	private class LeftTreeSelectionListener implements TreeSelectionListener {
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			TreePath selectionPath = e.getPath();
			if (selectionPath != null) {
				DefaultMutableTreeNode lastPathComponent = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
				MyTreeUserObject userObject = (MyTreeUserObject) lastPathComponent.getUserObject();

				final String key = getArtifactKey(userObject.getArtifact());
				List<MavenArtifactNode> mavenArtifactNodes = allArtifactsMap.get(key);
				if (mavenArtifactNodes != null) {// can be null while refreshing
					fillRightTree(mavenArtifactNodes);
				}
			}
		}
	}

	private class MyListSelectionListener implements ListSelectionListener {
		@Override
		public void valueChanged(ListSelectionEvent e) {
			if (listDataModel.isEmpty() || leftPanelList.getSelectedValue() == null) {
				return;
			}

			final MyListNode myListNode = (MyListNode) leftPanelList.getSelectedValue();
			fillRightTree(myListNode.getArtifacts());
		}
	}

	private void fillRightTree(List<MavenArtifactNode> mavenArtifactNodes) {
		rightTreeRoot.removeAllChildren();
		for (MavenArtifactNode mavenArtifactNode : mavenArtifactNodes) {
			MyTreeUserObject userObject = new MyTreeUserObject(mavenArtifactNode);
			userObject.showOnlyVersion = true;
			final DefaultMutableTreeNode newNode = new MyDefaultMutableTreeNode(userObject);
			fillRightTree(mavenArtifactNode, newNode);
			rightTreeRoot.add(newNode);
		}
		rightTreeModel.nodeStructureChanged(rightTreeRoot);
		TreeUtils.expandAll(rightTree);
	}

	private void fillRightTree(MavenArtifactNode mavenArtifactNode, DefaultMutableTreeNode node) {
		final MavenArtifactNode parent = mavenArtifactNode.getParent();
		if (parent == null) {
			return;
		}
		final DefaultMutableTreeNode parentDependencyNode = new MyDefaultMutableTreeNode(new MyTreeUserObject(parent));
		node.add(parentDependencyNode);
		parentDependencyNode.setParent(node);
		fillRightTree(parent, parentDependencyNode);
	}

	private void initializeModel() {
		intellijBugLabel.setVisible(false);
		falsePositive.setVisible(false);
		rightTreePopupHandler.hidePopup();
		leftTreePopupHandler.hidePopup();
		
		final Object selectedValue = leftPanelList.getSelectedValue();

		dependencyTree = mavenProject.getDependencyTree();
		allArtifactsMap = createAllArtifactsMap(dependencyTree);
		updateLeftPanel();

		rightTreeRoot.removeAllChildren();
		rightTreeModel.reload();
		leftPanelWrapper.revalidate();

		if (selectedValue != null) {
			leftPanelList.setSelectedValue(selectedValue, true);
		}
	}

	private void updateLeftPanel() {
		intellijBugLabel.setVisible(false);
		falsePositive.setVisible(false);
		listDataModel.clear();
		leftTreeRoot.removeAllChildren();

		final String searchFieldText = searchField.getText();
		boolean conflictsWarning = false;

		boolean showNoConflictsLabel = false;
		if (conflictsRadioButton.isSelected()) {
			for (Map.Entry<String, List<MavenArtifactNode>> s : allArtifactsMap.entrySet()) {
				final List<MavenArtifactNode> nodes = s.getValue();
				if (nodes.size() > 1 && hasConflicts(nodes)) {
					if (contains(searchFieldText, s.getKey())) {
						listDataModel.add(new MyListNode(s));
					}
				}
			}
			sortList();
			showNoConflictsLabel = listDataModel.isEmpty();
			leftPanelLayout.show(leftPanelWrapper, "list");
		} else if (allDependenciesAsListRadioButton.isSelected()) {  //list
			for (Map.Entry<String, List<MavenArtifactNode>> s : allArtifactsMap.entrySet()) {
				if (contains(searchFieldText, s.getKey())) {
					listDataModel.add(new MyListNode(s));
				}
			}
			sortList();
			showNoConflictsLabel = false;
			leftPanelLayout.show(leftPanelWrapper, "list");
		} else { // tree
			fillLeftTree(leftTreeRoot, dependencyTree, searchFieldText);
			sortTree();
			leftTreeModel.nodeStructureChanged(leftTreeRoot);
			TreeUtils.expandAll(leftTree);

			showNoConflictsLabel = false;
			leftPanelLayout.show(leftPanelWrapper, "allAsTree");
		}

		if (conflictsWarning) {
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					noConflictsWarningLabelScrollPane.getVerticalScrollBar().setValue(0);
				}
			});
			leftPanelLayout.show(leftPanelWrapper, "noConflictsWarningLabel");
		}
		buttonsPanel.setVisible(allDependenciesAsTreeRadioButton.isSelected());
		filter.setVisible(allDependenciesAsTreeRadioButton.isSelected());
		noConflictsWarningLabelScrollPane.setVisible(conflictsWarning);
		noConflictsLabel.setVisible(showNoConflictsLabel);
	}

	private void sortTree() {
		if (showSize.isSelected()) {
			leftTreeRoot.sortBySize(MyDefaultMutableTreeNode.DEEP_SIZE);
		} else if (showGroupId.isSelected()) {
			leftTreeRoot.sortBySize(MyDefaultMutableTreeNode.GROUP_ID);
		} else {
			leftTreeRoot.sortBySize(MyDefaultMutableTreeNode.ARTIFACT_ID);
		}
	}

	private void sortList() {
		if (showSize.isSelected()) {
			listDataModel.sort(MyDefaultListModel.DEEP_SIZE);
		} else if (showGroupId.isSelected()) {
			listDataModel.sort(MyDefaultListModel.GROUP_ID);
		} else {
			listDataModel.sort(MyDefaultListModel.ARTIFACT_ID);
		}
	}

	private boolean fillLeftTree(DefaultMutableTreeNode parent, List<MavenArtifactNode> dependencyTree, String searchFieldText) {
		boolean search = StringUtils.isNotBlank(searchFieldText);
		boolean hasAddedNodes = false;

		for (MavenArtifactNode mavenArtifactNode : dependencyTree) {
			boolean directMatch = false;
			MyTreeUserObject treeUserObject = new MyTreeUserObject(mavenArtifactNode);
			if (search && contains(searchFieldText, mavenArtifactNode)) {
				directMatch = true;
				treeUserObject.highlight = true;
			}
			final DefaultMutableTreeNode newNode = new MyDefaultMutableTreeNode(treeUserObject);
			boolean childAdded = fillLeftTree(newNode, mavenArtifactNode.getDependencies(), searchFieldText);

			if (!search || !filter.isSelected() || directMatch || childAdded) {
				parent.add(newNode);
				hasAddedNodes = true;
			}
		}
		return hasAddedNodes;
	}

	private boolean contains(String searchFieldText, MavenArtifactNode mavenArtifactNode) {
		MavenArtifact artifact = mavenArtifactNode.getArtifact();
		return contains(searchFieldText, getArtifactKey(artifact));
	}

	private boolean contains(String searchFieldText, String artifactKey) {
		return StringUtils.isBlank(searchFieldText) || StringUtil.containsIgnoreCase(artifactKey, searchFieldText);
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

			final String key = getArtifactKey(artifact);
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

	@NotNull
	private String getArtifactKey(MavenArtifact artifact) {
		return artifact.getGroupId() + " : " + artifact.getArtifactId();
	}

	public JComponent getRootComponent() {
		return rootPanel;
	}

	public JComponent getPreferredFocusedComponent() {
		return rootPanel;
	}

	public void selectNotify() {
		if (dependencyTree == null) {
			initializeModel();
			splitPane.setDividerLocation(0.5);
		}
	}

}
