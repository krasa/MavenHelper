package krasa.mavenrun.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.idea.maven.utils.actions.MavenActionUtil;

public class MavenActionGroup extends DefaultActionGroup implements DumbAware {
	public MavenActionGroup() {
	}

	public MavenActionGroup(AnAction... actions) {
		super(actions);
	}

	public MavenActionGroup(String shortName, boolean popup) {
		super(shortName, popup);
	}

	@Override
  public void update(AnActionEvent e) {
    super.update(e);
    boolean available = isAvailable(e);
    e.getPresentation().setEnabled(available);
    e.getPresentation().setVisible(available);
  }

  protected boolean isAvailable(AnActionEvent e) {
    return MavenActionUtil.hasProject(e.getDataContext())
           && !MavenActionUtil.getMavenProjects(e.getDataContext()).isEmpty();
  }
}
