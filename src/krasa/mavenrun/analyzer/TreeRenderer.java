package krasa.mavenrun.analyzer;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jetbrains.idea.maven.model.MavenArtifact;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.model.MavenArtifactState;

/**
 * @author Vojtech Krasa
 */
public class TreeRenderer extends ColoredTreeCellRenderer {

	private JCheckBox showGroupId;

	public TreeRenderer(JCheckBox showGroupId) {
		this.showGroupId = showGroupId;
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

        String curVersion = artifact.getVersion();
        if (myTreeUserObject.showOnlyVersion) {
			append(curVersion + " [" + classifier + artifact.getScope() + "]", myTreeUserObject.attributes);
		} else {
			SimpleTextAttributes attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
			SimpleTextAttributes boldAttributes = SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES;
			if (!"compile".equals(myTreeUserObject.getArtifact().getScope())) {
				attributes = SimpleTextAttributes.GRAYED_ATTRIBUTES;
				boldAttributes = SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES;
			}
			if (showGroupId.isSelected()) {
				append(artifact.getGroupId() + " : ", attributes);
			}
			append(artifact.getArtifactId(), boldAttributes);
            MavenArtifactNode mavenArtifactNode = myTreeUserObject.getMavenArtifactNode();
            if(mavenArtifactNode.getState() == MavenArtifactState.CONFLICT
                    && (mavenArtifactNode.getRelatedArtifact() == null || !curVersion.equals(mavenArtifactNode.getRelatedArtifact().getVersion()))){
                String realVersion = mavenArtifactNode.getRelatedArtifact().getVersion();
                append(" : " + curVersion +" (omitted for conflict with"+ realVersion +")" + " [" + classifier + artifact.getScope() + "]", attributes);
            } else {
                append(" : " + curVersion + " [" + classifier + artifact.getScope() + "]", attributes);
            }
		}

	}

}
