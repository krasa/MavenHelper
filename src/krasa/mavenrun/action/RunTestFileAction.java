package krasa.mavenrun.action;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.BaseRunConfigurationAction;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.LocatableConfiguration;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import icons.MavenIcons;

public class RunTestFileAction extends DumbAwareAction {

	public RunTestFileAction() {
		super("Test file", "Run current File with Maven", MavenIcons.MavenLogo);
	}

	public RunTestFileAction(String text, @Nullable String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	public void actionPerformed(AnActionEvent e) {
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		if (mavenProject != null) {

			PsiFile psiFile = LangDataKeys.PSI_FILE.getData(e.getDataContext());
			if (psiFile instanceof PsiJavaFile) {
				List<String> goals = getGoals(e, (PsiJavaFile) psiFile);

				final DataContext context = e.getDataContext();
				MavenRunnerParameters params = new MavenRunnerParameters(true, mavenProject.getDirectory(), goals,
						MavenActionUtil.getProjectsManager(context).getExplicitProfiles());
				run(context, params);
			} else {
				Messages.showWarningDialog(e.getProject(), "Cannot run for current file", "Maven Test File");
			}
		}
	}

	protected void run(DataContext context, MavenRunnerParameters params) {
		MavenRunConfigurationType.runConfiguration(MavenActionUtil.getProject(context), params, null);
	}

	protected List<String> getGoals(AnActionEvent e, PsiJavaFile psiFile) {
		List<String> goals = new ArrayList<String>();
		// goals.add("test");
		goals.add("-Dtest=" + getTestArgument(e, psiFile));
		goals.add("test-compile");
		goals.add("surefire:test");
		return goals;
	}

	protected String getTestArgument(AnActionEvent e, PsiJavaFile psiFile) {
		final ConfigurationContext context = ConfigurationContext.getFromContext(e.getDataContext());
		RunnerAndConfigurationSettings configuration = context.getConfiguration();
		String classAndMethod = configuration.getName().replace(".", "#");

		String result;
		String packageName = psiFile.getPackageName();
		if (StringUtils.isNotBlank(packageName)) {
			result = packageName + "." + classAndMethod;
		} else {
			result = classAndMethod;
		}
		return result;
	}

	@Override
	public void update(AnActionEvent e) {
		super.update(e);
        if (DumbService.isDumb(getEventProject(e))) {
            Presentation p = e.getPresentation();
            p.setVisible(false);
            return;
        }
        
        final ConfigurationContext context = ConfigurationContext.getFromContext(e.getDataContext());
		RunnerAndConfigurationSettings configuration = context.getConfiguration();

		boolean isTest = configuration != null;
		boolean available = isAvailable(e);
		boolean visible = isVisible(e);

		Presentation p = e.getPresentation();
		p.setEnabled(isTest && available);
		p.setVisible(isTest && visible);
		if (isTest && available && visible) {
			String s = BaseRunConfigurationAction.suggestRunActionName((LocatableConfiguration) configuration.getConfiguration());
			p.setText(getText(s));
		}
	}

	protected String getText(String s) {
		return "Test " + s;
	}

	protected boolean isAvailable(AnActionEvent e) {
		boolean isFile = false;
		VirtualFile data = PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
		if (data != null) {
			isFile = !data.isDirectory();
		}

		return isFile && MavenActionUtil.hasProject(e.getDataContext());
	}

	protected boolean isVisible(AnActionEvent e) {
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		return mavenProject != null;
	}
}
