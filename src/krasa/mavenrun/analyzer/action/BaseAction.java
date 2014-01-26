package krasa.mavenrun.analyzer.action;

import org.jetbrains.idea.maven.dom.model.MavenDomProjectModel;
import org.jetbrains.idea.maven.dom.model.MavenDomShortArtifactCoordinates;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;

import com.intellij.openapi.project.DumbAwareAction;
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
public abstract class BaseAction extends DumbAwareAction {
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

	protected MavenArtifact getParentMavenArtifact() {
		MavenArtifactNode oldestParent = mavenArtifactNode.getParent();
		if (oldestParent == null) {
			return mavenArtifactNode.getArtifact();
		}
		MavenArtifactNode parentNode = oldestParent.getParent();
		while (parentNode != null) {
			oldestParent = parentNode;
			parentNode = oldestParent.getParent();
		}
		return oldestParent.getArtifact();
	}

	protected DomFileElement getDomFileElement() {
		final XmlFile xmlFile = getXmlFile();
		return DomManager.getDomManager(project).getFileElement(xmlFile, MavenDomProjectModel.class);
	}

	protected XmlFile getXmlFile() {
		PsiFile psiFile = PsiManager.getInstance(project).findFile(mavenProject.getFile());
		return (XmlFile) psiFile;
	}

	protected boolean isSameDependency(MavenArtifact parent, MavenDomShortArtifactCoordinates mavenDomDependency) {
		GenericDomValue artifactID = mavenDomDependency.getArtifactId();
		GenericDomValue<String> groupId = mavenDomDependency.getGroupId();
		return isSameDependency(parent, artifactID, groupId);
	}

	protected boolean isSameDependency(MavenArtifact parent, GenericDomValue artifactID, GenericDomValue<String> groupId) {
		return artifactID != null && groupId != null && parent.getArtifactId().equals(artifactID.getValue())
				&& parent.getGroupId().equals(groupId.getValue());
	}

}
