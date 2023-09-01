package krasa.mavenhelper.action;

import com.intellij.execution.Executor;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * @author Vojtech Krasa
 */
public class RunConfigurationAction extends DumbAwareAction {

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
		ProgramRunnerUtils.executeConfiguration(myProject, myExecutor, mySettings);
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
		e.getPresentation().setEnabled(myEnabled);
	}

	@Override
	public @NotNull ActionUpdateThread getActionUpdateThread() {
		return ActionUpdateThread.BGT;
	}

}
