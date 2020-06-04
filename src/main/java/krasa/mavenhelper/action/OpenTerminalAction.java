package krasa.mavenhelper.action;

import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;
import org.jetbrains.plugins.terminal.TerminalView;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public class OpenTerminalAction extends AnAction implements DumbAware {

	private boolean pluginEnabled;

	public OpenTerminalAction() {
		pluginEnabled = isPluginEnabled();
	}

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
		super.update(e);
		Presentation p = e.getPresentation();
		p.setEnabled(isAvailable(e));
		p.setVisible(isVisible(e));
	}

	protected boolean isAvailable(AnActionEvent e) {
		return pluginEnabled && MavenActionUtil.hasProject(e.getDataContext());
	}

	protected boolean isVisible(AnActionEvent e) {
		return pluginEnabled && MavenActionUtil.getMavenProject(e.getDataContext()) != null;
	}

	private boolean isPluginEnabled() {
		IdeaPluginDescriptor plugin = PluginManager.getPlugin(PluginId.getId("org.jetbrains.plugins.terminal"));
		if (plugin != null) {
			return plugin.isEnabled();
		}
		return false;
	}
}
