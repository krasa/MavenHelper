package krasa.mavenrun.analyzer.action;

import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.navigator.MavenNavigationUtil;
import org.jetbrains.idea.maven.project.MavenProject;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.psi.xml.XmlFile;

/**
 * @author Vojtech Krasa
 */
public class JumpToSourceAction extends BaseAction {

	public JumpToSourceAction(Project project, MavenProject mavenProject, MavenArtifactNode myTreeNode) {
		super(project, mavenProject, myTreeNode, "Jump To Source");
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		final XmlFile xmlFile = getXmlFile();
		final Navigatable navigatable = MavenNavigationUtil.createNavigatableForDependency(project,
				xmlFile.getVirtualFile(), getParentMavenArtifact());
		if (navigatable != null && navigatable.canNavigate()) {
			navigatable.navigate(true);
			return;
		}
		final Notification notification = new Notification(MAVEN_HELPER_DEPENDENCY_ANALYZER_NOTIFICATION, "",
				"Parent dependency not found, it is probably in parent pom", NotificationType.WARNING);
		ApplicationManager.getApplication().invokeLater(new Runnable() {
			@Override
			public void run() {
				Notifications.Bus.notify(notification, project);
			}
		});
	}

}
