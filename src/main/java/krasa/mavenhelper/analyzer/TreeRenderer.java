package krasa.mavenhelper.analyzer;

import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
	private final JCheckBox showSize;
	private final GuiForm guiForm;
	private final SimpleTextAttributes errorBoldAttributes;

	private final SimpleTextAttributes testAttributes;
	private final SimpleTextAttributes testBoldAttributes;

	private final SimpleTextAttributes providedAttributes;
	private final SimpleTextAttributes providedBoldAttributes;

	private final SimpleTextAttributes runtimeAttributes;
	private final SimpleTextAttributes runtimeBoldAttributes;
	public static final SimpleTextAttributes ERROR_BOLD = SimpleTextAttributes.ERROR_ATTRIBUTES.derive(SimpleTextAttributes.STYLE_BOLD, null, null, null);

	public TreeRenderer(JCheckBox showGroupId, JCheckBox showSize, GuiForm guiForm) {
		this.showGroupId = showGroupId;
		this.showSize = showSize;
		this.guiForm = guiForm;
		errorBoldAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, SimpleTextAttributes.ERROR_ATTRIBUTES.getFgColor());

		testAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(4, 111, 0), new Color(0x69AF80)));
		testBoldAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, testAttributes.getFgColor());

		providedAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(0x02516D), new Color(0x028BBA)));
		providedBoldAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, providedAttributes.getFgColor());

		runtimeAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_PLAIN, new JBColor(new Color(0x8D4E81), new Color(0xB264A5)));
		runtimeBoldAttributes = new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, runtimeAttributes.getFgColor());
	}

	public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf,
									  int row, boolean hasFocus) {
		DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
		Object userObject = treeNode.getUserObject();
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
		MavenArtifactNode mavenArtifactNode = myTreeUserObject.getMavenArtifactNode();
		boolean omitted = Utils.isOmitted(mavenArtifactNode);
		boolean conflict_AlternativeMethod = Utils.isConflictAlternativeMethod(mavenArtifactNode);
		boolean error = omitted || conflict_AlternativeMethod;
		String currentVersion = artifact.getVersion();


		if (myTreeUserObject.showOnlyVersion) {
			SimpleTextAttributes attributes = SimpleTextAttributes.REGULAR_ATTRIBUTES;
			if (error) {
				attributes = SimpleTextAttributes.ERROR_ATTRIBUTES;
			}
			append(currentVersion + " [" + classifier + artifact.getScope() + "]", attributes);
			if (showSize.isSelected()) {
				long size = myTreeUserObject.getSize();
				long totalSize = myTreeUserObject.getTotalSize();
				append(" - " + size + " KB (" + totalSize + " KB)");
			}

			checkForBug(myTreeUserObject);
			if (!omitted && conflict_AlternativeMethod) {
				conflict_AlternativeMethod(mavenArtifactNode, attributes, getConflictWinner(mavenArtifactNode));
			}
		} else {
			SimpleTextAttributes attributes;
			SimpleTextAttributes boldAttributes;
			if (error) {
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
			append(" : " + currentVersion, attributes);
			append(" [" + classifier + artifact.getScope() + "]", attributes);

			if (showSize.isSelected()) {
				long size = myTreeUserObject.getSize();
				long totalSize = myTreeUserObject.getTotalSize();
				append(" - " + size + " KB (" + totalSize + " KB)");
			}

			if (error) {
				String winner = getConflictWinner(mavenArtifactNode);

				if (omitted) {
					append(" (omitted for conflict with: " + winner + ")", attributes);
					checkForBug(myTreeUserObject);
				} else {//conflict_AlternativeMethod
					conflict_AlternativeMethod(mavenArtifactNode, attributes, winner);
				}
			}

		}

	}

	private void conflict_AlternativeMethod(MavenArtifactNode mavenArtifactNode, SimpleTextAttributes attributes, String realArtifact) {
		append(" (artifact state: " + mavenArtifactNode.getState() + ", conflict with: " + realArtifact, attributes);
		append(")", attributes);
		append(" - 2)", ERROR_BOLD);
		guiForm.falsePositive.setVisible(true);
	}

	@Nullable
	private String getConflictWinner(MavenArtifactNode mavenArtifactNode) {
		String realArtifact = "null";
		MavenArtifact conflictWinner = mavenArtifactNode.getRelatedArtifact();

		if (conflictWinner != null) {
			String realVersion;
			String realClassifier;
			realVersion = conflictWinner.getVersion();
			realClassifier = conflictWinner.getClassifier();
			String scope = conflictWinner.getScope();
			if (realClassifier != null) {
				realClassifier = realClassifier + " - ";
			} else {
				realClassifier = "";
			}
			realArtifact = realVersion;
//			realArtifact = realVersion + " [" + realClassifier + scope + "]";
		}
		return realArtifact;
	}

	private void checkForBug(MyTreeUserObject myTreeUserObject) {
		MavenArtifactNode mavenArtifactNode = myTreeUserObject.getMavenArtifactNode();
		if (mavenArtifactNode.getState() == MavenArtifactState.CONFLICT && !Utils.isVersionMismatch(myTreeUserObject.getMavenArtifactNode())) {
			append(" - 1)", ERROR_BOLD);
			guiForm.intellijBugLabel.setVisible(true);
		}
	}



}
