package krasa.mavenhelper.analyzer;

import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;
import krasa.mavenhelper.MavenHelperApplicationService;
import krasa.mavenhelper.analyzer.action.BaseAction;
import krasa.mavenhelper.model.ApplicationSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProject;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

import static com.intellij.ui.ColorUtil.darker;
import static com.intellij.ui.ColorUtil.softer;

public class MyHighlightingTree extends Tree implements DataProvider {
	private Project project;
	private MavenProject mavenProject;

	public MyHighlightingTree() {
		setOpaque(false);
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public void setMavenProject(MavenProject mavenProject) {
		this.mavenProject = mavenProject;
	}

	@Override
	public Color getFileColorFor(Object object) {
		if (object instanceof MyTreeUserObject) {
			if (((MyTreeUserObject) object).isHighlight()) {
				ApplicationSettings state = MavenHelperApplicationService.getInstance().getState();
				if (UIUtil.isUnderDarcula()) {
					return darker(new JBColor(new Color(state.getSearchBackgroundColor()), new Color(state.getSearchBackgroundColor())), 8);
				} else {
					return softer(new JBColor(new Color(state.getSearchBackgroundColor()), new Color(state.getSearchBackgroundColor())));
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
		try {
			if (PlatformCoreDataKeys.BGT_DATA_PROVIDER.is(s)) {
				return (DataProvider) slowId -> getSlowData(slowId);
			}

			return null;
		} catch (NoSuchFieldError e) { // https://github.com/krasa/MavenHelper/issues/111 java.lang.NoSuchFieldError: BGT_DATA_PROVIDER
			return getSlowData(s);
		}
	}

	private @Nullable Object getSlowData(@NotNull String s) {
		if (CommonDataKeys.NAVIGATABLE.is(s)) {
			final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.getLastSelectedPathComponent();
			if (selectedNode == null) {
				return null;
			}
			if (project == null) {
				return null;
			}
			final MyTreeUserObject myTreeUserObject = (MyTreeUserObject) selectedNode.getUserObject();
			return BaseAction.getNavigatable(myTreeUserObject.getMavenArtifactNode(), project, mavenProject);
		}

		return null;
	}
}
