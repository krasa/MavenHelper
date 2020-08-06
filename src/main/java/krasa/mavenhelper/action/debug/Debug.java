package krasa.mavenhelper.action.debug;

import java.util.Arrays;
import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class Debug {

	public static final List<String> DEBUG_FORK_MODE = Arrays.asList("-DforkCount=0", "-DreuseForks=false");

	public static final List<String> DEBUG_FORK_MODE_LEGACY = Arrays.asList("-DforkMode=never", "-DforkCount=0",
			"-DreuseForks=false");


}
