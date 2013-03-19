package krasa.mavenrun.gui;

import static com.intellij.openapi.ui.Messages.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;

import javax.swing.*;

import krasa.mavenrun.model.ApplicationSettings;
import krasa.mavenrun.model.Goal;

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
	private JButton add;
	private JButton addPluginAware;

	protected JBList focusedComponent;

	public ApplicationSettingsForm(ApplicationSettings settings) {
		this.settings = settings.clone();
		initializeModel();
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Goal o = showDialog(ApplicationSettingsForm.this.settings.getGoalsAsStrings());
				if (o != null) {
					ApplicationSettingsForm.this.settings.getGoals().add(o);
					initializeModel();
				}
			}
		});
		addPluginAware.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				List<String> goalsAsStrings = ApplicationSettingsForm.this.settings.getPluginAwareGoalsAsString();
				Goal o = showDialog(goalsAsStrings);
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

	private Goal showDialog(List<String> goalsAsStrings) {
		Goal o = null;
		String s = Messages.showEditableChooseDialog("Command line:", "New Goal", getQuestionIcon(),
				toArray(goalsAsStrings), "", new NonEmptyInputValidator());
		if (StringUtils.isNotBlank(s)) {
			o = new Goal(s);
		}
		return o;
	}

	private String[] toArray(List<String> goalsAsStrings) {
		return goalsAsStrings.toArray(new String[goalsAsStrings.size()]);
	}

	private FocusAdapter getFocusListener() {
		return new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				focusedComponent = (JBList) e.getComponent();
			}
		};
	}

	private ActionListener deleteListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (focusedComponent == goals) {
					Object[] selectedValues = focusedComponent.getSelectedValues();
					for (Object goal : selectedValues) {
						boolean remove = settings.getGoals().remove(goal);
						if (!remove || !((DefaultListModel) focusedComponent.getModel()).removeElement(goal)) {
							throw new IllegalStateException("delete failed");
						}
					}
				} else if (focusedComponent == pluginAwareGoals) {
					Object[] selectedValues = focusedComponent.getSelectedValues();
					for (Object goal : selectedValues) {
						boolean remove = settings.getPluginAwareGoals().remove(goal);
						if (!remove || !((DefaultListModel) focusedComponent.getModel()).removeElement(goal)) {
							throw new IllegalStateException("delete failed");
						}
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
		for (Goal o : settings.getGoals()) {
			model.addElement(o);
		}
		for (Goal o : settings.getPluginAwareGoals()) {
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
