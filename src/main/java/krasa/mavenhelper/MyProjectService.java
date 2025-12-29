package krasa.mavenhelper;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectChanges;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.project.MavenProjectsTree;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyProjectService {
	private List<MyEventListener> myEventListeners = new CopyOnWriteArrayList<>();

	public static MyProjectService getInstance(Project project) {
		return project.getService(MyProjectService.class);
	}

	public MyProjectService(Project project) {
		MavenProjectsManager.getInstance(project).addProjectsTreeListener(new MavenProjectsTree.Listener() {
			@Override
			public void profilesChanged() {

			}

			@Override
			public void projectsIgnoredStateChanged(@NotNull List<MavenProject> ignored, @NotNull List<MavenProject> unignored, boolean fromImport) {

			}

			@Override
			public void projectsUpdated(@NotNull List<? extends Pair<MavenProject, MavenProjectChanges>> updated, @NotNull List<MavenProject> deleted) {
				MavenProjectsTree.Listener.super.projectsUpdated(updated, deleted);
			}

			@Override
			public void projectResolved(@NotNull Pair<MavenProject, MavenProjectChanges> projectWithChanges) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						for (MyEventListener myEventListener : myEventListeners) {
							myEventListener.projectResolved(projectWithChanges);
						}
					}
				});
			}


			@Override
			public void pluginsResolved(@NotNull MavenProject project) {

			}

			@Override
			public void foldersResolved(@NotNull Pair<MavenProject, MavenProjectChanges> projectWithChanges) {

			}

			@Override
			public void artifactsDownloaded(@NotNull MavenProject project) {

			}
		});
	}

	public void register(MyEventListener myEventListener) {
		myEventListeners.add(myEventListener);
	}

	public void unregister(MyEventListener myEventListener) {
		if (myEventListener != null) {
			myEventListeners.remove(myEventListener);
		}
	}

	public interface MyEventListener {
		void projectResolved(@NotNull Pair<MavenProject, MavenProjectChanges> projectWithChanges);
	}
}
