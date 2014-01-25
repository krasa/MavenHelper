package krasa.mavenrun.gui;

import static com.intellij.openapi.ui.Messages.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.*;
import javax.swing.event.ListDataListener;

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

	protected DefaultListModel goalsModel;
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
		addGoal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Goal o = showDialog(ApplicationSettingsForm.this.settings);
				if (o != null) {
					goalsModel.addElement(o);
				}
			}
		});
		addPluginAware.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Goal o = showDialog(ApplicationSettingsForm.this.settings);
				if (o != null) {
					pluginsModel.addElement(o);
				}
			}
		});
		deleteButton.addActionListener(deleteListener());

		final FocusAdapter focusListener = getFocusListener();
		pluginAwareGoals.addFocusListener(focusListener);
		goals.addFocusListener(focusListener);

		final KeyAdapter keyAdapter = getDeleteKeyListener();
		goals.addKeyListener(keyAdapter);
		pluginAwareGoals.addKeyListener(keyAdapter);
	}

	private KeyAdapter getDeleteKeyListener() {
		return new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == 127) {
					deleteGoal();
				}
			}
		};
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
				deleteGoal();
			}
		};
	}

	private void deleteGoal() {
		if (focusedComponent == goals) {
			delete(goalsModel);
		} else if (focusedComponent == pluginAwareGoals) {
			delete(pluginsModel);
		}
	}

	private void delete(DefaultListModel goals) {
		Object[] selectedValues = focusedComponent.getSelectedValues();
		for (Object goal : selectedValues) {
			goals.removeElement(goal);
		}
	}

	private void createUIComponents() {
		goalsModel = new DefaultListModel();
		pluginsModel = new DefaultListModel();
		goals = createJBList(goalsModel);
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
		jbList.setDragEnabled(true);
		jbList.setDropMode(DropMode.INSERT);
		jbList.setTransferHandler(new MyListDropHandler(jbList));

		new MyDragListener(jbList);
		return jbList;
	}

	private void initializeModel() {
		removeListeners(goalsModel);
		removeListeners(pluginsModel);
		goalsModel.clear();
		pluginsModel.clear();
		for (Goal o : settings.getGoals().getGoals()) {
			goalsModel.addElement(o);
		}
		for (Goal o : settings.getPluginAwareGoals().getGoals()) {
			pluginsModel.addElement(o);
		}
		addModelListeners(settings);
	}

	private void removeListeners(final DefaultListModel listModel) {
		ListDataListener[] listDataListeners = listModel.getListeners(MyListDataListener.class);
		for (ListDataListener listDataListener : listDataListeners) {
			listModel.removeListDataListener(listDataListener);
		}
	}

	private void addModelListeners(ApplicationSettings settings) {
		goalsModel.addListDataListener(new MyListDataListener(goalsModel, settings.getGoals()));
		pluginsModel.addListDataListener(new MyListDataListener(pluginsModel, settings.getPluginAwareGoals()));
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
