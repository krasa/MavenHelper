package krasa.mavenrun.analyzer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jetbrains.idea.maven.model.MavenArtifact;

import com.intellij.ui.ColoredTreeCellRenderer;

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
		append(artifact.getDisplayStringSimple(), myTreeUserObject.attributes);
	}

}
