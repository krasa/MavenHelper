package krasa.mavenhelper.action;

import com.intellij.execution.Location;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.CreateAction;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.QuickSwitchSchemeAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.popup.PopupFactoryImpl;
import com.intellij.ui.popup.list.ListPopupImpl;
import com.intellij.ui.popup.list.ListPopupModel;
import krasa.mavenhelper.ApplicationService;
import krasa.mavenhelper.action.debug.DebugConfigurationAction;
import krasa.mavenhelper.action.debug.DebugGoalAction;
import krasa.mavenhelper.action.debug.DebugTestFileAction;
import krasa.mavenhelper.gui.GoalEditor;
import krasa.mavenhelper.icons.MyIcons;
import krasa.mavenhelper.model.ApplicationSettings;
import krasa.mavenhelper.model.Goal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenGoalLocation;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class QuickRunMavenGoalAction extends QuickSwitchSchemeAction implements DumbAware {

	private final Logger LOG = Logger.getInstance("#" + getClass().getCanonicalName());

	@Override
	protected void fillActions(final Project currentProject, DefaultActionGroup group, DataContext dataContext) {
		if (currentProject != null) {
			group.addAll(new MainMavenActionGroup() {

				@Override
				protected void addTestFile(List<AnAction> result) {
					QuickRunMavenGoalAction.this.addTestFile(result);
				}

				@Override
				protected AnAction getRunConfigurationAction(Project project, RunnerAndConfigurationSettings cfg) {
					return QuickRunMavenGoalAction.this.getRunConfigurationAction(project, cfg);
				}

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
						boolean deleted = ApplicationService.getInstance().getState().removeGoal(action.getGoal());

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

	void addTestFile(List<AnAction> result) {
		RunTestFileAction action = new RunTestFileAction();
		result.add(new ActionGroup(action.getTemplatePresentation().getText(), action.getTemplatePresentation().getDescription(), action.getTemplatePresentation().getIcon()) {
			@NotNull
			@Override
			public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
				return new AnAction[]{new DebugTestFileAction() {
					@Override
					protected String getText(String s) {
						return "Debug";
					}
				}};
			}

			@Override
			public void update(@NotNull AnActionEvent e) {
				action.update(e);
			}

			@Override
			public void actionPerformed(@NotNull AnActionEvent e) {
				action.actionPerformed(e);
			}

			@Override
			public boolean hideIfNoVisibleChildren() {
				return true;
			}

			@Override
			public boolean canBePerformed(@NotNull DataContext context) {
				return true;
			}

			@Override
			public boolean isPopup() {
				return true;
			}

		});

	}


	protected AnAction getRunConfigurationAction(Project project, RunnerAndConfigurationSettings cfg) {
		RunConfigurationAction action = new RunConfigurationAction(DefaultRunExecutor.getRunExecutorInstance(), true, project, cfg);

		return new ActionGroup(action.getTemplatePresentation().getText(), action.getTemplatePresentation().getDescription(), action.getTemplatePresentation().getIcon()) {
			@NotNull
			@Override
			public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
				DebugConfigurationAction debugConfigurationAction = new DebugConfigurationAction(DefaultDebugExecutor.getDebugExecutorInstance(), true, project, cfg);
				debugConfigurationAction.getTemplatePresentation().setText("Debug");
				return new AnAction[]{debugConfigurationAction};
			}

			@Override
			public void actionPerformed(@NotNull AnActionEvent e) {
				action.actionPerformed(e);
			}

			@Override
			public boolean canBePerformed(@NotNull DataContext context) {
				return true;
			}

			@Override
			public boolean isPopup() {
				return true;
			}

		};
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
			super(goalRunAction.getTemplatePresentation().getText(), goalRunAction.getTemplatePresentation().getDescription(), goalRunAction.getTemplatePresentation().getIcon());
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

		@NotNull
		@Override
		public AnAction[] getChildren(@Nullable AnActionEvent anActionEvent) {
			if (plugin) {
				return new AnAction[]{
					debug(goal, mavenProject)
				};

			} else {
				return new AnAction[]{
					debug(goal, mavenProject), editAndRun(goal, mavenProject), delete(goal), new MyCreateAction(goal, mavenProject)
				};
			}
		}

		public Goal getGoal() {
			return goal;
		}

		private AnAction editAndRun(Goal goal, MavenProjectInfo mavenProject) {
			return new DumbAwareAction("Edit and Run", null, AllIcons.Actions.Edit) {

				@Override
				public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
					Goal edit_and_run = GoalEditor.editGoal("Edit and Run", ApplicationSettings.get(), goal);
					if (edit_and_run != null) {
						RunGoalAction.create(goal, MyIcons.RUN_MAVEN_ICON, true, mavenProject).actionPerformed(anActionEvent);
					}
				}
			};
		}

		private AnAction debug(Goal goalRunAction, MavenProjectInfo mavenProject) {
			return DebugGoalAction.createDebug(goalRunAction, "Debug", MyIcons.ICON, mavenProject);
		}

		private AnAction delete(Goal goal) {
			return new DumbAwareAction("Delete", null, AllIcons.General.Remove) {
				@Override
				public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
					ApplicationService.getInstance().getState().removeGoal(goal);
				}
			};
		}

		private class MyCreateAction extends DumbAwareAction {
			private final Goal goal;
			private final MavenProjectInfo mavenProject;
			private CreateAction createAction;

			public MyCreateAction(Goal goal, MavenProjectInfo mavenProject) {
				super("Create Run Configuration");
				this.goal = goal;
				this.mavenProject = mavenProject;
				createAction = new CreateAction();
			}

			@Override
			public void actionPerformed(@NotNull AnActionEvent e) {
				createAction.actionPerformed(getAnActionEvent(e));
			}

			@Override
			public void update(@NotNull AnActionEvent e) {
				createAction.update(getAnActionEvent(e));
			}

			@NotNull
			private AnActionEvent getAnActionEvent(@NotNull AnActionEvent e) {
				DataContext dataContext = new DataContext() {
					@Nullable
					@Override
					public Object getData(@NotNull String s) {
						if (Location.DATA_KEY.is(s)) {
							PsiFile data = LangDataKeys.PSI_FILE.getData(e.getDataContext());
							ConfigurationContext fromContext = ConfigurationContext.getFromContext(e.getDataContext());
							PsiFile psiFile = PsiManager.getInstance(e.getProject()).findFile(mavenProject.mavenProject.getFile());
							return new MavenGoalLocation(e.getProject(), psiFile, goal.parse(data, fromContext));
						}
						return e.getDataContext().getData(s);
					}
				};

				return AnActionEvent.createFromDataContext("MavenRunHelper.CreateRunConfiguration", e.getPresentation(), dataContext);
			}
		}
	}
}
