package krasa.mavenhelper.analyzer.action;

import org.jetbrains.idea.maven.dom.model.MavenDomDependencies;
import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.util.xml.DomFileElement;

/**
 * @author Vojtech Krasa
 */
public class RemoveDependencyAction extends BaseAction {

	public RemoveDependencyAction(Project project, MavenProject mavenProject, MavenArtifactNode myTreeNode) {
		super(project, mavenProject, myTreeNode, "Remove");
	}

	private void exclude() {
		DomFileElement domFileElement = getDomFileElement(myArtifact);

		if (domFileElement != null) {
			final MavenDomProjectModel rootElement = (MavenDomProjectModel) domFileElement.getRootElement();
			final MavenDomDependencies dependencies = rootElement.getDependencies();
			boolean found = false;

			for (MavenDomDependency mavenDomDependency : dependencies.getDependencies()) {
				if (isSameDependency(myArtifact.getArtifact(), mavenDomDependency)) {
					found = true;
					mavenDomDependency.undefine();
					dependencyDeleted();
				}
			}
			if (!found) {
				final Notification notification = new Notification(MAVEN_HELPER_DEPENDENCY_ANALYZER_NOTIFICATION, "",
						"Parent dependency not found, it is probably in the parent pom", NotificationType.WARNING);
				ApplicationManager.getApplication().invokeLater(new Runnable() {
					@Override
					public void run() {
						Notifications.Bus.notify(notification, myProject);
					}
				});
			}
		}
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		// CommandProcessor for undo and formatting
		CommandProcessor.getInstance().executeCommand(myProject, new Runnable() {
			public void run() {
				ApplicationManager.getApplication().runWriteAction(new Runnable() {
					public void run() {
						exclude();
					}
				});
			}
		}, "Remove", "MavenRunHelper");
	}

	public void dependencyDeleted() {

	}
}
