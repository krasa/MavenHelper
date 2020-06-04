package krasa.mavenhelper.action;

import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiClassOwner;
import com.intellij.psi.PsiFile;
import krasa.mavenhelper.model.ApplicationSettings;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenConstants;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import java.io.File;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class Utils {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(Utils.class);
	public static final String NOT_RESOLVED = "NOT_RESOLVED";

	static VirtualFile getPomDir(AnActionEvent e) {
		VirtualFile fileByUrl = null;
		String pomDir = getPomDirAsString(e.getDataContext(), null);
		if (pomDir != null) {
			fileByUrl = VirtualFileManager.getInstance().findFileByUrl("file://" + pomDir);

		}
		return fileByUrl;
	}

	@Nullable
	static String getPomDirAsString(DataContext dataContext, @Nullable MavenProjectInfo mavenProjectInfo) {
		ApplicationSettings state = ApplicationSettings.get();


		String pomDir = null;
		if (mavenProjectInfo != null && mavenProjectInfo.root) {
			return mavenProjectInfo.mavenProject.getDirectory();
		}
		if (state.isUseIgnoredPoms()) {
			VirtualFile data = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
			if (data != null) {
				File focusedFile = new File(data.getPath());
				pomDir = getNearbyPOMDir(focusedFile);
			}
		} else {
			MavenProject mavenProject = MavenActionUtil.getMavenProject(dataContext);
			if (mavenProject != null) {
				pomDir = mavenProject.getDirectory();
			}
		}
		return pomDir;
	}

	private static String getNearbyPOMDir(File focusFile) {
		if (focusFile == null || !focusFile.exists())
			return null;
		File dir = (focusFile.isDirectory() ? focusFile : focusFile.getParentFile());
		return isPomDir(dir) ? dir.getAbsolutePath() : getNearbyPOMDir(dir == null ? null : dir.getParentFile());
	}

	private static boolean isPomDir(File file) {
		if (file == null || !file.exists() || !file.isDirectory())
			return false;
		return new File(file.getAbsolutePath() + File.separator + MavenConstants.POM_XML).exists();
	}

	public static String getTestArgument(@Nullable PsiFile psiFile, ConfigurationContext context) {
		RunnerAndConfigurationSettings configuration = context.getConfiguration();
		String classAndMethod = null;
		if (configuration != null) {
			classAndMethod = configuration.getName().replace(".", "#");
		}

		String result;
		String packageName = null;
		if (psiFile instanceof PsiClassOwner) {
			packageName = ((PsiClassOwner) psiFile).getPackageName();
		}

		if (isNotBlank(packageName) && isNotBlank(classAndMethod)) {
			result = packageName + "." + classAndMethod;
		} else if (isNotBlank(classAndMethod)) {
			result = classAndMethod;
		} else {
			result = NOT_RESOLVED;
		}
		return result;
	}

	public static String getQualifiedName(@Nullable PsiFile psiFile) {
		String result = NOT_RESOLVED;

		if (psiFile != null) {
			String packageName = null;
			if (psiFile instanceof PsiClassOwner) {
				packageName = ((PsiClassOwner) psiFile).getPackageName();
			}
			String name = psiFile.getName();
			name = StringUtils.substringBefore(name, ".");

			if (isNotBlank(packageName) && isNotBlank(name)) {
				result = packageName + "." + name;
			} else if (isNotBlank(name)) {
				result = name;
			}
		}

		return result;
	}

	public static String getTestArgumentWithoutMethod(AnActionEvent e, PsiFile psiFile) {
		return StringUtils.substringBefore(getTestArgument(psiFile, ConfigurationContext.getFromContext(e.getDataContext())), "#");
	}


	public static String limitLength(String text) {
		if (text == null) {
			return null;
		}

		int max = 200;
		if (text.length() > max) text = text.substring(0, max - 3) + "...";
		return text;
	}


}
