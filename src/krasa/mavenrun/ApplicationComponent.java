package krasa.mavenrun;

import javax.swing.*;

import krasa.mavenrun.action.MainMavenActionGroup;
import krasa.mavenrun.action.RunGoalAction;
import krasa.mavenrun.action.debug.DebugGoalAction;
import krasa.mavenrun.action.debug.DebugIcons;
import krasa.mavenrun.action.debug.MainMavenDebugActionGroup;
import krasa.mavenrun.gui.ApplicationSettingsForm;
import krasa.mavenrun.model.ApplicationSettings;
import krasa.mavenrun.model.Goal;

import org.apache.commons.lang.WordUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.Constraints;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import icons.MavenIcons;

@State(name = "MavenRunHelper", storages = { @Storage(id = "MavenRunHelper", file = "$APP_CONFIG$/mavenRunHelper.xml") })
public class ApplicationComponent implements com.intellij.openapi.components.ApplicationComponent, Configurable,
		PersistentStateComponent<ApplicationSettings> {
	public static final String RUN_MAVEN = "Run Maven";
	public static final String DEBUG_MAVEN = "Debug Maven";
	private ApplicationSettingsForm form;
	private ApplicationSettings settings = ApplicationSettings.defaultApplicationSettings();

	public void initComponent() {
		addActionGroup(new MainMavenDebugActionGroup(DEBUG_MAVEN, DebugIcons.PluginGoal), RUN_MAVEN);
		addActionGroup(new MainMavenActionGroup(RUN_MAVEN, MavenIcons.Phase), RUN_MAVEN);
		registerActions();
	}

	private void registerActions() {
		ActionManager instance = ActionManager.getInstance();
		for (Goal goal : settings.getAllGoals()) {
			String actionId = getActionId(goal);
			registerAction(instance, actionId, new RunGoalAction(goal, MavenIcons.PluginGoal));
			actionId = getDebugActionId(goal);
			registerAction(instance, actionId, new DebugGoalAction(goal, MavenIcons.PluginGoal));
		}
	}

	private void unRegisterActions() {
		ActionManager instance = ActionManager.getInstance();
		for (Goal goal : settings.getAllGoals()) {
			unRegisterAction(instance, getActionId(goal));
			unRegisterAction(instance, getDebugActionId(goal));
		}
	}

	public void registerAction(Goal o, final RunGoalAction runGoalAction) {
		ActionManager instance = ActionManager.getInstance();
		registerAction(instance, getActionId(o), runGoalAction);
	}

	private void registerAction(ActionManager instance, final String actionId1, final RunGoalAction runGoalAction) {
		unRegisterAction(instance, actionId1);
		instance.registerAction(actionId1, runGoalAction, PluginId.getId("MavenRunHelper"));
	}

	private void unRegisterAction(ActionManager instance, final String actionId) {
		instance.unregisterAction(actionId);
	}

	private String getActionId(Goal goal) {
		return "MavenRunHelper" + WordUtils.capitalizeFully(goal.getCommandLine()).replaceAll(" ", "");
	}

	private String getDebugActionId(Goal goal) {
		return "MavenRunHelperDebug" + WordUtils.capitalizeFully(goal.getCommandLine()).replaceAll(" ", "");

	}

	private void addActionGroup(ActionGroup actionGroup, final String runMaven) {
		DefaultActionGroup editorPopupMenu = (DefaultActionGroup) ActionManager.getInstance().getAction(
				"EditorPopupMenu.Run");
		DefaultActionGroup projectViewPopupMenuRunGroup = (DefaultActionGroup) ActionManager.getInstance().getAction(
				"ProjectViewPopupMenuRunGroup");
		clear(editorPopupMenu, projectViewPopupMenuRunGroup, runMaven);

		add(actionGroup, editorPopupMenu, projectViewPopupMenuRunGroup);
	}

	private void add(ActionGroup actionGroup, DefaultActionGroup editorPopupMenu,
			DefaultActionGroup projectViewPopupMenuRunGroup) {
		editorPopupMenu.add(actionGroup, Constraints.FIRST);
		projectViewPopupMenuRunGroup.add(actionGroup, Constraints.FIRST);
	}

	private void clear(DefaultActionGroup editorPopupMenu, DefaultActionGroup projectViewPopupMenuRunGroup,
			final String runMaven) {
		clear(editorPopupMenu, RUN_MAVEN);
		clear(projectViewPopupMenuRunGroup, runMaven);
	}

	private void clear(DefaultActionGroup editorPopupMenu, final String runMaven) {
		AnAction[] childActionsOrStubs = editorPopupMenu.getChildActionsOrStubs();
		for (AnAction childActionsOrStub : childActionsOrStubs) {
			if (runMaven.equals(childActionsOrStub.getTemplatePresentation().getText())) {
				editorPopupMenu.remove(childActionsOrStub);
			}
		}
	}

	public void disposeComponent() {
		// TODO: insert component disposal logic here
	}

	@NotNull
	public String getComponentName() {
		return "ApplicationComponent";
	}

	@NotNull
	@Override
	public ApplicationSettings getState() {
		if (settings == null) {
			settings = ApplicationSettings.defaultApplicationSettings();
		}
		return settings;
	}

	public void loadState(ApplicationSettings state) {
		if (state.getVersion() < ApplicationSettings.ACTUAL_VERSION) {
			settings = ApplicationSettings.defaultApplicationSettings();
		} else {
			settings = state;
		}
	}

	@Nls
	public String getDisplayName() {
		return "Maven Helper";
	}

	@Nullable
	public String getHelpTopic() {
		return null;
	}

	@Nullable
	public JComponent createComponent() {
		form = new ApplicationSettingsForm(getState());
		return form.getRootComponent();
	}

	public boolean isModified() {
		return form.isSettingsModified(settings);
	}

	public void apply() throws ConfigurationException {
		unRegisterActions();
		settings = form.getSettings().clone();
		registerActions();
	}

	public void reset() {
		if (form != null) {
			form.importFrom(settings);
		}
	}

	public void disposeUIResources() {
		form = null;
	}

	public static ApplicationComponent getInstance() {
		return ApplicationManager.getApplication().getComponent(ApplicationComponent.class);
	}

}
