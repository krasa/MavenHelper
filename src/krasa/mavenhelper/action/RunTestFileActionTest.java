package krasa.mavenhelper.action;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RunTestFileActionTest {

	@Test
	public void testMatchClassRegexPatter() throws Exception {
		assertTrue(RunTestFileAction.matchClassRegexPatter("com/foo/FailsafeSampleIT.java", "**/*IT.java"));
		assertFalse(RunTestFileAction.matchClassRegexPatter("com/foo/FailsafeSampleTest.java", "**/*IT.java"));
	}
}