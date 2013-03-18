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
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAware;
import com.intellij.psi.JavaDirectoryService;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPackage;

public class TestFileAction extends AnAction implements DumbAware {

	public TestFileAction() {
		super("Test file");
	}

	private List<String> parse(String goal) {
		List<String> strings = new ArrayList<String>();
		String[] split = goal.split(" ");
		for (String s : split) {
			if (StringUtils.isNotBlank(s)) {
				strings.add(s);
			}
		}
		return strings;
	}

	public void actionPerformed(AnActionEvent e) {
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		if (mavenProject != null) {

			PsiPackage filePackage = getFilePackage(e.getDataContext());
			if (filePackage != null) {
				List<String> goals = new ArrayList<String>();
				goals.add("test");

				PsiFile psiFile = LangDataKeys.PSI_FILE.getData(e.getDataContext());
				goals.add("-Dtest=" + filePackage.getQualifiedName() + "." + psiFile.getName());
				goals.add("test-compile");
				goals.add("surefire:test");

				final DataContext context = e.getDataContext();
				MavenRunnerParameters params = new MavenRunnerParameters(true, mavenProject.getDirectory(), goals,
						MavenActionUtil.getProjectsManager(context).getExplicitProfiles());
				MavenRunConfigurationType.runConfiguration(MavenActionUtil.getProject(context), params, null);
			}
		}
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
		return MavenActionUtil.hasProject(e.getDataContext());
	}

	protected boolean isVisible(AnActionEvent e) {
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		return mavenProject != null;
	}
}
