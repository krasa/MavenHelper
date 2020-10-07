package krasa.mavenhelper.analyzer.action;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.dom.model.MavenDomShortArtifactCoordinates;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.model.MavenId;
import org.jetbrains.idea.maven.navigator.MavenNavigationUtil;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

/**
 * @author Vojtech Krasa
 */
public abstract class BaseAction extends DumbAwareAction {
	private static final Logger LOG = Logger.getInstance(BaseAction.class);

	public static final String MAVEN_HELPER_DEPENDENCY_ANALYZER_NOTIFICATION = "Maven Helper - Dependency Analyzer - notification";

	protected final Project myProject;
	protected final MavenProject myMavenProject;
	protected final MavenArtifactNode myArtifact;

	public BaseAction(Project myProject, MavenProject myMavenProject, MavenArtifactNode myTreeNode, final String text) {
		super(text);
		this.myProject = myProject;
		this.myMavenProject = myMavenProject;
		myArtifact = myTreeNode;
	}

	protected MavenArtifactNode getOldestParentMavenArtifact() {
		MavenArtifactNode oldestParent = myArtifact.getParent();
		if (oldestParent == null) {
			return myArtifact;
		}
		MavenArtifactNode parentNode = oldestParent.getParent();
		while (parentNode != null) {
			oldestParent = parentNode;
			parentNode = oldestParent.getParent();
		}
		return oldestParent;
	}

	protected DomFileElement getDomFileElement(MavenArtifactNode mavenArtifactNode) {
		XmlFile xmlFile = getXmlFile(mavenArtifactNode);
		if (xmlFile == null) {
//			showError(mavenArtifactNode);
			return null;
		}
		DomFileElement<MavenDomProjectModel> fileElement = DomManager.getDomManager(myProject).getFileElement(xmlFile, MavenDomProjectModel.class);

		if (fileElement == null) {
			showError(xmlFile);
		}
		return fileElement;
	}

//	private void showError(MavenArtifactNode mavenArtifactNode) {
//		final Notification notification = new Notification(MAVEN_HELPER_DEPENDENCY_ANALYZER_NOTIFICATION, "",
//				"Pom file not found for "+mavenArtifactNode, NotificationType.ERROR);
//		ApplicationManager.getApplication().invokeLater(new Runnable() {
//			@Override
//			public void run() {
//				Notifications.Bus.notify(notification, myProject);
//			}
//		});}


	private void showError(XmlFile xmlFile) {
		String fileType = xmlFile.getFileType().toString();
		String pluginName = "null";

		PluginId pluginByClassName = PluginManagerCore.getPluginByClassName(xmlFile.getFileType().getClass().getCanonicalName());
		if (pluginByClassName != null) {
			IdeaPluginDescriptor plugin = PluginManagerCore.getPlugin(pluginByClassName);
			if (plugin != null) {
				pluginName = plugin.getName();
			}
		}
		if (!fileType.startsWith("com.intellij")) {
			final Notification notification = new Notification(MAVEN_HELPER_DEPENDENCY_ANALYZER_NOTIFICATION, "",
					"Pom file not found. Possible plugin conflict with plugin: '" + pluginName + "'.\n" +
							"" + fileType + "; " + xmlFile.getVirtualFile(), NotificationType.ERROR);
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				@Override
				public void run() {
					Notifications.Bus.notify(notification, myProject);
				}
			});
		} else {
			final Notification notification = new Notification(MAVEN_HELPER_DEPENDENCY_ANALYZER_NOTIFICATION, "",
					"Pom file not found. " + fileType + "; " + xmlFile.getVirtualFile(), NotificationType.WARNING);
			ApplicationManager.getApplication().invokeLater(new Runnable() {
				@Override
				public void run() {
					Notifications.Bus.notify(notification, myProject);
				}
			});
		}
	}

	protected XmlFile getXmlFile(MavenArtifactNode artifact) {
		VirtualFile virtualFile = getVirtualFile(artifact, myProject, myMavenProject);
		if (virtualFile != null) {
			PsiFile psiFile = PsiManager.getInstance(myProject).findFile(virtualFile);
			if (psiFile instanceof XmlFile) {
				return (XmlFile) psiFile;
			} else {
				Object o = psiFile != null ? psiFile.getVirtualFile() : "";
				LOG.error("Not XmlFile " + psiFile + " " + o+"; artifact="+artifact);
			}
		}
		LOG.error("XmlFile null for virtualFile="+virtualFile + "; artifact="+artifact);

		return null;
	}

	/**
	 * org.jetbrains.idea.maven.navigator.MavenProjectsStructure.DependencyNode#getNavigatable()
	 */
	public static Navigatable getNavigatable(MavenArtifactNode myArtifactNode, Project project, MavenProject mavenProject) {
		final VirtualFile file = getVirtualFile(myArtifactNode, project, mavenProject);
		return file == null ? null : MavenNavigationUtil.createNavigatableForDependency(project, file, myArtifactNode.getArtifact());
	}

	private static VirtualFile getVirtualFile(MavenArtifactNode myArtifactNode, Project project, MavenProject mavenProject) {
		final MavenArtifactNode parent = myArtifactNode.getParent();
		final VirtualFile file;
		if (parent == null) {
			file = mavenProject.getFile();
		} else {
			// final MavenId id = parent.getArtifact().getMavenId(); //this doesn't work for snapshots
			MavenArtifact artifact = parent.getArtifact();
			final MavenId id = new MavenId(artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion());

			final MavenProject pr = MavenProjectsManager.getInstance(project).findProject(id);
			file = pr == null ? MavenNavigationUtil.getArtifactFile(project, id) : pr.getFile();
		}
		return file;
	}

	protected boolean isSameDependency(MavenArtifact parent, MavenDomShortArtifactCoordinates mavenDomDependency) {
		GenericDomValue artifactID = mavenDomDependency.getArtifactId();
		GenericDomValue<String> groupId = mavenDomDependency.getGroupId();
		return isSameDependency(parent, artifactID, groupId);
	}

	protected boolean isSameDependency(MavenArtifact parent, GenericDomValue artifactID, GenericDomValue<String> groupId) {
		return artifactID != null && groupId != null && parent.getArtifactId().equals(artifactID.getValue()) && parent.getGroupId().equals(groupId.getValue());
	}

}
