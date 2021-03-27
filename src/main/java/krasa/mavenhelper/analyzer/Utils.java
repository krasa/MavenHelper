package krasa.mavenhelper.analyzer;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.SimpleColoredComponent;
import org.jetbrains.idea.maven.model.MavenArtifactNode;
import org.jetbrains.idea.maven.model.MavenArtifactState;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class Utils {
	private static final Logger LOG = com.intellij.openapi.diagnostic.Logger.getInstance(Utils.class);

	//  myRelatedArtifact is a conflict winner, null means there was no conflict (probably) 
	public static boolean isVersionMatch(MavenArtifactNode mavenArtifactNode) {
		return mavenArtifactNode.getRelatedArtifact() != null && mavenArtifactNode.getRelatedArtifact().getVersion().equals(mavenArtifactNode.getArtifact().getVersion());
	}

	public static boolean isVersionMismatch(MavenArtifactNode mavenArtifactNode) {
		return mavenArtifactNode.getRelatedArtifact() != null && !mavenArtifactNode.getRelatedArtifact().getVersion().equals(mavenArtifactNode.getArtifact().getVersion());
	}

	static boolean isOmitted(MavenArtifactNode mavenArtifactNode) {
		return mavenArtifactNode.getState() == MavenArtifactState.CONFLICT || isVersionMismatch(mavenArtifactNode);
	}

	static boolean isConflictAlternativeMethod(MavenArtifactNode mavenArtifactNode) {
		return mavenArtifactNode.getState() != MavenArtifactState.ADDED && mavenArtifactNode.getState() != MavenArtifactState.EXCLUDED && !isVersionMatch(mavenArtifactNode);
	}


	public static void appendSize(SimpleColoredComponent r, long size, long totalSize) {
		r.append(formatThousands(totalSize) + " KB (" + formatThousands(size) + " KB) - ", GuiForm.SIZE_ATTRIBUTES);
	}

	static String formatThousands(long l) {
		DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
		DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();
		symbols.setGroupingSeparator(' ');
		formatter.setDecimalFormatSymbols(symbols);
		return formatter.format(l);
	}
}
