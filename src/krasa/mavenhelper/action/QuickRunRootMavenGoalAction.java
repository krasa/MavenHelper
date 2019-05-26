package krasa.mavenhelper.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import krasa.mavenhelper.model.Goal;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import javax.swing.*;

public class QuickRunRootMavenGoalAction extends QuickRunMavenGoalAction implements DumbAware {

	private final Logger LOG = Logger.getInstance("#" + getClass().getCanonicalName());

	@Override
	protected void fillActions(final Project currentProject, DefaultActionGroup group, DataContext dataContext) {
		if (currentProject != null) {
			group.addAll(new RootMavenActionGroup() {

				@Override
				protected AnAction createGoalRunAction(Goal goal, final Icon icon, boolean plugin, MavenProjectInfo mavenProject) {
					return QuickRunRootMavenGoalAction.this.createGoalRunAction(goal, icon, plugin, mavenProject);
				}
			}.getActions(dataContext, currentProject));
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
		return MavenActionUtil.hasProject(e.getDataContext());
	}

	protected boolean isVisible(AnActionEvent e) {
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		return mavenProject != null;
	}

	protected JBPopupFactory.ActionSelectionAid getAidMethod() {
		return JBPopupFactory.ActionSelectionAid.SPEEDSEARCH;
	}

}
