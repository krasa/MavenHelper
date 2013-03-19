package krasa.mavenrun.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nullable;
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
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPackage;
import icons.MavenIcons;

public class TestFileAction extends AnAction implements DumbAware {

	public TestFileAction() {
		super("Test file", "Run current File with maven", MavenIcons.MavenLogo);
	}

	public void actionPerformed(AnActionEvent e) {
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		if (mavenProject != null) {

			PsiPackage filePackage = getFilePackage(e.getDataContext());
			if (filePackage != null) {
				List<String> goals = new ArrayList<String>();
				// goals.add("test");

				PsiFile psiFile = LangDataKeys.PSI_FILE.getData(e.getDataContext());
				goals.add("-Dtest=" + wholePackageAndName(filePackage, psiFile));
				goals.add("test-compile");
				goals.add("surefire:test");

				final DataContext context = e.getDataContext();
				MavenRunnerParameters params = new MavenRunnerParameters(true, mavenProject.getDirectory(), goals,
						MavenActionUtil.getProjectsManager(context).getExplicitProfiles());
				MavenRunConfigurationType.runConfiguration(MavenActionUtil.getProject(context), params, null);
			} else {
				Messages.showWarningDialog(e.getProject(), "Cannot run for current file", "Maven Test File");
			}
		}
	}

	private String wholePackageAndName(PsiPackage filePackage, PsiFile psiFile) {
		String qualifiedName = filePackage.getQualifiedName();
		String name = psiFile.getName();
		name = name.substring(0, name.indexOf("."));
		if (StringUtils.isNotBlank(qualifiedName)) {
			return qualifiedName + "." + name;
		}
		return name;
	}

	@Nullable
	static PsiPackage getFilePackage(DataContext dataContext) {
		PsiFile psiFile = LangDataKeys.PSI_FILE.getData(dataContext);
		if (psiFile == null)
			return null;
		PsiDirectory containingDirectory = psiFile.getContainingDirectory();
		if (containingDirectory == null || !containingDirectory.isValid())
			return null;
		return JavaDirectoryService.getInstance().getPackage(containingDirectory);
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
