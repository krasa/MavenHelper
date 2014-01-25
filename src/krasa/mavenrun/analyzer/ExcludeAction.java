package krasa.mavenrun.analyzer;

import org.jetbrains.idea.maven.dom.model.MavenDomDependencies;
import org.jetbrains.idea.maven.dom.model.MavenDomDependency;
import org.jetbrains.idea.maven.dom.model.MavenDomExclusion;
import org.jetbrains.idea.maven.dom.model.MavenDomExclusions;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.dom.model.MavenDomShortArtifactCoordinates;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;

/**
 * @author Vojtech Krasa
 */
class ExcludeAction extends AnAction {
	private final Project project;
	private final MavenProject mavenProject;
	private final MavenArtifactNode mavenArtifactNode;

	public ExcludeAction(Project project, MavenProject mavenProject, MavenArtifactNode myTreeNode) {
		super("Exclude");
		this.project = project;
		this.mavenProject = mavenProject;
		mavenArtifactNode = myTreeNode;
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
				final Notification notification = new Notification("Maven Helper - Exclude notification", "",
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

	private MavenArtifact getParentMavenArtifact() {
		MavenArtifactNode oldestParent = mavenArtifactNode.getParent();

		MavenArtifactNode parentNode = oldestParent.getParent();
		while (parentNode != null) {
			oldestParent = parentNode;
			parentNode = oldestParent.getParent();
		}
		return oldestParent.getArtifact();
	}

	private DomFileElement getDomFileElement() {
		PsiFile psiFile = PsiManager.getInstance(project).findFile(mavenProject.getFile());
		final XmlFile xmlFile = (XmlFile) psiFile;
		return DomManager.getDomManager(project).getFileElement(xmlFile, MavenDomProjectModel.class);
	}

	private void createExclusion(MavenArtifact artifactToExclude, MavenDomExclusions exclusions) {
		MavenDomExclusion exclusion = exclusions.addExclusion();
		exclusion.getGroupId().setValue(artifactToExclude.getGroupId());
		exclusion.getArtifactId().setValue(artifactToExclude.getArtifactId());
	}

	private boolean isSameDependency(MavenArtifact parent, MavenDomShortArtifactCoordinates mavenDomDependency) {
		GenericDomValue artifactID = mavenDomDependency.getArtifactId();
		GenericDomValue<String> groupId = mavenDomDependency.getGroupId();
		return isSameDependency(parent, artifactID, groupId);
	}

	private boolean isSameDependency(MavenArtifact parent, GenericDomValue artifactID, GenericDomValue<String> groupId) {
		return artifactID != null && groupId != null && parent.getArtifactId().equals(artifactID.getValue())
				&& parent.getGroupId().equals(groupId.getValue());
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
		}, "Exclude", null);
	}

	public void dependencyExcluded() {

	}
}
