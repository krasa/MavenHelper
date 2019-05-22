package krasa.mavenhelper.gui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DocumentAdapter;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AliasEditor extends DialogWrapper {
	private JTextField fromField;
	private JPanel myPanel;
	private JTextField toField;
	private final Validator myValidator;

	public interface Validator {
		boolean isOK(String name, String value);
	}

	public AliasEditor(String title, String macroName, String value, Validator validator) {
		super(true);
		setTitle(title);
		myValidator = validator;
		DocumentListener documentListener = new DocumentAdapter() {
			@Override
			public void textChanged(@NotNull DocumentEvent event) {
				updateControls();
			}
		};
		fromField.getDocument().addDocumentListener(documentListener);
		toField.getDocument().addDocumentListener(documentListener);

		fromField.setText(macroName);
		toField.setText(value);

		init();
		updateControls();
	}

	private void updateControls() {
		getOKAction().setEnabled(myValidator.isOK(getFrom(), getTo()));
	}

	@Override
	public JComponent getPreferredFocusedComponent() {
		return fromField;
	}

	@Override
	protected String getHelpId() {
		return null;
	}

	@Override
	protected void doOKAction() {
		if (!myValidator.isOK(getFrom(), getTo())) return;
		super.doOKAction();
	}

	public String getFrom() {
		return fromField.getText().trim();
	}

	public String getTo() {
		return toField.getText().trim();
	}

	@Override
	protected JComponent createNorthPanel() {
		return myPanel;
	}

	@Override
	protected JComponent createCenterPanel() {
		return null;
	}
}