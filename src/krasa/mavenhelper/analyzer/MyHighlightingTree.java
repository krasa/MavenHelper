package krasa.mavenhelper.analyzer;

import static com.intellij.ui.ColorUtil.darker;
import static com.intellij.ui.ColorUtil.softer;

import java.awt.*;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProject;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;

import krasa.mavenhelper.analyzer.action.BaseAction;

public class MyHighlightingTree extends Tree implements DataProvider {
	private final Project project;
	private final MavenProject mavenProject;

	public MyHighlightingTree(Project project, MavenProject mavenProject) {
		this.project = project;
		this.mavenProject = mavenProject;
		setOpaque(false);
	}

	@Override
	public Color getFileColorFor(Object object) {
		if (object instanceof MyTreeUserObject) {
			if (((MyTreeUserObject) object).isHighlight()) {
				if (UIUtil.isUnderDarcula()) {
					return darker(JBColor.CYAN, 8);
				} else {
					return softer(Color.CYAN);
				}
			}
		}
		return super.getFileColorFor(object);
	}

	@Override
	public boolean isFileColorsEnabled() {
		return true;
	}

	@Nullable
	@Override
	public Object getData(String s) {
		if (CommonDataKeys.NAVIGATABLE.is(s)) {
			final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();
			if (selectedNode == null) {
				return null;
			}
			final MyTreeUserObject myTreeUserObject = (MyTreeUserObject) selectedNode.getUserObject();
			return BaseAction.getNavigatable(myTreeUserObject.getMavenArtifactNode(), project, mavenProject);
		}
		return null;
	}
}
