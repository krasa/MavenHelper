package krasa.mavenrun.analyzer.action;

import org.jetbrains.idea.maven.dom.model.*;
import org.jetbrains.idea.maven.model.MavenArtifact;
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
public class ExcludeDependencyAction extends BaseAction {

	public ExcludeDependencyAction(Project project, MavenProject mavenProject, MavenArtifactNode myTreeNode) {
		super(project, mavenProject, myTreeNode, "Exclude");
	}

	private void exclude() {
		final MavenArtifact artifactToExclude = mavenArtifactNode.getArtifact();
		final MavenArtifact oldestParent = getParentMavenArtifact();

		DomFileElement domFileElement = getDomFileElement();

		if (domFileElement != null) {
			final MavenDomProjectModel rootElement = (MavenDomProjectModel) domFileElement.getRootElement();
			final MavenDomDependencies dependencies = rootElement.getDependencies();
			boolean found = false;

			for (MavenDomDependency mavenDomDependency : dependencies.getDependencies()) {
				if (isSameDependency(oldestParent, mavenDomDependency)) {
					found = true;
					final MavenDomExclusions exclusions = mavenDomDependency.getExclusions();
					for (MavenDomExclusion mavenDomExclusion : exclusions.getExclusions()) {
						if (isSameDependency(artifactToExclude, mavenDomExclusion)) {
							return;
						}
					}
					createExclusion(artifactToExclude, exclusions);
					dependencyExcluded();
				}
			}
			if (!found) {
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
	}

	private void createExclusion(MavenArtifact artifactToExclude, MavenDomExclusions exclusions) {
		MavenDomExclusion exclusion = exclusions.addExclusion();
		exclusion.getGroupId().setValue(artifactToExclude.getGroupId());
		exclusion.getArtifactId().setValue(artifactToExclude.getArtifactId());
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		// CommandProcessor for undo and formatting
		CommandProcessor.getInstance().executeCommand(project, new Runnable() {
			public void run() {
				ApplicationManager.getApplication().runWriteAction(new Runnable() {
					public void run() {
						exclude();
					}
				});
			}
		}, "Exclude", "MavenRunHelper");
	}

	public void dependencyExcluded() {

	}
}
