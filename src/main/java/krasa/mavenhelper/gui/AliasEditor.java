package krasa.mavenhelper.gui;

import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.DocumentAdapter;
import krasa.mavenhelper.model.ApplicationSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;

public class AliasEditor extends DialogWrapper {
	private JTextField fromField;
	private JPanel myPanel;
	private final Validator myValidator;
	private GoalEditor goalEditor;

	public interface Validator {
		boolean isOK(String name, String value);
	}

	public AliasEditor(String title, String macroName, String value, Validator validator) {
		super(true);
		setTitle(title);
		myValidator = validator;

		goalEditor = new GoalEditor(null, value, ApplicationSettings.get(), false, null, null) {
			@Override
			protected void updateControls() {
				if (goalEditor != null) {
					AliasEditor.this.updateControls();
				}
			}
		};
		goalEditor.commandLineLabel.setText("To:");
		//noinspection deprecation
		fromField.setNextFocusableComponent(goalEditor.getPreferredFocusedComponent());

		fromField.getDocument().addDocumentListener(new DocumentAdapter() {
			@Override
			public void textChanged(@NotNull DocumentEvent event) {
				updateControls();
			}
		});

		fromField.setText(macroName);
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
		return goalEditor.getCmd();
	}

	@Override
	protected JComponent createNorthPanel() {
		return myPanel;
	}

	@Override
	protected JComponent createCenterPanel() {
		return goalEditor.createCenterPanel();
	}

	@Override
	@Nullable
	protected String getDimensionServiceKey() {
//		return null;
		return GoalEditor.DIMENSION;
	}

}