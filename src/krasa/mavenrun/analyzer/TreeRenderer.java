package krasa.mavenrun.analyzer;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.model.MavenArtifactState;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 * @author Vojtech Krasa
 */
public class TreeRenderer extends ColoredTreeCellRenderer {

	private JCheckBox showGroupId;
	private SimpleTextAttributes errorBoldAttributes;

	public TreeRenderer(JCheckBox showGroupId) {
		this.showGroupId = showGroupId;
		errorBoldAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, SimpleTextAttributes.ERROR_ATTRIBUTES.getFgColor());
	}

	public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
									  int row, boolean hasFocus) {
		Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
		if (!(userObject instanceof MyTreeUserObject))
			return;

		MyTreeUserObject myTreeUserObject = (MyTreeUserObject) userObject;
		final MavenArtifact artifact = myTreeUserObject.getArtifact();

		String classifier = artifact.getClassifier();
		if (classifier != null) {
			classifier = classifier + " - ";
		} else {
			classifier = "";
		}

		String currentVersion = artifact.getVersion();
		if (myTreeUserObject.showOnlyVersion) {
			append(currentVersion + " [" + classifier + artifact.getScope() + "]", myTreeUserObject.attributes);
		} else {
			MavenArtifactNode mavenArtifactNode = myTreeUserObject.getMavenArtifactNode();
			boolean omitted = mavenArtifactNode.getState() == MavenArtifactState.CONFLICT
				&& (mavenArtifactNode.getRelatedArtifact() == null || !currentVersion.equals(mavenArtifactNode.getRelatedArtifact().getVersion()));

			SimpleTextAttributes attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
			SimpleTextAttributes boldAttributes = SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES;

			if (!"compile".equals(myTreeUserObject.getArtifact().getScope())) {
				attributes = SimpleTextAttributes.GRAYED_ATTRIBUTES;
				boldAttributes = SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES;
			}
			if (omitted) {
				attributes = SimpleTextAttributes.ERROR_ATTRIBUTES;
				boldAttributes = errorBoldAttributes;
			}

			if (showGroupId.isSelected()) {
				append(artifact.getGroupId() + " : ", attributes);
			}
			append(artifact.getArtifactId(), boldAttributes);

			if (omitted) {
				String realVersion = mavenArtifactNode.getRelatedArtifact().getVersion();
				append(" : " + currentVersion + " (omitted for conflict with " + realVersion + ")" + " [" + classifier + artifact.getScope() + "]", attributes);
			} else {
				append(" : " + currentVersion + " [" + classifier + artifact.getScope() + "]", attributes);
			}
		}

	}

}
