package krasa.mavenhelper.action.debug;

import java.util.*;

import com.intellij.openapi.util.IconLoader;

/**
 * @author Vojtech Krasa
 */
public class Debug {

	public static final List<String> DEBUG_FORK_MODE = Arrays.asList("-DforkMode=never", "-DforkCount=0",
			"-DreuseForks=false");

	public static final javax.swing.Icon ICON = load("debug.png"); // 16x16

	private static javax.swing.Icon load(String path) {
		return IconLoader.getIcon(path, Debug.class);
	}

}
