package krasa.mavenrun.analyzer;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.idea.maven.model.MavenArtifact;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.model.MavenArtifactState;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * @author Vojtech Krasa
 */
public class TreeRenderer extends ColoredTreeCellRenderer {

	private final JCheckBox showGroupId;
	private final SimpleTextAttributes errorBoldAttributes;

	private final SimpleTextAttributes testAttributes;
	private final SimpleTextAttributes testBoldAttributes;

	private final SimpleTextAttributes providedAttributes;
	private final SimpleTextAttributes providedBoldAttributes;

	private final SimpleTextAttributes runtimeAttributes;
	private final SimpleTextAttributes runtimeBoldAttributes;

	public TreeRenderer(JCheckBox showGroupId) {
		this.showGroupId = showGroupId;
		errorBoldAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, SimpleTextAttributes.ERROR_ATTRIBUTES.getFgColor());

		testAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(4, 111, 0), new Color(0x69AF80)));
		testBoldAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, testAttributes.getFgColor());

		providedAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(0x02516D), new Color(0x028BBA)));
		providedBoldAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, providedAttributes.getFgColor());

		runtimeAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(0x8D4E81), new Color(0xB264A5)));
		runtimeBoldAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, runtimeAttributes.getFgColor());
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

			SimpleTextAttributes attributes;
			SimpleTextAttributes boldAttributes;
			if (omitted) {
				attributes = SimpleTextAttributes.ERROR_ATTRIBUTES;
				boldAttributes = errorBoldAttributes;
			} else if ("test".equals(myTreeUserObject.getArtifact().getScope())) {
				attributes = testAttributes;
				boldAttributes = testBoldAttributes;
			} else if ("provided".equals(myTreeUserObject.getArtifact().getScope())) {
				attributes = providedAttributes;
				boldAttributes = providedBoldAttributes;
			} else if ("runtime".equals(myTreeUserObject.getArtifact().getScope())) {
				attributes = runtimeAttributes;
				boldAttributes = runtimeBoldAttributes;
			} else if ("compile".equals(myTreeUserObject.getArtifact().getScope())) {
				attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
				boldAttributes = SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES;
			} else {
				attributes = SimpleTextAttributes.GRAYED_ATTRIBUTES;
				boldAttributes = SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES;
			}

			if (showGroupId.isSelected()) {
				append(artifact.getGroupId() + " : ", attributes);
			}
			append(artifact.getArtifactId(), boldAttributes);

			if (omitted) {
				MavenArtifact relatedArtifact = mavenArtifactNode.getRelatedArtifact();
				String realVersion = null;
				if (relatedArtifact != null) {
					realVersion = relatedArtifact.getVersion();
				}
				append(" : " + currentVersion + " (omitted for conflict with " + realVersion + ")" + " [" + classifier + artifact.getScope() + "]", attributes);
			} else {
				append(" : " + currentVersion + " [" + classifier + artifact.getScope() + "]", attributes);
			}
		}

	}

}
