package krasa.mavenrun.analyzer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jetbrains.idea.maven.model.MavenArtifact;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;

/**
 * @author Vojtech Krasa
 */
public class TreeRenderer extends ColoredTreeCellRenderer {

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

		if (myTreeUserObject.showOnlyVersion) {
			append(artifact.getVersion() + " (" + classifier + artifact.getScope() + ")", myTreeUserObject.attributes);
	} else {
			append(artifact.getGroupId() + ":", SimpleTextAttributes.REGULAR_ATTRIBUTES);
			append(artifact.getArtifactId(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
			append(":" + artifact.getVersion() + " (" + classifier + artifact.getScope() + ")",
			SimpleTextAttributes.REGULAR_ATTRIBUTES);

		}

	}

}
