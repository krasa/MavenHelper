package krasa.mavenrun.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import krasa.mavenrun.model.ApplicationSettings;
import krasa.mavenrun.model.Goal;

import com.intellij.ui.components.JBList;

/**
 * @author Vojtech Krasa
 */
public class ApplicationSettingsForm {
	protected ApplicationSettings settings;
	private JLabel goalsLabel;
	private JList goals;
	protected DefaultListModel model;
	private JComponent rootComponent;
	private JButton deleteButton;

	public ApplicationSettingsForm(ApplicationSettings settings) {
		this.settings = settings.clone();
		initializeModel(settings);
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object[] selectedValues = goals.getSelectedValues();
				for (Object goal : selectedValues) {
					boolean remove = ApplicationSettingsForm.this.settings.getGoals().remove(goal);
					if (!remove || !model.removeElement(goal)) {
						throw new IllegalStateException("delete failed");
					}
				}
			}
		});
	}

	private void createUIComponents() {
		model = createModel();
		goals = createJBList();
	}

	private DefaultListModel createModel() {
		return new DefaultListModel();
	}

	private JBList createJBList() {
		JBList jbList = new JBList(model);
		jbList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				final Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				Goal goal = (Goal) value;
				setText(goal.getCommandLine());
				return comp;
			}
		});
		return jbList;
	}

	private void initializeModel(final ApplicationSettings settings1) {
		model.clear();
		for (Goal o : settings1.getGoals()) {
			model.addElement(o);
		}
	}

	public ApplicationSettings getSettings() {
		return settings;
	}

	public JComponent getRootComponent() {
		return rootComponent;
	}

	public boolean isSettingsModified(ApplicationSettings settings) {
		return !this.settings.equals(settings);
	}

	public void importFrom(ApplicationSettings settings) {
		this.settings = settings.clone();
		initializeModel(settings);
	}
}
