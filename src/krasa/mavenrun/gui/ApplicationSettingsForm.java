package krasa.mavenrun.gui;

import static com.intellij.openapi.ui.Messages.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.*;

import krasa.mavenrun.model.ApplicationSettings;
import krasa.mavenrun.model.Goal;
import krasa.mavenrun.model.Goals;

import org.apache.commons.lang.StringUtils;

import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NonEmptyInputValidator;
import com.intellij.ui.components.JBList;

/**
 * @author Vojtech Krasa
 */
public class ApplicationSettingsForm {

	protected DefaultListModel model;
	protected DefaultListModel pluginsModel;

	protected ApplicationSettings settings;
	private JList goals;
	private JComponent rootComponent;
	private JButton deleteButton;
	private JList pluginAwareGoals;
	private JButton addGoal;
	private JButton addPluginAware;

	protected JBList focusedComponent;

	public ApplicationSettingsForm(ApplicationSettings settings) {
		this.settings = settings.clone();
		initializeModel();
		addGoal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Goal o = showDialog(ApplicationSettingsForm.this.settings);
				if (o != null) {
					ApplicationSettingsForm.this.settings.getGoals().add(o);
					initializeModel();
				}
			}
		});
		addPluginAware.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Goal o = showDialog(ApplicationSettingsForm.this.settings);
				if (o != null) {
					ApplicationSettingsForm.this.settings.getPluginAwareGoals().add(o);
					initializeModel();
				}
			}
		});

		deleteButton.addActionListener(deleteListener());
		pluginAwareGoals.addFocusListener(getFocusListener());
		goals.addFocusListener(getFocusListener());
	}

	public static Goal showDialog(final ApplicationSettings settings1) {
		String[] goalsAsStrings = settings1.getAllGoalsAsStringArray();
		Goal o = null;
		String s = Messages.showEditableChooseDialog("Command line:", "New Goal", getQuestionIcon(), goalsAsStrings,
				"", new NonEmptyInputValidator());
		if (StringUtils.isNotBlank(s)) {
			o = new Goal(s);
		}
		return o;
	}

	private FocusAdapter getFocusListener() {
		return new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (focusedComponent != null) {
					focusedComponent.getSelectionModel().clearSelection();
				}
				focusedComponent = (JBList) e.getComponent();
			}
		};
	}

	private ActionListener deleteListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (focusedComponent == goals) {
					delete(settings.getGoals());
				} else if (focusedComponent == pluginAwareGoals) {
					delete(settings.getPluginAwareGoals());
				}
			}

			private void delete(Goals goals) {
				Object[] selectedValues = focusedComponent.getSelectedValues();
				for (Object goal : selectedValues) {
					boolean remove = goals.remove(goal);
					if (!remove || !((DefaultListModel) focusedComponent.getModel()).removeElement(goal)) {
						throw new IllegalStateException("delete failed");
					}
				}
			}
		};
	}

	private void createUIComponents() {
		model = new DefaultListModel();
		pluginsModel = new DefaultListModel();
		goals = createJBList(model);
		pluginAwareGoals = createJBList(pluginsModel);
	}

	private JBList createJBList(DefaultListModel pluginsModel) {
		JBList jbList = new JBList(pluginsModel);
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

	private void initializeModel() {
		model.clear();
		pluginsModel.clear();
		for (Goal o : settings.getGoals().getGoals()) {
			model.addElement(o);
		}
		for (Goal o : settings.getPluginAwareGoals().getGoals()) {
			pluginsModel.addElement(o);
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
		initializeModel();
	}
}
