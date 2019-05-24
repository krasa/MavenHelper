package krasa.mavenhelper.gui;

import com.intellij.application.options.colors.ColorAndFontOptions;
import com.intellij.ide.HelpTooltip;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.util.ui.StatusText;
import krasa.mavenhelper.action.Utils;
import krasa.mavenhelper.model.Alias;
import krasa.mavenhelper.model.ApplicationSettings;
import krasa.mavenhelper.model.Goal;
import krasa.mavenhelper.model.Goals;
import org.apache.commons.lang.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.MavenArtifactUtil;
import org.jetbrains.idea.maven.utils.MavenPluginInfo;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GoalEditor extends DialogWrapper {

	private static final Logger LOG = Logger.getInstance(GoalEditor.class);

	public static final String SAVE = "MavenRunHelper.GoalEditor.save";
	public static final String DIMENSION = "MavenRunHelper.GoalEditor";

	private JPanel optionsPanel;
	private JPanel cmdPanel;
	private JPanel mainPanel;
	private JPanel goalsPanel;
	private JPanel aliasesPanel;
	private JPanel optionsPanel2;
	private JCheckBox saveGoalCheckBox;
	protected JLabel commandLineLabel;
	private EditorImpl myEditor;

	public GoalEditor(String title, String initialValue, ApplicationSettings applicationSettings, boolean persist, Project project, DataContext dataContext) {
		super(true);
		setTitle(title);
		saveGoalCheckBox.setSelected(PropertiesComponent.getInstance().getBoolean(SAVE, true));
		saveGoalCheckBox.setVisible(persist);

		try {
//		optionsPanel.add(new JBLabel("Append:"));
//		optionsPanel.setLayout(new WrapLayout());
			optionsPanel.add(getLinkLabel("-DskipTests", null));
			optionsPanel.add(getLinkLabel(new ListItem("--update-snapshots", "Forces a check for updated releases and snapshots on remote repositories")));
			optionsPanel.add(getLinkLabel(new ListItem("--offline", "Work offline")));
			optionsPanel.add(getLinkLabel(new ListItem("--debug", "Produce execution debug output")));
			optionsPanel.add(getLinkLabel(new ListItem("--non-recursive", "Do not recurse into sub-projects")));

//		optionsPanel2.setLayout(new WrapLayout());
			optionsPanel2.add(listPopup("Option...", getOptions(false), false));
			optionsPanel2.add(listPopup("Short Option...", getOptions(true), true));

			optionsPanel2.add(listPopup("Alias...", toListItems(applicationSettings.getAliases().getAliases()), false));

//		goalsPanel.add(new JBLabel("Goals:"));
//		goalsPanel.setLayout(new WrapLayout());

			goalsPanel.add(listPopup("Lifecycle Goal...", getGoals(), false));
			goalsPanel.add(listPopup("Existing Goal...", getExistingGoals(applicationSettings), false));
			goalsPanel.add(listPopup("Util...", getHelpfulGoals(), false));


			if (dataContext != null) {
				MavenProject mavenProject = MavenActionUtil.getMavenProject(dataContext);
				if (mavenProject != null) {
					List<ListItem> listItems = new ArrayList<>();
					for (MavenPlugin mavenPlugin : mavenProject.getDeclaredPlugins()) {
						MavenPluginInfo pluginInfo = MavenArtifactUtil.readPluginInfo(
							MavenProjectsManager.getInstance(project).getLocalRepository(), mavenPlugin.getMavenId());
						if (pluginInfo != null) {
							boolean first = true;
							for (MavenPluginInfo.Mojo mojo : pluginInfo.getMojos()) {
								ListItem listItem = new ListItem(mojo.getDisplayName());
								if (first) {
									listItem.separatorAbove = new ListItem(mavenPlugin.getArtifactId());
								}
								listItems.add(listItem);
								first = false;
							}
						}
					}
					goalsPanel.add(listPopup("Plugin Goal...", listItems.toArray(new ListItem[0]), false));
				}


			}

		} catch (Throwable e) {
			LOG.error(Objects.toString(e), e);
		}

//		aliasesPanel.add(new JBLabel("Aliases:"));

		init();

		myEditor.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void documentChanged(@NotNull DocumentEvent event) {
				updateControls();
			}
		});
		append(initialValue);

		updateControls();
//		IdeFocusManager.findInstanceByComponent(mainPanel).requestFocus(myEditor.getComponent(), true);
	}


	private ListItem[] getExistingGoals(ApplicationSettings applicationSettings) {
		Goals goals = applicationSettings.getGoals();
		Goals pluginAwareGoals = applicationSettings.getPluginAwareGoals();

		List<ListItem> arrayList = new ArrayList<>(goals.size() + pluginAwareGoals.size());

		for (Goal goal : goals.getGoals()) {
			arrayList.add(new ListItem(goal.getCommandLine()));
		}

		List<Goal> pluginAwareGoalsGoals = pluginAwareGoals.getGoals();
		for (int i = 0; i < pluginAwareGoalsGoals.size(); i++) {
			Goal goal = pluginAwareGoalsGoals.get(i);
			ListItem e = new ListItem(goal.getCommandLine());
			if (i == 0) {
				e.separatorAbove = new ListItem("Plugin aware:");
			}
			arrayList.add(e);
		}

		return arrayList.toArray(new ListItem[0]);
	}

	private ListItem[] getHelpfulGoals() {
		return new ListItem[]{
			new ListItem("dependency:tree", "Display project dependencies"),
			new ListItem("dependency:analyze", "Analyze project dependencies"),

			new ListItem("help:effective-settings", "Display effective Maven settings").withSeparatorAbove(),
			new ListItem("help:effective-pom", "Display effective POM"),
			new ListItem("help:active-profiles", "Display all profiles (from settings.xml and POMs hierarchy)"),

			new ListItem("versions:display-dependency-updates", "Check dependencies for newer versions").withSeparatorAbove(),
			new ListItem("versions:display-plugin-updates", "Check plugins for newer versions"),
			new ListItem("versions:display-property-updates", "Check for newer versions defined as properties"),

		};
	}

	private ListItem[] getOptions(boolean shortcut) {
		return new ListItem[]{


			new ListItem(shortcut, "-am", "--also-make", "If project list is specified, also build projects required by the list"),
			new ListItem(shortcut, "-amd", "--also-make-dependents", "If project list is specified, also build projects that depend on projects on the list"),
			new ListItem(shortcut, "-B", "--batch-mode", "Run in non-interactive (batch) mode"),
			new ListItem(shortcut, "-C", "--strict-checksums", "Fail the build if checksums don't match"),
			new ListItem(shortcut, "-c", "--lax-checksums", "Warn if checksums don't match"),
			new ListItem(shortcut, "-cpu", "--check-plugin-updates", "Ineffective, only kept for backward compatibility"),
			new ListItem(shortcut, "-D", "--define <arg>", "Define a system property"),
			new ListItem(shortcut, "-e", "--errors", "Produce execution error messages"),
			new ListItem(shortcut, "-emp", "--encrypt-master-password <arg>", "Encrypt master security password"),
			new ListItem(shortcut, "-ep", "--encrypt-password <arg>", "Encrypt server password"),
			new ListItem(shortcut, "-f", "--file <arg>", "Force the use of an alternate POM file (or directory with pom.xml)."),
			new ListItem(shortcut, "-fae", "--fail-at-end", "Only fail the build afterwards; allow all non-impacted builds to continue"),
			new ListItem(shortcut, "-ff", "--fail-fast", "Stop at first failure in reactorized builds"),
			new ListItem(shortcut, "-fn", "--fail-never", "NEVER fail the build, regardless of project result"),
			new ListItem(shortcut, "-gs", "--global-settings <arg>", "Alternate path for the global settings file"),
			new ListItem(shortcut, "-h", "--help", "Display help information"),
			new ListItem(shortcut, "-l", "--log-file <arg>", "Log file to where all build output will go."),
			new ListItem(shortcut, "-llr", "--legacy-local-repository", "Use Maven 2 Legacy Local Repository behaviour, ie no use of _maven.repositories. Can also be activated by using -Dmaven.legacyLocalRepo=true"),
			new ListItem(shortcut, "-N", "--non-recursive", "Do not recurse into sub-projects"),
			new ListItem(shortcut, "-npr", "--no-plugin-registry", "Ineffective, only kept for backward compatibility"),
			new ListItem(shortcut, "-npu", "--no-plugin-updates", "Ineffective, only kept for backward compatibility"),
			new ListItem(shortcut, "-nsu", "--no-snapshot-updates", "Suppress SNAPSHOT updates"),
			new ListItem(shortcut, "-o", "--offline", "Work offline"),
			new ListItem(shortcut, "-P", "--activate-profiles <arg>", "Comma-delimited list of profiles to activate"),
			new ListItem(shortcut, "-pl", "--projects <arg>", "Comma-delimited list of specified reactor projects to build instead of all projects. A project can be specified by [groupId]:artifactId or by its relative path."),
			new ListItem(shortcut, "-q", "--quiet", "Quiet output - only show errors"),
			new ListItem(shortcut, "-rf", "--resume-from <arg>", "Resume reactor from specified project"),
			new ListItem(shortcut, "-s", "--settings <arg>", "Alternate path for the user settings file"),
			new ListItem(shortcut, "-T", "--threads <arg>", "Thread count, for instance 2.0C where C is core multiplied"),
			new ListItem(shortcut, "-t", "--toolchains <arg>", "Alternate path for the user toolchains file"),
			new ListItem(shortcut, "-U", "--update-snapshots", "Forces a check for updated releases and snapshots on remote repositories"),
			new ListItem(shortcut, "-up", "--update-plugins", "Ineffective, only kept for backward compatibility"),
			new ListItem(shortcut, "-V", "--show-version", "Display version information WITHOUT stopping build"),
			new ListItem(shortcut, "-v", "--version", "Display version information"),
			new ListItem(shortcut, "-X", "--debug", "Produce execution debug output"),

		};
	}

	private ListItem[] getGoals() {
		return new ListItem[]{
			new ListItem("Clean Lifecycle").asSeparatorBefore(
				new ListItem("pre-clean", "execute processes needed prior to the actual project cleaning")),
			new ListItem("clean", "remove all files generated by the previous build"),
			new ListItem("post-clean", "execute processes needed to finalize the project cleaning"),

			new ListItem("Default Lifecycle").asSeparatorBefore(
				new ListItem("validate", "validate the project is correct and all necessary information is available.")),
			new ListItem("initialize", "initialize build state, e.g. set properties or create directories."),
			new ListItem("generate-sources", "generate any source code for inclusion in compilation."),
			new ListItem("process-sources", "process the source code, for example to filter any values."),
			new ListItem("generate-resources", "generate resources for inclusion in the package."),
			new ListItem("process-resources", "copy and process the resources into the destination directory, ready for packaging."),
			new ListItem("compile", "compile the source code of the project."),
			new ListItem("process-classes", "post-process the generated files from compilation, for example to do bytecode enhancement on Java classes."),
			new ListItem("generate-test-sources", "generate any test source code for inclusion in compilation."),
			new ListItem("process-test-sources", "process the test source code, for example to filter any values."),
			new ListItem("generate-test-resources", "create resources for testing."),
			new ListItem("process-test-resources", "copy and process the resources into the test destination directory."),
			new ListItem("test-compile", "compile the test source code into the test destination directory"),
			new ListItem("process-test-classes", "post-process the generated files from test compilation, for example to do bytecode enhancement on Java classes. For Maven 2.0.5 and above."),
			new ListItem("test", "run tests using a suitable unit testing framework. These tests should not require the code be packaged or deployed."),
			new ListItem("prepare-package", "perform any operations necessary to prepare a package before the actual packaging. This often results in an unpacked, processed version of the package. (Maven 2.1 and above)"),
			new ListItem("package", "take the compiled code and package it in its distributable format, such as a JAR."),
			new ListItem("pre-integration-test", "perform actions required before integration tests are executed. This may involve things such as setting up the required environment."),
			new ListItem("integration-test", "process and deploy the package if necessary into an environment where integration tests can be run."),
			new ListItem("post-integration-test", "perform actions required after integration tests have been executed. This may including cleaning up the environment."),
			new ListItem("verify", "run any checks to verify the package is valid and meets quality criteria."),
			new ListItem("install", "install the package into the local repository, for use as a dependency in other projects locally."),
			new ListItem("deploy", "done in an integration or release environment, copies the final package to the remote repository for sharing with other developers and projects."),

			new ListItem("Site Lifecycle").asSeparatorBefore(
				new ListItem("pre-site", "execute processes needed prior to the actual project site generation")),
			new ListItem("site", "generate the project's site documentation"),
			new ListItem("post-site", "execute processes needed to finalize the site generation, and to prepare for site deployment"),
			new ListItem("site-deploy", "deploy the generated site documentation to the specified web server"),
		};
	}

	private ListItem[] toListItems(Goals goals) {
		ListItem[] items = new ListItem[goals.size()];
		List<Goal> goalsGoals = goals.getGoals();
		for (int i = 0; i < goalsGoals.size(); i++) {
			Goal goal = goalsGoals.get(i);
			items[i] = new ListItem(goal.getCommandLine());
		}
		return items;
	}

	private ListItem[] toListItems(String[] strings) {
		ListItem[] items = new ListItem[strings.length];
		for (int i = 0; i < strings.length; i++) {
			String prof = strings[i];
			items[i] = new ListItem(prof);
		}
		return items;
	}

	private ListItem[] toListItems(List<Alias> aliases) {
		ListItem[] items = new ListItem[aliases.size()];
		for (int i = 0; i < aliases.size(); i++) {
			Alias alias = aliases.get(i);
			items[i] = new ListItem(alias.getFrom(), alias.getTo());
		}
		return items;
	}

	private Component getLinkLabel(ListItem listItem) {
		return getLinkLabel(listItem.getPresentableText(), listItem.getDescription());

	}

	@NotNull
	private JComponent getLinkLabel(final String text, String description) {
		JButton jButton = new JButton(text);
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				append(text);

			}
		});

		if (description != null) {
			new HelpTooltip().setDescription(description).installOn(jButton);
		}

		return jButton;

//		LinkLabel linkLabel = new LinkLabel(text, null, new LinkListener() {
//			@Override
//			public void linkSelected(LinkLabel linkLabel, Object o) {
//				append(text);
//			}
//		});
//		if (description != null) {
//			new HelpTooltip().setDescription(description).installOn(linkLabel);
//		}
//		return linkLabel;
	}


	@NotNull
	private JComponent listPopup(String text, ListItem[] goalsAsStrings, boolean shortcut) {
//		return new LinkLabel<>(text, null, new LinkListener<String>() {
//			@Override
//			public void linkSelected(LinkLabel linkLabel, String o) {
//				ListPopupImpl popup = newListPopup(goalsAsStrings, shortcut);
//				popup.showUnderneathOf(linkLabel);
//			}
//		});

		JButton jButton = new JButton(text);
		jButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ListPopup popup = newListPopup(goalsAsStrings, shortcut);
				popup.showUnderneathOf(jButton);
			}
		});
		return jButton;
	}


	private void append(String txt) {
		ApplicationManager.getApplication().runWriteAction(() -> {
			String text = getCmd();
			if (text.length() > 0 && !text.endsWith(" ") && !text.endsWith("=")) {
				text += " ";
			}

			text += txt;

			myEditor.getDocument().setText(text);
			myEditor.getCaretModel().moveToOffset(myEditor.getDocument().getTextLength());
			cmdPanel.validate();
			cmdPanel.repaint();
		});
	}


	protected void updateControls() {
		getOKAction().setEnabled(!getCmd().isEmpty());
	}

	@Override
	public JComponent getPreferredFocusedComponent() {
		return myEditor.getContentComponent();
	}

	@Override
	protected String getHelpId() {
		return null;
	}

	@Override
	protected void doOKAction() {
		if (getCmd().isEmpty()) return;
		super.doOKAction();
	}

	public String getCmd() {
		return myEditor.getDocument().getText();
	}

	@Nullable
	protected String getDimensionServiceKey() {
//		return null;
		return DIMENSION;
	}

	@Override
	protected JComponent createNorthPanel() {
		return null;
	}

	@Override
	protected JComponent createCenterPanel() {
		return mainPanel;
	}

	private void createUIComponents() {
		myEditor = (EditorImpl) createEditor();
		cmdPanel = (JPanel) myEditor.getComponent();
		cmdPanel.setPreferredSize(new Dimension(800, 50));
	}

	@NotNull
	private static Editor createEditor() {
		EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
		ColorAndFontOptions options = new ColorAndFontOptions();
		options.reset();
		options.selectScheme(scheme.getName());
		EditorFactory editorFactory = EditorFactory.getInstance();
		Document editorDocument = editorFactory.createDocument("");


		EditorEx editor = (EditorEx) (true ? editorFactory.createEditor(editorDocument) : editorFactory.createViewer(editorDocument));
		editor.setColorsScheme(scheme);
		EditorSettings settings = editor.getSettings();
		settings.setLineNumbersShown(false);
		settings.setUseSoftWraps(true);
		settings.setWhitespacesShown(false);
		settings.setLineMarkerAreaShown(false);
		settings.setIndentGuidesShown(false);
		settings.setFoldingOutlineShown(false);
		settings.setAdditionalColumnsCount(0);
		settings.setAdditionalLinesCount(0);
		settings.setRightMarginShown(false);

		return editor;
	}

	@NotNull
	private ListPopup newListPopup(ListItem[] goalsAsStrings, boolean shortcut) {
		BaseListPopupStep<ListItem> listPopupStep = new ListItemBaseListPopupStep(goalsAsStrings, shortcut);
		return JBPopupFactory.getInstance().createListPopup(listPopupStep);
	}

	public boolean isPersist() {
		return saveGoalCheckBox.isSelected();
	}


	private class ListItemBaseListPopupStep extends BaseListPopupStep<ListItem> implements ListPopupStepEx<ListItem> {

		public ListItemBaseListPopupStep(ListItem[] goalsAsStrings, boolean shortcut) {
			super(null, goalsAsStrings);
		}

		@Override
		public boolean isSelectable(ListItem value) {
			return true;
		}

		@Nullable
		@Override
		public ListSeparator getSeparatorAbove(ListItem value) {
			return value.separatorAbove != null ? new ListSeparator(value.separatorAbove.getPresentableText()) : null;
		}

		@Override
		public PopupStep onChosen(final ListItem selectedValue, boolean finalChoice) {
			append(selectedValue.getCmd());
			return FINAL_CHOICE;
		}

		@Override
		public PopupStep onChosen(ListItem listItem, boolean finalChoice, int eventModifiers) {
			return onChosen(listItem, finalChoice);
		}

		@Nullable
		@Override
		public String getTooltipTextFor(ListItem listItem) {
			return StringEscapeUtils.escapeHtml(listItem.description);
		}

		@Override
		public void setEmptyText(@NotNull StatusText statusText) {

		}

		@Override
		public boolean isSpeedSearchEnabled() {
			return true;
		}

		@NotNull
		@Override
		public String getTextFor(ListItem value) {
			return value.getPresentableText();
		}
	}

	public class ListItem {
		private String cmd;
		private String description;
		private ListItem separatorAbove;
		private String presentableText;


		public ListItem(String text) {
			this(false, null, text, null);
		}

		public ListItem(String text, String description) {
			this(false, null, text, description);
		}

		public ListItem(boolean shortcut, String shortcutText, String text, String description) {
			if (shortcut) {
				if (shortcutText == null) {
					throw new IllegalArgumentException("shortcut is null");
				}
				this.cmd = shortcutText;
			} else {
				this.cmd = text;
			}
			this.description = description;
			presentableText = createPresentableText(text, shortcutText, shortcut);
		}

		public ListItem(String cmd, String presentableText, String description) {
			this.cmd = cmd;
			this.description = description;
			this.presentableText = presentableText;
		}


		public String getPresentableText() {
			return presentableText;
		}


		public String getDescription() {
			return description;
		}


		private String createPresentableText(String text, String shortcutText, boolean shortcut) {
			String first = text;
			String second = shortcutText;
			if (shortcut) {
				first = shortcutText;
				second = text;
			}

			String s = first;
			if (second != null) {
				if (s != null) {
					s += " (" + second + ")";
				} else {
					s = second;
				}
			}
			return Utils.limitLength(s);
		}

		public ListItem asSeparatorBefore(ListItem listItem) {
			listItem.separatorAbove = this;
			return listItem;
		}

		public ListItem withSeparatorAbove() {
			separatorAbove = new ListItem(null);
			return this;
		}

		public String getCmd() {
			return cmd;
		}
	}
}