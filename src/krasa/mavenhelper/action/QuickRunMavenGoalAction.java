package krasa.mavenhelper.action;

import com.intellij.ide.actions.QuickSwitchSchemeAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.ui.popup.PopupFactoryImpl;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.ui.popup.list.ListPopupModel;
import krasa.mavenhelper.ApplicationComponent;
import krasa.mavenhelper.action.debug.DebugGoalAction;
import krasa.mavenhelper.gui.GoalEditor;
import krasa.mavenhelper.icons.MyIcons;
import krasa.mavenhelper.model.ApplicationSettings;
import krasa.mavenhelper.model.Goal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public class QuickRunMavenGoalAction extends QuickSwitchSchemeAction implements DumbAware {

	private final Logger LOG = Logger.getInstance("#" + getClass().getCanonicalName());

	@Override
	protected void fillActions(final Project currentProject, DefaultActionGroup group, DataContext dataContext) {
		if (currentProject != null) {
			group.addAll(new MainMavenActionGroup() {

				@Override
				protected AnAction createGoalRunAction(Goal goal, final Icon icon, boolean plugin, MavenProjectInfo mavenProject) {
					return QuickRunMavenGoalAction.this.createGoalRunAction(goal, icon, plugin, mavenProject);
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

	@Override
	protected void showPopup(AnActionEvent e, ListPopup p) {
		final ListPopupImpl popup = (ListPopupImpl) p;
		registerActions(popup);
		super.showPopup(e, popup);
	}

	private void registerActions(final ListPopupImpl popup) {
		if (ApplicationSettings.get().isEnableDelete()) {
			popup.registerAction("delete", KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					JList list = popup.getList();
					int selectedIndex = list.getSelectedIndex();
					ListPopupModel model = (ListPopupModel) list.getModel();
					PopupFactoryImpl.ActionItem selectedItem = (PopupFactoryImpl.ActionItem) model.get(selectedIndex);

					if (selectedItem != null && selectedItem.getAction() instanceof MyActionGroup) {
						MyActionGroup action = (MyActionGroup) selectedItem.getAction();
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

	protected AnAction createGoalRunAction(Goal goal, Icon runIcon, boolean plugin, MavenProjectInfo mavenProject) {
		RunGoalAction goalRunAction = RunGoalAction.create(goal, runIcon, true, mavenProject);
		return new MyActionGroup(goalRunAction, plugin, goal, mavenProject);
	}

	private class MyActionGroup extends ActionGroup {
		private final RunGoalAction goalRunAction;
		private final boolean plugin;
		private final Goal goal;
		private final MavenProjectInfo mavenProject;

		public MyActionGroup(RunGoalAction goalRunAction, boolean plugin, Goal goal, MavenProjectInfo mavenProject) {
			super(goalRunAction.getTemplateText(), goalRunAction.getTemplatePresentation().getDescription(), goalRunAction.getTemplatePresentation().getIcon());
			this.goalRunAction = goalRunAction;
			this.plugin = plugin;
			this.goal = goal;
			this.mavenProject = mavenProject;
		}

		@Override
		public void actionPerformed(@NotNull AnActionEvent e) {
			goalRunAction.actionPerformed(e);
		}

		@Override
		public boolean canBePerformed(@NotNull DataContext context) {
			return true;
		}

		@Override
		public boolean isPopup() {
			return true;
		}

		@Override
		public boolean hideIfNoVisibleChildren() {
			return super.hideIfNoVisibleChildren();
		}

		@Override
		public boolean disableIfNoVisibleChildren() {
			return super.disableIfNoVisibleChildren();
		}

		@NotNull
		@Override
		public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
			if (plugin) {
				return new AnAction[]{
					debug(goal, mavenProject)
				};

			} else {
				return new AnAction[]{
					debug(goal, mavenProject), editAndRun(goal, mavenProject), delete(goal)
				};
			}
		}

		public Goal getGoal() {
			return goal;
		}

		private AnAction editAndRun(Goal goal, MavenProjectInfo mavenProject) {
			return new DumbAwareAction("Edit and Run") {

				@Override
				public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
					GoalEditor.editGoal("Edit and Run", ApplicationSettings.get(), goal);
					RunGoalAction.create(goal, MyIcons.RUN_MAVEN_ICON, true, mavenProject).actionPerformed(anActionEvent);
				}
			};
		}

		private AnAction debug(Goal goalRunAction, MavenProjectInfo mavenProject) {
			return DebugGoalAction.createDebug(goalRunAction, MyIcons.ICON, false, mavenProject);
		}

		private AnAction delete(Goal goal) {
			return new DumbAwareAction("Delete") {
				@Override
				public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
					ApplicationComponent.getInstance().getState().removeGoal(goal);
				}
			};
		}
		
	}
}
