package krasa.mavenhelper.analyzer.action;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.KeyStrokeAdapter;
import krasa.mavenhelper.analyzer.GuiForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.project.MavenProject;

/**
 * @author Vojtech Krasa
 */
public class JumpToLeftTreeListAction extends BaseAction {

	private final GuiForm guiForm;

	public JumpToLeftTreeListAction(Project project, MavenProject mavenProject, MavenArtifactNode myTreeNode, GuiForm guiForm) {
		super(project, mavenProject, myTreeNode, getLabel());
		this.guiForm = guiForm;
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		guiForm.switchToLeftTree(myArtifact);
	}

	@NotNull
	private static String getLabel() {
		Shortcut[] shortcuts = KeymapManager.getInstance().getActiveKeymap().getShortcuts("EditSource");
		if (shortcuts.length > 0) {
			Shortcut shortcut = shortcuts[0];
			if (shortcut.isKeyboard()) {
				KeyboardShortcut key = (KeyboardShortcut) shortcut;
				String s = KeyStrokeAdapter.toString(key.getFirstKeyStroke());
				if (s != null) {
					return "Jump to Left Tree [" + s.toUpperCase() + "]";
				}
			}
		}
		return "Jump to Left Tree";
	}

}
