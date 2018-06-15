package krasa.mavenhelper;

import org.apache.commons.lang.WordUtils;
import org.jetbrains.annotations.NotNull;

import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;

import icons.MavenIcons;
import krasa.mavenhelper.action.MainMavenActionGroup;
import krasa.mavenhelper.action.RunGoalAction;
import krasa.mavenhelper.action.RunTestFileAction;
import krasa.mavenhelper.action.debug.Debug;
import krasa.mavenhelper.action.debug.DebugGoalAction;
import krasa.mavenhelper.action.debug.DebugTestFileAction;
import krasa.mavenhelper.action.debug.MainMavenDebugActionGroup;
import krasa.mavenhelper.model.ApplicationSettings;
import krasa.mavenhelper.model.Goal;

@State(name = "MavenRunHelper", storages = { @Storage(file = "$APP_CONFIG$/mavenRunHelper.xml") })
public class ApplicationComponent implements com.intellij.openapi.components.ApplicationComponent,
		PersistentStateComponent<ApplicationSettings> {
	static final Logger LOG = Logger.getInstance(ApplicationComponent.class);

	public static final NotificationGroup NOTIFICATION = new NotificationGroup("Maven Helper",
		NotificationDisplayType.STICKY_BALLOON, true);
	
	
	public static final String RUN_MAVEN = "Run Maven";
	public static final String DEBUG_MAVEN = "Debug Maven";
	private ApplicationSettings settings = ApplicationSettings.defaultApplicationSettings();

	public void initComponent() {
		addActionGroup(new MainMavenDebugActionGroup(DEBUG_MAVEN, Debug.ICON), RUN_MAVEN);
		addActionGroup(new MainMavenActionGroup(RUN_MAVEN, MavenIcons.Phase), RUN_MAVEN);
		registerActions();
	}

	public void registerActions() {
		ActionManager instance = ActionManager.getInstance();
		for (Goal goal : settings.getAllGoals()) {
			String actionId = getActionId(goal);
			registerAction(instance, actionId, RunGoalAction.create(goal, MavenIcons.PluginGoal, false));
			actionId = getDebugActionId(goal);
			registerAction(instance, actionId, DebugGoalAction.createDebug(goal, MavenIcons.PluginGoal, false));
		}
		registerAction(instance, "krasa.MavenHelper.RunTestFileAction", new RunTestFileAction());
		registerAction(instance, "krasa.MavenHelper.DebugTestFileAction", new DebugTestFileAction());
	}

	public void unRegisterActions() {
		ActionManager instance = ActionManager.getInstance();
		for (Goal goal : settings.getAllGoals()) {
			unRegisterAction(instance, getActionId(goal));
			unRegisterAction(instance, getDebugActionId(goal));
		}
	}

	public void registerAction(Goal o, RunGoalAction runGoalAction) {
		ActionManager instance = ActionManager.getInstance();
		registerAction(instance, getActionId(o), runGoalAction);
	}

	private void registerAction(ActionManager instance, String actionId1, AnAction runGoalAction) {
		unRegisterAction(instance, actionId1);
		instance.registerAction(actionId1, runGoalAction, PluginId.getId("MavenRunHelper"));
	}

	private void unRegisterAction(ActionManager instance, String actionId) {
		instance.unregisterAction(actionId);
	}

	private String getActionId(Goal goal) {
		return "MavenRunHelper" + WordUtils.capitalizeFully(goal.getCommandLine()).replaceAll(" ", "");
	}

	private String getDebugActionId(Goal goal) {
		return "MavenRunHelperDebug" + WordUtils.capitalizeFully(goal.getCommandLine()).replaceAll(" ", "");

	}

	private void addActionGroup(ActionGroup actionGroup, String runMaven) {
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
			String runMaven) {
		clear(editorPopupMenu, RUN_MAVEN);
		clear(projectViewPopupMenuRunGroup, runMaven);
	}

	private void clear(DefaultActionGroup editorPopupMenu, String runMaven) {
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

	@Override
	public void loadState(ApplicationSettings state) {
		if (state.getVersion() < ApplicationSettings.ACTUAL_VERSION) {
			settings = ApplicationSettings.defaultApplicationSettings();
		} else {
			settings = state;
		}
	}


	public static ApplicationComponent getInstance() {
		return ApplicationManager.getApplication().getComponent(ApplicationComponent.class);
	}

}
