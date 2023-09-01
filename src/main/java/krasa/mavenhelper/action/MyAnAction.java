package krasa.mavenhelper.action;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.util.NlsActions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import javax.swing.*;

public abstract class MyAnAction extends DumbAwareAction {

	public MyAnAction() {
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.BGT;
	}

	public MyAnAction(@Nullable @NlsActions.ActionText String text, @Nullable @NlsActions.ActionDescription String description, @Nullable Icon icon) {
		super(text, description, icon);
	}


	public MyAnAction(@Nullable Icon icon) {
		super(icon);
	}

	public MyAnAction(@Nullable @NlsActions.ActionText String text) {
		super(text);
	}


	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		Presentation p = e.getPresentation();
		p.setEnabled(isEnabled(e));
		p.setVisible(isVisible(e));
	}

	protected boolean isVisible(AnActionEvent e) {
		return MavenActionUtil.isMavenizedProject(e.getDataContext());
	}

	protected boolean isEnabled(AnActionEvent e) {
		return MavenActionUtil.hasProject(e.getDataContext()) && Utils.getMavenProject(e.getDataContext()) != null;
	}

}
