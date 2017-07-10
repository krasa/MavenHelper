package krasa.mavenhelper.action;

import com.intellij.execution.Executor;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * @author Vojtech Krasa
 */
public class RunConfigurationAction extends AnAction {

	protected final Executor myExecutor;
	protected final boolean myEnabled;
	protected final Project myProject;
	protected final RunnerAndConfigurationSettings mySettings;

	public RunConfigurationAction(Executor executor,
								  boolean enabled,
								  Project project,
								  RunnerAndConfigurationSettings settings) {
		super(settings.getName(), null, executor.getIcon());
		myExecutor = executor;
		myEnabled = enabled;
		myProject = project;
		mySettings = settings;
	}

	@Override
	public void actionPerformed(AnActionEvent event) {
		if (!myEnabled) return;

		ProgramRunnerUtil.executeConfiguration(myProject, mySettings, myExecutor);
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		e.getPresentation().setEnabled(myEnabled);
	}
}
