package krasa.mavenhelper.action.debug;

import java.util.*;

import com.intellij.openapi.util.IconLoader;

/**
 * @author Vojtech Krasa
 */
public class Debug {

	public static final List<String> DEBUG_FORK_MODE = Arrays.asList("-DforkMode=never", "-DforkCount=0",
			"-DreuseForks=false");

	public static final javax.swing.Icon ICON = IconLoader.getIcon("/krasa/mavenhelper/action/debug/debug.png"); // 16x16


}
