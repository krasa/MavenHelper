package krasa.mavenhelper.analyzer.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.ui.KeyStrokeAdapter;

/**
 * @author Vojtech Krasa
 */
public class JumpToSourceAction extends BaseAction {

	public JumpToSourceAction(Project project, MavenProject mavenProject, MavenArtifactNode myTreeNode) {
		super(project, mavenProject, myTreeNode, getLabel());
	}

	@NotNull
	private static String getLabel() {
		Shortcut[] shortcuts = KeymapManager.getInstance().getActiveKeymap().getShortcuts("EditSource");
		if (shortcuts.length > 0) {
			Shortcut shortcut = shortcuts[0];
			if (shortcut.isKeyboard()) {
				KeyboardShortcut key = (KeyboardShortcut) shortcut;
				String s = KeyStrokeAdapter.toString(key.getFirstKeyStroke());
				if (s != null) {
					return "Jump To Source [" + s.toUpperCase() + "]";
				}
			}
		}
		return "Jump To Source";
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		final Navigatable navigatable = getNavigatable(mavenArtifactNode, project, mavenProject);
		if (navigatable != null && navigatable.canNavigate()) {
			navigatable.navigate(true);
		} else {
			final Notification notification = new Notification(MAVEN_HELPER_DEPENDENCY_ANALYZER_NOTIFICATION, "", "Parent dependency not found, strange...",
					NotificationType.WARNING);
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				@Override
				public void run() {
					Notifications.Bus.notify(notification, project);
				}
			});
		}
	}

}
