package krasa.mavenhelper;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import krasa.mavenhelper.action.MainMavenActionGroup;
import krasa.mavenhelper.action.RunGoalAction;
import krasa.mavenhelper.action.RunTestFileAction;
import krasa.mavenhelper.action.debug.DebugGoalAction;
import krasa.mavenhelper.action.debug.DebugTestFileAction;
import krasa.mavenhelper.action.debug.MainMavenDebugActionGroup;
import krasa.mavenhelper.icons.MyIcons;
import krasa.mavenhelper.model.ApplicationSettings;
import krasa.mavenhelper.model.Goal;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.NotNull;

@State(name = "MavenRunHelper", storages = {@Storage("mavenRunHelper.xml")})
public class MavenHelperApplicationService implements PersistentStateComponent<ApplicationSettings> {
	static final Logger LOG = Logger.getInstance(MavenHelperApplicationService.class);

	public static final String RUN_MAVEN = "Run Maven";
	public static final String DEBUG_MAVEN = "Debug Maven";
	private ApplicationSettings settings = new ApplicationSettings();

	public void initShortcuts() {
		addActionGroup(new MainMavenDebugActionGroup(DEBUG_MAVEN, MyIcons.ICON), DEBUG_MAVEN);
		addActionGroup(new MainMavenActionGroup(RUN_MAVEN, MyIcons.RUN_MAVEN_ICON), RUN_MAVEN);
		registerActions();
	}

	public void registerActions() {
		ActionManager instance = ActionManager.getInstance();
		for (Goal goal : settings.getAllGoals()) {
			String actionId = getActionId(goal);
			registerAction(instance, actionId, RunGoalAction.create(goal, MyIcons.PLUGIN_GOAL, false, null));
			actionId = getDebugActionId(goal);
			registerAction(instance, actionId, DebugGoalAction.createDebug(goal, MyIcons.PLUGIN_GOAL, false, null));
		}
		registerAction(instance, "krasa.MavenHelper.RunTestFileAction", new RunTestFileAction().alwaysVisible());
		registerAction(instance, "krasa.MavenHelper.DebugTestFileAction", new DebugTestFileAction().alwaysVisible());
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

	private void addActionGroup(ActionGroup actionGroup, String name) {
		DefaultActionGroup editorPopupMenu = (DefaultActionGroup) ActionManager.getInstance().getAction(
				"EditorPopupMenu.Run");
		DefaultActionGroup projectViewPopupMenuRunGroup = (DefaultActionGroup) ActionManager.getInstance().getAction(
				"ProjectViewPopupMenuRunGroup");
		DefaultActionGroup mavenHelperBaseProjectMenu = (DefaultActionGroup) ActionManager.getInstance().getAction(
				"MavenHelper.BaseProjectMenu");
		clear(editorPopupMenu, projectViewPopupMenuRunGroup, mavenHelperBaseProjectMenu, name);

		// Now it splatted to standalone blocks, so actions can be initialized separately.
		if (settings.isInitializeEditorPopups()) {
			editorPopupMenu.add(actionGroup, Constraints.FIRST);
		}
		if (settings.isInitializeProjectPopups()) {
			projectViewPopupMenuRunGroup.add(actionGroup, Constraints.FIRST);
		}
		if (settings.isInitializeMavenGroupPopups()) {
			mavenHelperBaseProjectMenu.add(actionGroup, Constraints.FIRST);
		}
	}

	/** Saved for compatibility. */
	@SuppressWarnings("unused")
	private void add(ActionGroup actionGroup, DefaultActionGroup editorPopupMenu,
					 DefaultActionGroup projectViewPopupMenuRunGroup, DefaultActionGroup viewPopupMenuRunGroup) {
		editorPopupMenu.add(actionGroup, Constraints.FIRST);
		projectViewPopupMenuRunGroup.add(actionGroup, Constraints.FIRST);
		viewPopupMenuRunGroup.add(actionGroup, Constraints.FIRST);
	}

	/** Saved for compatibility. */
	private void clear(DefaultActionGroup editorPopupMenu, DefaultActionGroup projectViewPopupMenuRunGroup,
					   DefaultActionGroup mavenHelperBaseProjectMenu, String name) {
		clear(editorPopupMenu, name);
		clear(projectViewPopupMenuRunGroup, name);
		clear(mavenHelperBaseProjectMenu, name);
	}

	private void clear(DefaultActionGroup editorPopupMenu, String name) {
		AnAction[] childActionsOrStubs = editorPopupMenu.getChildActionsOrStubs();
		for (AnAction childActionsOrStub : childActionsOrStubs) {
			if (name.equals(childActionsOrStub.getTemplatePresentation().getText())) {
				editorPopupMenu.remove(childActionsOrStub);
			}
		}
	}

	@NotNull
	@Override
	public ApplicationSettings getState() {
		if (settings == null) {
			settings = new ApplicationSettings();
		}
		return settings;
	}

	@Override
	public void loadState(ApplicationSettings state) {
		settings = state;
		ApplicationSettings.addDefaultAliases(settings.getAliases());
	}


	public static MavenHelperApplicationService getInstance() {
		return ApplicationManager.getApplication().getService(MavenHelperApplicationService.class);
	}

}
