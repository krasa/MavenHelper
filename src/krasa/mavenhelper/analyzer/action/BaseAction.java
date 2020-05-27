package krasa.mavenhelper.analyzer.action;

import com.intellij.openapi.diagnostic.Logger;
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

	protected final Project project;
	protected final MavenProject mavenProject;
	protected final MavenArtifactNode mavenArtifactNode;

	public BaseAction(Project project, MavenProject mavenProject, MavenArtifactNode myTreeNode, final String text) {
		super(text);
		this.project = project;
		this.mavenProject = mavenProject;
		mavenArtifactNode = myTreeNode;
	}

	protected MavenArtifactNode getOldestParentMavenArtifact() {
		MavenArtifactNode oldestParent = mavenArtifactNode.getParent();
		if (oldestParent == null) {
			return mavenArtifactNode;
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
		return xmlFile == null ? null : DomManager.getDomManager(project).getFileElement(xmlFile, MavenDomProjectModel.class);
	}

	protected XmlFile getXmlFile(MavenArtifactNode artifact) {
		VirtualFile virtualFile = getVirtualFile(artifact, project, mavenProject);
		if (virtualFile != null) {
			PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
			if (psiFile instanceof XmlFile) {
				return (XmlFile) psiFile;
			} else {
				LOG.error("Not XmlFile "+psiFile);
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
