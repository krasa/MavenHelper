package krasa.mavenrun.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import icons.MavenIcons;

public class RunTestFileAction extends AnAction implements DumbAware {

	public RunTestFileAction() {
		super("Test file", "Run current File with maven", MavenIcons.MavenLogo);
	}

	public void actionPerformed(AnActionEvent e) {
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		if (mavenProject != null) {

			PsiFile psiFile = LangDataKeys.PSI_FILE.getData(e.getDataContext());
			if (psiFile instanceof PsiJavaFile) {
				List<String> goals = getGoals((PsiJavaFile) psiFile);

				final DataContext context = e.getDataContext();
				MavenRunnerParameters params = new MavenRunnerParameters(true, mavenProject.getDirectory(), goals,
						MavenActionUtil.getProjectsManager(context).getExplicitProfiles());
				MavenRunConfigurationType.runConfiguration(MavenActionUtil.getProject(context), params, null);
			} else {
				Messages.showWarningDialog(e.getProject(), "Cannot run for current file", "Maven Test File");
			}
		}
	}

	private List<String> getGoals(PsiJavaFile psiFile) {
		List<String> goals = new ArrayList<String>();
		// goals.add("test");
		goals.add("-Dtest=" + wholePackageAndName(psiFile));
		goals.add("test-compile");
		goals.add("surefire:test");
		return goals;
	}

	private String wholePackageAndName(PsiJavaFile psiFile) {
		String packageName = psiFile.getPackageName();
		if (StringUtils.isNotBlank(packageName)) {
			String psiFileName = psiFile.getName();
			return packageName + "." + psiFileName.substring(0, psiFileName.indexOf("."));
		} else {
			return psiFile.getName();
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
