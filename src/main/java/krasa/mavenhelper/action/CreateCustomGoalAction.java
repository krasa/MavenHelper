package krasa.mavenhelper.action;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import krasa.mavenhelper.MavenHelperApplicationService;
import krasa.mavenhelper.gui.GoalEditor;
import krasa.mavenhelper.icons.MyIcons;
import krasa.mavenhelper.model.ApplicationSettings;
import krasa.mavenhelper.model.Goal;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

public class CreateCustomGoalAction extends AnAction implements DumbAware {
	@Nullable
	protected MavenProjectInfo mavenProject;
	private boolean runGoal = true;

	public CreateCustomGoalAction() {
	}

	public CreateCustomGoalAction(@Nullable String text, @NotNull MavenProjectInfo mavenProject) {
		super(text);
		this.mavenProject = mavenProject;
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		MavenProjectInfo mavenProjectInfo = MavenProjectInfo.get(mavenProject, e);

		MavenHelperApplicationService instance = MavenHelperApplicationService.getInstance();
		ApplicationSettings state = instance.getState();

		DataContext context = e.getDataContext();
		Project project = MavenActionUtil.getProject(context);
		String pomDir = Utils.getPomDirAsString(context, mavenProjectInfo);
		MavenProjectsManager projectsManager = MavenActionUtil.getProjectsManager(context);
		PsiFile data = LangDataKeys.PSI_FILE.getData(e.getDataContext());
		ConfigurationContext configurationContext = ConfigurationContext.getFromContext(e.getDataContext());


		GoalEditor editor = new GoalEditor("Create and Run", "", state, true, e.getProject(), e.getDataContext());
		if (editor.showAndGet()) {
			String s = editor.getCmd();
			if (StringUtils.isNotBlank(s)) {

				Goal goal = new Goal(s);

				PropertiesComponent.getInstance().setValue(GoalEditor.SAVE, editor.isPersist(), true);
				if (editor.isPersist()) {
					state.getGoals().add(goal);
					instance.registerAction(goal, getRunGoalAction(goal, null));
				}

				if (runGoal) {
					getRunGoalAction(goal, mavenProjectInfo).actionPerformed(project, pomDir, projectsManager, data, configurationContext, mavenProjectInfo);
				}
			}
		}

	}

	protected RunGoalAction getRunGoalAction(Goal goal, MavenProjectInfo mavenProject1) {
		return RunGoalAction.create(goal, MyIcons.PLUGIN_GOAL, false, mavenProject1);
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		Presentation p = e.getPresentation();
		p.setEnabled(isAvailable(e) && isVisible(e));
//		p.setVisible(isVisible(e));
	}


	protected boolean isAvailable(AnActionEvent e) {
		return MavenActionUtil.hasProject(e.getDataContext());
	}

	protected boolean isVisible(AnActionEvent e) {
		return MavenProjectInfo.get(mavenProject, e).mavenProject != null;
	}

}
