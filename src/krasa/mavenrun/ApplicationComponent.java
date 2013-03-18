package krasa.mavenrun;

import javax.swing.*;

import krasa.mavenrun.action.MainMavenActionGroup;
import krasa.mavenrun.action.MavenGoalRunAction;
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
		addActionGroup(new MainMavenActionGroup(RUN_MAVEN, MavenIcons.Phase));
		registerActions();
	}

	private void registerActions() {
		ActionManager instance = ActionManager.getInstance();
		for (Goal goal : settings.getGoals()) {
			registerAction(instance, goal);
		}
	}

	private void registerAction(ActionManager instance, Goal goal) {
		String actionId = "MavenRunHelper" + WordUtils.capitalizeFully(goal.getCommandLine()).replaceAll(" ", "");
		instance.unregisterAction(actionId);
		instance.registerAction(actionId, new MavenGoalRunAction(goal, MavenIcons.PluginGoal),
				PluginId.getId("MavenRunHelper"));
		instance.getKeyboardShortcut(actionId);
	}

	public void registerAction(Goal o) {
		ActionManager instance = ActionManager.getInstance();
		registerAction(instance, o);
	}

	private void addActionGroup(ActionGroup actionGroup) {
		DefaultActionGroup editorPopupMenu = (DefaultActionGroup) ActionManager.getInstance().getAction(
				"EditorPopupMenu.Run");
		DefaultActionGroup projectViewPopupMenuRunGroup = (DefaultActionGroup) ActionManager.getInstance().getAction(
				"ProjectViewPopupMenuRunGroup");
		clear(editorPopupMenu, projectViewPopupMenuRunGroup);

		add(actionGroup, editorPopupMenu, projectViewPopupMenuRunGroup);
	}

	private void add(ActionGroup actionGroup, DefaultActionGroup editorPopupMenu,
			DefaultActionGroup projectViewPopupMenuRunGroup) {
		editorPopupMenu.add(actionGroup, Constraints.FIRST);
		projectViewPopupMenuRunGroup.add(actionGroup, Constraints.FIRST);
	}

	private void clear(DefaultActionGroup editorPopupMenu, DefaultActionGroup projectViewPopupMenuRunGroup) {
		clear(editorPopupMenu);
		clear(projectViewPopupMenuRunGroup);
	}

	private void clear(DefaultActionGroup editorPopupMenu) {
		AnAction[] childActionsOrStubs = editorPopupMenu.getChildActionsOrStubs();
		for (AnAction childActionsOrStub : childActionsOrStubs) {
			if (RUN_MAVEN.equals(childActionsOrStub.getTemplatePresentation().getText())) {
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
		settings = state;
	}

	@Nls
	public String getDisplayName() {
		return "Maven Run Helper";
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
		settings = form.getSettings().clone();
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
