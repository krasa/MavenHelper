package krasa.mavenrun.analyzer;

import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;

import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;

/**
 * @author Vojtech Krasa
 */
class MyTreeUserObject {
	public static final SimpleTextAttributes ERROR_ATTRIBUTES_BOLD = new SimpleTextAttributes(
			SimpleTextAttributes.STYLE_BOLD, JBColor.red);

	private MavenArtifactNode mavenArtifactNode;
	protected SimpleTextAttributes attributes;
	protected SimpleTextAttributes boldAttributes;
	boolean showOnlyVersion = false;
	public MyTreeUserObject(MavenArtifactNode mavenArtifactNode) {
		this.mavenArtifactNode = mavenArtifactNode;
		this.attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
		this.boldAttributes = SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES;
	}

	public MyTreeUserObject(MavenArtifactNode mavenArtifactNode, final SimpleTextAttributes regularAttributes,
			SimpleTextAttributes boldAttributes1) {
		this.mavenArtifactNode = mavenArtifactNode;
		this.attributes = regularAttributes;
		this.boldAttributes = boldAttributes1;
	}

	static MyTreeUserObject create(MavenArtifactNode mavenArtifactNode, String maxVersion) {
		SimpleTextAttributes attributes = SimpleTextAttributes.ERROR_ATTRIBUTES;
		SimpleTextAttributes boldAttributes = ERROR_ATTRIBUTES_BOLD;
		if (maxVersion.equals(mavenArtifactNode.getArtifact().getVersion())) {
			attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
			boldAttributes = SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES;
		}
		final MyTreeUserObject myTreeUserObject = new MyTreeUserObject(mavenArtifactNode, attributes, boldAttributes);
		myTreeUserObject.showOnlyVersion = true;
		return myTreeUserObject;
	}

	public MavenArtifact getArtifact() {
		return mavenArtifactNode.getArtifact();
	}

	public MavenArtifactNode getMavenArtifactNode() {
		return mavenArtifactNode;
	}

	@Override
	public String toString() {
		return mavenArtifactNode.getArtifact().getArtifactId();
	}
}
