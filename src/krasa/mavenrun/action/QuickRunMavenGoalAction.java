package krasa.mavenrun.action;

import com.intellij.ide.actions.QuickSwitchSchemeAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.*;
import com.intellij.openapi.ui.popup.JBPopupFactory;

public class QuickRunMavenGoalAction extends QuickSwitchSchemeAction implements DumbAware {

	private final Logger LOG = Logger.getInstance("#" + getClass().getCanonicalName());

	@Override
	protected void fillActions(final Project currentProject, DefaultActionGroup group, DataContext dataContext) {
		if (currentProject != null) {
			group.addAll(new MainMavenActionGroup().getActions(dataContext, currentProject));
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
