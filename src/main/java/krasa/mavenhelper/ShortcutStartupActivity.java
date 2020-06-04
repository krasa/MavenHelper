package krasa.mavenhelper;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class ShortcutStartupActivity implements StartupActivity {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(ShortcutStartupActivity.class);

	private volatile boolean registered = false;

	@Override
	public void runActivity(@NotNull Project project) {
		if (!registered) {
			ApplicationService.getInstance().initShortcuts();
			registered = true;
		}
	}

}
