package krasa.mavenhelper.action;

import com.intellij.ide.actions.QuickSwitchSchemeAction;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;

public class QuickRunRootMavenGoalAction extends QuickSwitchSchemeAction implements DumbAware {

	private final Logger LOG = Logger.getInstance("#" + getClass().getCanonicalName());

	@Override
	protected void fillActions(final Project currentProject, DefaultActionGroup group, DataContext dataContext) {
		if (currentProject != null) {
			group.addAll(new RootMavenActionGroup().getActions(dataContext, currentProject));
		}
	}

	@Override
	protected boolean isEnabled() {
		return true;
	}

	protected JBPopupFactory.ActionSelectionAid getAidMethod() {
		return JBPopupFactory.ActionSelectionAid.SPEEDSEARCH;
	}

}
