package krasa.mavenhelper.analyzer;

import com.intellij.codeHighlighting.BackgroundEditorHighlighter;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorLocation;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public final class UIFormEditor extends UserDataHolderBase implements /* Navigatable */FileEditor {
	private static final Logger log = LoggerFactory.getLogger(UIFormEditor.class);

	public static final FileEditorState MY_EDITOR_STATE = new FileEditorState() {
		@Override
		public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level) {
			return false;
		}
	};
	private final VirtualFile file;
	private GuiForm myEditor;

	public UIFormEditor(@NotNull Project project, final VirtualFile file) {
		this.file = file;
		final MavenProject mavenProject = MavenProjectsManager.getInstance(project).findProject(file);
		if (mavenProject != null) {
			myEditor = new GuiForm(project, file, mavenProject);
		} else {
			log.warn("MavenProject not found for file " + file.getPath(), new RuntimeException());
		}
	}

	@Override
	@NotNull
	public JComponent getComponent() {
		if (myEditor != null) {
			return myEditor.getRootComponent();
		}
		return new JLabel("Unexpected error. Try it again.");
	}

	@Override
	public void dispose() {
		if (myEditor != null) {
			myEditor.dispose();
		}
	}

	@Override
	public VirtualFile getFile() {
		return file;
	}

	@Override
	public JComponent getPreferredFocusedComponent() {
		if (myEditor != null) {
			return myEditor.getPreferredFocusedComponent();
		}
		return null;
	}

	@Override
	@NotNull
	public String getName() {
		return "Dependency Analyzer";
	}

	@Override
	public boolean isModified() {
		return false;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public void selectNotify() {
		if (myEditor != null) {
			myEditor.selectNotify();
		}
	}

	@Override
	public void deselectNotify() {
	}

	@Override
	public void addPropertyChangeListener(@NotNull final PropertyChangeListener listener) {
	}

	@Override
	public void removePropertyChangeListener(@NotNull final PropertyChangeListener listener) {
	}

	@Override
	public BackgroundEditorHighlighter getBackgroundHighlighter() {
		return null;
	}

	@Override
	public FileEditorLocation getCurrentLocation() {
		return null;
	}

	@Override
	@NotNull
	public FileEditorState getState(@NotNull final FileEditorStateLevel ignored) {
		return MY_EDITOR_STATE;
	}

	@Override
	public void setState(@NotNull final FileEditorState state) {
	}

	@Override
	public StructureViewBuilder getStructureViewBuilder() {
		return null;
	}
}
