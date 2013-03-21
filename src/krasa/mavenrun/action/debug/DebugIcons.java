package krasa.mavenrun.action.debug;

import com.intellij.openapi.util.IconLoader;

/**
 * @author Vojtech Krasa
 */
public class DebugIcons {
	private static javax.swing.Icon load(String path) {
		return IconLoader.getIcon(path, DebugIcons.class);
	}

	public static final javax.swing.Icon PluginGoal = load("debug.png"); // 16x16

}
