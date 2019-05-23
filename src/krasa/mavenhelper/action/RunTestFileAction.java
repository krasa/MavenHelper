package krasa.mavenhelper.action;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.BaseRunConfigurationAction;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.LocatableConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import krasa.mavenhelper.analyzer.ComparableVersion;
import krasa.mavenhelper.icons.MyIcons;
import org.apache.maven.shared.utils.io.MatchPatterns;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunConfigurationType;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.model.MavenPlugin;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class RunTestFileAction extends DumbAwareAction {
	private final Logger LOG = Logger.getInstance("#" + getClass().getCanonicalName());

	public RunTestFileAction() {
		super("Test file", "Run current File with Maven", MyIcons.MAVEN_LOGO);
	}

	public RunTestFileAction(String text, @Nullable String description, @Nullable Icon icon) {
		super(text, description, icon);
	}

	public void actionPerformed(AnActionEvent e) {
		MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
		if (mavenProject != null) {

			PsiFile psiFile = LangDataKeys.PSI_FILE.getData(e.getDataContext());
			if (psiFile instanceof PsiClassOwner) {
				List<String> goals = getGoals(e, (PsiClassOwner) psiFile,
						MavenActionUtil.getMavenProject(e.getDataContext()));

				final DataContext context = e.getDataContext();
				MavenRunnerParameters params = new MavenRunnerParameters(true, mavenProject.getDirectory(), null, goals,
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

	protected List<String> getGoals(AnActionEvent e, PsiClassOwner psiFile, MavenProject mavenProject) {
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

	private void addSurefireParameters(AnActionEvent e, PsiClassOwner psiFile, List<String> goals) {
		goals.add("-Dtest=" + Utils.getTestArgument(psiFile, ConfigurationContext.getFromContext(e.getDataContext())));
	}

	private void addFailSafeParameters(AnActionEvent e, PsiClassOwner psiFile, List<String> goals, MavenPlugin mavenProjectPlugin) {
		ComparableVersion version = new ComparableVersion(mavenProjectPlugin.getVersion());
		ComparableVersion minimumForMethodTest = new ComparableVersion("2.7.3");
		if (minimumForMethodTest.compareTo(version) == 1) {
			goals.add("-Dit.test=" + Utils.getTestArgumentWithoutMethod(e, psiFile));
        } else {
			goals.add("-Dit.test=" + Utils.getTestArgument(psiFile, ConfigurationContext.getFromContext(e.getDataContext())));
        }
	}

	private boolean isExcludedFromSurefire(PsiClassOwner psiFile, MavenProject mavenProject) {
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
	private String getPsiFilePath(PsiClassOwner psiFile) {
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
			RunConfiguration runConfiguration = configuration.getConfiguration();
			if (runConfiguration instanceof LocatableConfiguration) {
				String s = BaseRunConfigurationAction.suggestRunActionName((LocatableConfiguration) runConfiguration);
				p.setText(getText(s));
			} else {
				p.setText(getText(ProgramRunnerUtil.shortenName(configuration.getName(), 0)));
			}
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
