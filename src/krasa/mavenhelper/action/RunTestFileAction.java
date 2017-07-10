package krasa.mavenhelper.action;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import krasa.mavenhelper.analyzer.ComparableVersion;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.shared.utils.io.MatchPatterns;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.BaseRunConfigurationAction;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.LocatableConfiguration;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import icons.MavenIcons;

public class RunTestFileAction extends DumbAwareAction {
	private final Logger LOG = Logger.getInstance("#" + getClass().getCanonicalName());

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
				List<String> goals = getGoals(e, (PsiJavaFile) psiFile,
						MavenActionUtil.getMavenProject(e.getDataContext()));

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

	protected List<String> getGoals(AnActionEvent e, PsiJavaFile psiFile, MavenProject mavenProject) {
		List<String> goals = new ArrayList<String>();
		boolean skipTests = isSkipTests(mavenProject);
		// so many possibilities...
		if (skipTests || isExcludedFromSurefire(psiFile, mavenProject)) {
			MavenPlugin failsafePlugin = mavenProject.findPlugin("org.apache.maven.plugins", "maven-failsafe-plugin");
			if (failsafePlugin != null) {
                addFailSafeParameters(e, psiFile, goals, failsafePlugin);
            } else {
                addSurefireParameters(e, psiFile, goals);
            }
			goals.add("verify");
		} else {
			addSurefireParameters(e, psiFile, goals);
			goals.add("test-compile");
			goals.add("surefire:test");
		}

		return goals;
	}

	private void addSurefireParameters(AnActionEvent e, PsiJavaFile psiFile, List<String> goals) {
		goals.add("-Dtest=" + getTestArgument(e, psiFile));
	}

	private void addFailSafeParameters(AnActionEvent e, PsiJavaFile psiFile, List<String> goals, MavenPlugin mavenProjectPlugin) {
		ComparableVersion version = new ComparableVersion(mavenProjectPlugin.getVersion());
		ComparableVersion minimumForMethodTest = new ComparableVersion("2.7.3");
		if (minimumForMethodTest.compareTo(version) == 1) {
            goals.add("-Dit.test=" + getTestArgumentWithoutMethod(e, psiFile));
        } else {
            goals.add("-Dit.test=" + getTestArgument(e, psiFile));
        }
	}

	private boolean isExcludedFromSurefire(PsiJavaFile psiFile, MavenProject mavenProject) {
		boolean excluded = false;
		try {
			Element pluginConfiguration = mavenProject.getPluginConfiguration("org.apache.maven.plugins",
					"maven-surefire-plugin");
			excluded = false;
			String fullName = null;
			if (pluginConfiguration != null) {
				Element excludes = pluginConfiguration.getChild("excludes");
				if (excludes != null) {
					List<Element> exclude = excludes.getChildren("exclude");
					for (Element element : exclude) {
						if (fullName == null) {
							fullName = getPsiFilePath(psiFile);
						}
						excluded = matchClassRegexPatter(fullName, element.getText());
						if (excluded) {
							break;
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.warn(e);
		}
		return excluded;
	}

	protected static boolean matchClassRegexPatter(String testClassFile, String classPattern) {
		return MatchPatterns.from(classPattern).matches(testClassFile, true);
	}

	@NotNull
	private String getPsiFilePath(PsiJavaFile psiFile) {
		String packageName = psiFile.getPackageName();
		String fullName;
		if (packageName.isEmpty()) {
			fullName = psiFile.getName();
		} else {
			fullName = packageName.replace(".", "/") + "/" + psiFile.getVirtualFile().getName();
		}
		return fullName;
	}

	private boolean isSkipTests(MavenProject mavenProject) {
		Element pluginConfiguration = mavenProject.getPluginConfiguration("org.apache.maven.plugins",
				"maven-surefire-plugin");
		boolean skipTests = false;
		if (pluginConfiguration != null) {
			Element skip;
			if ((skip = pluginConfiguration.getChild("skip")) != null) {
				skipTests = Boolean.parseBoolean(skip.getText());
			} else if ((skip = pluginConfiguration.getChild("skipTests")) != null) {
				skipTests = Boolean.parseBoolean(skip.getText());
			}
		}
		return skipTests;
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

	protected String getTestArgumentWithoutMethod(AnActionEvent e, PsiJavaFile psiFile) {
		return StringUtils.substringBefore(getTestArgument(e, psiFile), "#");
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
