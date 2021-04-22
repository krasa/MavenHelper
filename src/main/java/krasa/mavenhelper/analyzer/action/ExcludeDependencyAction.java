package krasa.mavenhelper.analyzer.action;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.util.xml.DomFileElement;
import org.jetbrains.idea.maven.dom.model.*;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;

/**
 * @author Vojtech Krasa
 */
public abstract class ExcludeDependencyAction extends BaseAction {

	public ExcludeDependencyAction(Project project, MavenProject mavenProject, MavenArtifactNode myTreeNode) {
		super(project, mavenProject, myTreeNode, "Exclude");
	}



	private void exclude() {
		final MavenArtifact artifactToExclude = myArtifact.getArtifact();
		final MavenArtifactNode oldestParent = getOldestParentMavenArtifact();

		DomFileElement domFileElement = getDomFileElement(oldestParent);

		if (domFileElement != null) {
			final MavenDomProjectModel rootElement = (MavenDomProjectModel) domFileElement.getRootElement();
			final MavenDomDependencies dependencies = rootElement.getDependencies();
			boolean found = false;

			for (MavenDomDependency mavenDomDependency : dependencies.getDependencies()) {
				if (isSameDependency(oldestParent.getArtifact(), mavenDomDependency)) {
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

	private void createExclusion(MavenArtifact artifactToExclude, MavenDomExclusions exclusions) {
		MavenDomExclusion exclusion = exclusions.addExclusion();
		exclusion.getGroupId().setValue(artifactToExclude.getGroupId());
		exclusion.getArtifactId().setValue(artifactToExclude.getArtifactId());
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		// CommandProcessor for undo and formatting
		CommandProcessor.getInstance().executeCommand(myProject, new Runnable() {
			@Override
			public void run() {
				ApplicationManager.getApplication().runWriteAction(new Runnable() {
					@Override
					public void run() {
						exclude();
					}
				});
			}
		}, "Exclude", "MavenRunHelper");
	}

	public abstract void dependencyExcluded();
}
