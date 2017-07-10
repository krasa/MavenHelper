package krasa.mavenhelper;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class RefreshIconsAction extends AnAction {
	public void actionPerformed(AnActionEvent e) {
		ApplicationComponent.getInstance().initComponent();
	}
}
