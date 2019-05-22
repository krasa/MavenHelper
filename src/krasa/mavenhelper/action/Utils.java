package krasa.mavenhelper.action;

import java.io.File;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.model.MavenConstants;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;

import krasa.mavenhelper.ApplicationComponent;
import krasa.mavenhelper.model.ApplicationSettings;

public class Utils {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(Utils.class);

	static VirtualFile getPomDir(AnActionEvent e) {
		VirtualFile fileByUrl = null;
		String pomDir = getPomDirAsString(e);
		if (pomDir != null) {
			fileByUrl = VirtualFileManager.getInstance().findFileByUrl("file://" + pomDir);

		}
		return fileByUrl;
	}

	@Nullable
	static String getPomDirAsString(AnActionEvent e) {
		ApplicationComponent instance = ApplicationComponent.getInstance();
		ApplicationSettings state = instance.getState();

		String pomDir = null;
		if (state.isUseIgnoredPoms()) {
			VirtualFile data = CommonDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
			if (data != null) {
				File focusedFile = new File(data.getPath());
				pomDir = getNearbyPOMDir(focusedFile);
			}
		} else {
			MavenProject mavenProject = MavenActionUtil.getMavenProject(e.getDataContext());
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

}
