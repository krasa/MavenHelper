package krasa.mavenhelper.analyzer;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.model.MavenConstants;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * @author Vojtech Krasa
 */
public class MyFileEditorProvider implements FileEditorProvider, DumbAware {
	private static final Logger LOG = Logger.getInstance("#krasa.mavenrun.analyzer.MyFileEditorProvider");

	public boolean accept(@NotNull final Project project, @NotNull final VirtualFile file) {
		return isPomFile(project, file);
	}

	private boolean isPomFile(@NotNull final Project project, @NotNull final VirtualFile file) {
		final String path = file.getPath();
		if (!path.endsWith("/" + MavenConstants.POM_XML))
			return false;
		MavenProjectsManager instance = MavenProjectsManager.getInstance(project);
		final MavenProject mavenProject = instance == null ? null : instance.findProject(file);
		if (mavenProject != null) {
			return mavenProject.getPath().equals(path);
		}
		return false;
	}

	@NotNull
	public FileEditor createEditor(@NotNull final Project project, @NotNull final VirtualFile file) {
		LOG.assertTrue(accept(project, file));
		return new UIFormEditor(project, file);
	}

	public void disposeEditor(@NotNull final FileEditor editor) {
		Disposer.dispose(editor);
	}

	@NotNull
	public FileEditorState readState(@NotNull final Element element, @NotNull final Project project,
			@NotNull final VirtualFile file) {
		return UIFormEditor.MY_EDITOR_STATE;
	}

	public void writeState(@NotNull final FileEditorState state, @NotNull final Project project,
			@NotNull final Element element) {
	}

	@NotNull
	public String getEditorTypeId() {
		return "MavenHelperPluginDependencyAnalyzer";
	}

	@NotNull
	public FileEditorPolicy getPolicy() {
		return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
	}

}
