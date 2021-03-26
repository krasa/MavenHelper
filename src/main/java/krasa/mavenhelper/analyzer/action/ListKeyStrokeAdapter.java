package krasa.mavenhelper.analyzer.action;

import com.intellij.openapi.actionSystem.KeyboardShortcut;
import com.intellij.openapi.actionSystem.Shortcut;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.ui.KeyStrokeAdapter;
import krasa.mavenhelper.analyzer.GuiForm;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class ListKeyStrokeAdapter extends KeyStrokeAdapter {
	private final GuiForm guiForm;

	public ListKeyStrokeAdapter(GuiForm guiForm) {
		this.guiForm = guiForm;
	}

	@Override
	public void keyPressed(KeyEvent event) {
		Shortcut[] shortcuts = KeymapManager.getInstance().getActiveKeymap().getShortcuts("EditSource");
		if (shortcuts.length > 0) {
			Shortcut shortcut = shortcuts[0];
			if (shortcut.isKeyboard()) {
				KeyboardShortcut key = (KeyboardShortcut) shortcut;
				KeyStroke firstKeyStroke = key.getFirstKeyStroke();
				KeyStroke keyStrokeForEvent = KeyStroke.getKeyStrokeForEvent(event);
				if (keyStrokeForEvent.equals(firstKeyStroke)) {
					guiForm.switchToLeftTree();
				}
			}
		}

	}

}
