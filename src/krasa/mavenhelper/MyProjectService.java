package krasa.mavenhelper;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectChanges;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.project.MavenProjectsTree;
import org.jetbrains.idea.maven.server.NativeMavenProjectHolder;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MyProjectService {
	private List<MyEventListener> myEventListeners = new CopyOnWriteArrayList<>();

	public static MyProjectService getInstance(Project project) {
		return ServiceManager.getService(project, MyProjectService.class);
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

			public void projectsUpdated(@NotNull List<Pair<MavenProject, MavenProjectChanges>> updated, @NotNull List<MavenProject> deleted) {
			}

			@Override
			public void projectResolved(@NotNull Pair<MavenProject, MavenProjectChanges> projectWithChanges, @Nullable NativeMavenProjectHolder nativeMavenProject) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						for (MyEventListener myEventListener : myEventListeners) {
							myEventListener.projectResolved(projectWithChanges, nativeMavenProject);
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
		void projectResolved(@NotNull Pair<MavenProject, MavenProjectChanges> projectWithChanges, @Nullable NativeMavenProjectHolder nativeMavenProject);
	}
}
