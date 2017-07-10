package krasa.mavenhelper.analyzer;

import static com.intellij.ui.ColorUtil.darker;
import static com.intellij.ui.ColorUtil.softer;

import java.awt.*;

import com.intellij.ui.JBColor;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.UIUtil;

public class MyHighlightingTree extends Tree {
	public MyHighlightingTree() {
		setOpaque(false);
	}

	@Override
	public Color getFileColorFor(Object object) {
		if (object instanceof MyTreeUserObject) {
			if (((MyTreeUserObject) object).isHighlight()) {
				if (UIUtil.isUnderDarcula()) {
					return darker(JBColor.CYAN, 8);
				} else {
					return softer(Color.CYAN);
				}
			}
		}
		return super.getFileColorFor(object);
	}

	@Override
	public boolean isFileColorsEnabled() {
		return true;
	}

}
