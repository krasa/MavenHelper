package krasa.mavenhelper.action;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.plugins.terminal.TerminalView;

public class OpenTerminalAction extends MyAnAction {

	private boolean pluginEnabled;

	public OpenTerminalAction() {
		pluginEnabled = isTerminalEnabled();
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		Project project = getEventProject(e);
		if (project == null) {
			return;
		}
		VirtualFile fileByUrl = Utils.getPomDir(e);
		if (fileByUrl != null) {
			TerminalView.getInstance(project).openTerminalIn(fileByUrl);
		}
	}

	@Override
	public void update(AnActionEvent e) {
		Presentation p = e.getPresentation();
		p.setEnabled(pluginEnabled && isEnabled(e));
		p.setVisible(pluginEnabled && isVisible(e));
	}

	public static boolean isTerminalEnabled() {
		IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("org.jetbrains.plugins.terminal"));
		if (plugin != null) {
			return plugin.isEnabled();
		}
		return false;
	}
}
