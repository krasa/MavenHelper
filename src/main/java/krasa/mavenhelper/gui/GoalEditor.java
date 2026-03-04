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
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.ui.popup.ListPopupStepEx;
import com.intellij.openapi.ui.popup.ListSeparator;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.StatusText;
import krasa.mavenhelper.action.Utils;
import krasa.mavenhelper.i18n.MavenHelperBundle;
import krasa.mavenhelper.model.Alias;
import krasa.mavenhelper.model.ApplicationSettings;
import krasa.mavenhelper.model.Goal;
import krasa.mavenhelper.model.Goals;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.MavenArtifactUtil;
import org.jetbrains.idea.maven.utils.MavenPluginInfo;

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
	private JLabel appendLabel;
	private EditorImpl myEditor;

	public static Goal editGoal(String title, final ApplicationSettings settings1, Goal goal) {
		GoalEditor editor = new GoalEditor(title, goal.getCommandLine(), settings1, false, null, null);
		if (editor.showAndGet()) {
			String s = editor.getCmd();
			if (StringUtils.isNotBlank(s)) {
				goal.setCommandLine(s);
			}
			return goal;
		}
		return null;
	}
	                 
	public GoalEditor(String title, String initialValue, ApplicationSettings applicationSettings, boolean persist, Project project, DataContext dataContext) {
		super(true);
		initLocalization();
		setTitle(title);
		saveGoalCheckBox.setSelected(PropertiesComponent.getInstance().getBoolean(SAVE, true));
		saveGoalCheckBox.setVisible(persist);

		try {
//		optionsPanel.add(new JBLabel("Append:"));
//		optionsPanel.setLayout(new WrapLayout());
			optionsPanel.add(getLinkLabel("-DskipTests", MavenHelperBundle.message("goal.option.skip.tests")));
			optionsPanel.add(getLinkLabel(new ListItem("--update-snapshots", MavenHelperBundle.message("goal.option.update.snapshots"))));
			optionsPanel.add(getLinkLabel(new ListItem("--offline", MavenHelperBundle.message("goal.option.offline"))));
			optionsPanel.add(getLinkLabel(new ListItem("--debug", MavenHelperBundle.message("goal.option.debug"))));
			optionsPanel.add(getLinkLabel(new ListItem("--non-recursive", MavenHelperBundle.message("goal.option.non.recursive"))));

//		optionsPanel2.setLayout(new WrapLayout());
			optionsPanel2.add(listPopup(MavenHelperBundle.message("goal.editor.option"), getOptions(false), false));
			optionsPanel2.add(listPopup(MavenHelperBundle.message("goal.editor.shortcut.option"), getOptions(true), true));

			optionsPanel2.add(listPopup(MavenHelperBundle.message("goal.editor.alias"), toListItems(applicationSettings.getAliases().getAliases()), false));

//		goalsPanel.add(new JBLabel("Goals:"));
//		goalsPanel.setLayout(new WrapLayout());

			goalsPanel.add(listPopup(MavenHelperBundle.message("goal.editor.lifecycle.goal"), getGoals(), false));
			goalsPanel.add(listPopup(MavenHelperBundle.message("goal.editor.existing.goal"), getExistingGoals(applicationSettings), false));
			goalsPanel.add(listPopup(MavenHelperBundle.message("goal.editor.util"), getHelpfulGoals(), false));


			if (dataContext != null) {
				MavenProject mavenProject = Utils.getMavenProject(dataContext);
				if (mavenProject != null) {
					List<ListItem> listItems = new ArrayList<>();
					for (MavenPlugin mavenPlugin : mavenProject.getDeclaredPlugins()) {
						MavenPluginInfo pluginInfo = MavenArtifactUtil.readPluginInfo(mavenProject.getLocalRepository(), mavenPlugin.getMavenId());
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
					goalsPanel.add(listPopup(MavenHelperBundle.message("goal.editor.plugin.goal"), listItems.toArray(new ListItem[0]), false));
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

	private void initLocalization() {
		commandLineLabel.setText(MavenHelperBundle.message("goal.editor.command.line"));
		appendLabel.setText(MavenHelperBundle.message("goal.editor.append"));
		saveGoalCheckBox.setText(MavenHelperBundle.message("goal.editor.save.goal"));
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
				e.separatorAbove = new ListItem(MavenHelperBundle.message("goal.plugin.aware.label"));
			}
			arrayList.add(e);
		}

		return arrayList.toArray(new ListItem[0]);
	}

	private ListItem[] getHelpfulGoals() {
		return new ListItem[]{
				new ListItem("dependency:tree -Dverbose", MavenHelperBundle.message("goal.help.dependency.tree")),
				new ListItem("dependency:analyze -Dverbose", MavenHelperBundle.message("goal.help.dependency.analyze")),

				new ListItem("help:effective-settings", MavenHelperBundle.message("goal.help.effective.settings")).withSeparatorAbove(),
				new ListItem("help:effective-pom", MavenHelperBundle.message("goal.help.effective.pom")),
				new ListItem("help:active-profiles", MavenHelperBundle.message("goal.help.active.profiles")),

				new ListItem("versions:display-dependency-updates", MavenHelperBundle.message("goal.help.versions.dependency")).withSeparatorAbove(),
				new ListItem("versions:display-plugin-updates", MavenHelperBundle.message("goal.help.versions.plugin")),
				new ListItem("versions:display-property-updates", MavenHelperBundle.message("goal.help.versions.property")),
				new ListItem("versions:set -DnewVersion=$version$", MavenHelperBundle.message("goal.help.versions.set")),
				new ListItem("versions:revert", MavenHelperBundle.message("goal.help.versions.revert")),
		};
	}

	private ListItem[] getOptions(boolean shortcut) {
		return new ListItem[]{


				new ListItem(shortcut, "-am", "--also-make", MavenHelperBundle.message("goal.option.also.make")),
				new ListItem(shortcut, "-amd", "--also-make-dependents", MavenHelperBundle.message("goal.option.also.make.dependents")),
				new ListItem(shortcut, "-B", "--batch-mode", MavenHelperBundle.message("goal.option.batch.mode")),
				new ListItem(shortcut, "-C", "--strict-checksums", MavenHelperBundle.message("goal.option.strict.checksums")),
				new ListItem(shortcut, "-c", "--lax-checksums", MavenHelperBundle.message("goal.option.lax.checksums")),
				new ListItem(shortcut, "-cpu", "--check-plugin-updates", MavenHelperBundle.message("goal.option.check.plugin.updates")),
				new ListItem(shortcut, "-D", "--define <arg>", MavenHelperBundle.message("goal.option.define")),
				new ListItem(shortcut, "-e", "--errors", MavenHelperBundle.message("goal.option.errors")),
				new ListItem(shortcut, "-emp", "--encrypt-master-password <arg>", MavenHelperBundle.message("goal.option.encrypt.master.password")),
				new ListItem(shortcut, "-ep", "--encrypt-password <arg>", MavenHelperBundle.message("goal.option.encrypt.password")),
				new ListItem(shortcut, "-f", "--file <arg>", MavenHelperBundle.message("goal.option.file")),
				new ListItem(shortcut, "-fae", "--fail-at-end", MavenHelperBundle.message("goal.option.fail.at.end")),
				new ListItem(shortcut, "-ff", "--fail-fast", MavenHelperBundle.message("goal.option.fail.fast")),
				new ListItem(shortcut, "-fn", "--fail-never", MavenHelperBundle.message("goal.option.fail.never")),
				new ListItem(shortcut, "-gs", "--global-settings <arg>", MavenHelperBundle.message("goal.option.global.settings")),
				new ListItem(shortcut, "-h", "--help", MavenHelperBundle.message("goal.option.help")),
				new ListItem(shortcut, "-l", "--log-file <arg>", MavenHelperBundle.message("goal.option.log.file")),
				new ListItem(shortcut, "-llr", "--legacy-local-repository", MavenHelperBundle.message("goal.option.legacy.local.repository")),
				new ListItem(shortcut, "-N", "--non-recursive", MavenHelperBundle.message("goal.option.non.recursive")),
				new ListItem(shortcut, "-npr", "--no-plugin-registry", MavenHelperBundle.message("goal.option.no.plugin.registry")),
				new ListItem(shortcut, "-npu", "--no-plugin-updates", MavenHelperBundle.message("goal.option.no.plugin.updates")),
				new ListItem(shortcut, "-nsu", "--no-snapshot-updates", MavenHelperBundle.message("goal.option.no.snapshot.updates")),
				new ListItem(shortcut, "-o", "--offline", MavenHelperBundle.message("goal.option.offline")),
				new ListItem(shortcut, "-P", "--activate-profiles <arg>", MavenHelperBundle.message("goal.option.activate.profiles")),
				new ListItem(shortcut, "-pl", "--projects <arg>", MavenHelperBundle.message("goal.option.projects")),
				new ListItem(shortcut, "-q", "--quiet", MavenHelperBundle.message("goal.option.quiet")),
				new ListItem(shortcut, "-rf", "--resume-from <arg>", MavenHelperBundle.message("goal.option.resume.from")),
				new ListItem(shortcut, "-s", "--settings <arg>", MavenHelperBundle.message("goal.option.settings")),
				new ListItem(shortcut, "-T", "--threads <arg>", MavenHelperBundle.message("goal.option.threads")),
				new ListItem(shortcut, "-t", "--toolchains <arg>", MavenHelperBundle.message("goal.option.toolchains")),
				new ListItem(shortcut, "-U", "--update-snapshots", MavenHelperBundle.message("goal.option.update.snapshots")),
				new ListItem(shortcut, "-up", "--update-plugins", MavenHelperBundle.message("goal.option.update.plugins")),
				new ListItem(shortcut, "-V", "--show-version", MavenHelperBundle.message("goal.option.show.version")),
				new ListItem(shortcut, "-v", "--version", MavenHelperBundle.message("goal.option.version")),
				new ListItem(shortcut, "-X", "--debug", MavenHelperBundle.message("goal.option.debug")),

		};
	}

	private ListItem[] getGoals() {
		return new ListItem[]{
				new ListItem("Clean Lifecycle").asSeparatorBefore(new ListItem("pre-clean", MavenHelperBundle.message("goal.lifecycle.clean.pre"))),
				new ListItem("clean", MavenHelperBundle.message("goal.lifecycle.clean")),
				new ListItem("post-clean", MavenHelperBundle.message("goal.lifecycle.clean.post")),

				new ListItem("Default Lifecycle").asSeparatorBefore(new ListItem("validate", MavenHelperBundle.message("goal.lifecycle.default.validate"))),
				new ListItem("initialize", MavenHelperBundle.message("goal.lifecycle.default.initialize")),
				new ListItem("generate-sources", MavenHelperBundle.message("goal.lifecycle.default.generate.sources")),
				new ListItem("process-sources", MavenHelperBundle.message("goal.lifecycle.default.process.sources")),
				new ListItem("generate-resources", MavenHelperBundle.message("goal.lifecycle.default.generate.resources")),
				new ListItem("process-resources", MavenHelperBundle.message("goal.lifecycle.default.process.resources")),
				new ListItem("compile", MavenHelperBundle.message("goal.lifecycle.default.compile")),
				new ListItem("process-classes", MavenHelperBundle.message("goal.lifecycle.default.process.classes")),
				new ListItem("generate-test-sources", MavenHelperBundle.message("goal.lifecycle.default.generate.test.sources")),
				new ListItem("process-test-sources", MavenHelperBundle.message("goal.lifecycle.default.process.test.sources")),
				new ListItem("generate-test-resources", MavenHelperBundle.message("goal.lifecycle.default.generate.test.resources")),
				new ListItem("process-test-resources", MavenHelperBundle.message("goal.lifecycle.default.process.test.resources")),
				new ListItem("test-compile", MavenHelperBundle.message("goal.lifecycle.default.test.compile")),
				new ListItem("process-test-classes", MavenHelperBundle.message("goal.lifecycle.default.process.test.classes")),
				new ListItem("test", MavenHelperBundle.message("goal.lifecycle.default.test")),
				new ListItem("prepare-package", MavenHelperBundle.message("goal.lifecycle.default.prepare.package")),
				new ListItem("package", MavenHelperBundle.message("goal.lifecycle.default.package")),
				new ListItem("pre-integration-test", MavenHelperBundle.message("goal.lifecycle.default.pre.integration.test")),
				new ListItem("integration-test", MavenHelperBundle.message("goal.lifecycle.default.integration.test")),
				new ListItem("post-integration-test", MavenHelperBundle.message("goal.lifecycle.default.post.integration.test")),
				new ListItem("verify", MavenHelperBundle.message("goal.lifecycle.default.verify")),
				new ListItem("install", MavenHelperBundle.message("goal.lifecycle.default.install")),
				new ListItem("deploy", MavenHelperBundle.message("goal.lifecycle.default.deploy")),

				new ListItem("Site Lifecycle").asSeparatorBefore(new ListItem("pre-site", MavenHelperBundle.message("goal.lifecycle.site.pre"))),
				new ListItem("site", MavenHelperBundle.message("goal.lifecycle.site")),
				new ListItem("post-site", MavenHelperBundle.message("goal.lifecycle.site.post")),
				new ListItem("site-deploy", MavenHelperBundle.message("goal.lifecycle.site.deploy")),
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
			@Override
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
			@Override
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
			myEditor.getContentComponent().requestFocus();
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

	@Override
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

	@Override
	protected void dispose() {
		super.dispose();
		if (myEditor != null) {
			Disposer.dispose(myEditor.getDisposable());
		}
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

		// //		@Override
		public PopupStep onChosen(ListItem listItem, boolean finalChoice, int eventModifiers) {
			return onChosen(listItem, finalChoice);
		}

		@Nullable
		@Override
		public String getTooltipTextFor(ListItem listItem) {
			return StringEscapeUtils.escapeHtml4(listItem.description);
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
