package krasa.mavenhelper;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class ShortcutStartupActivity implements StartupActivity, DumbAware {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(ShortcutStartupActivity.class);

	private static final AtomicBoolean registered = new AtomicBoolean();

	@Override
	public void runActivity(@NotNull Project project) {
		if (registered.compareAndSet(false, true)) {
			MavenHelperApplicationService.getInstance().initShortcuts();
		}
	}

}
