package krasa.mavenhelper.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;

import com.intellij.ide.actions.QuickSwitchSchemeAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.ui.popup.PopupFactoryImpl;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.ui.popup.list.ListPopupModel;

import krasa.mavenhelper.ApplicationComponent;

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

	@Override
	protected void showPopup(AnActionEvent e, ListPopup p) {
		final ListPopupImpl popup = (ListPopupImpl) p;
		registerActions(popup);
		super.showPopup(e, popup);
	}

	private void registerActions(final ListPopupImpl popup) {
		popup.registerAction("delete", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				JList list = popup.getList();
				int selectedIndex = list.getSelectedIndex();
				ListPopupModel model = (ListPopupModel) list.getModel();
				PopupFactoryImpl.ActionItem selectedItem = (PopupFactoryImpl.ActionItem) model.get(selectedIndex);

				if (selectedItem != null && selectedItem.getAction() instanceof RunGoalAction) {
					RunGoalAction action = (RunGoalAction) selectedItem.getAction();
					boolean deleted = ApplicationComponent.getInstance().getState().removeGoal(action.getGoal());

					if (deleted) {
						model.deleteItem(selectedItem);
						if (selectedIndex == list.getModel().getSize()) { // is last
							list.setSelectedIndex(selectedIndex - 1);
						} else {
							list.setSelectedIndex(selectedIndex);
						}
					}
				}
			}
		});
	}
}
