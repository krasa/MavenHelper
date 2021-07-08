package krasa.mavenhelper.gui;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.ui.ColorPicker;
import com.intellij.ui.DoubleClickListener;
import com.intellij.ui.JBColor;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import java.util.ArrayList;
import krasa.mavenhelper.Donate;
import krasa.mavenhelper.model.ApplicationSettings;
import krasa.mavenhelper.model.Goal;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.*;
import java.util.EventListener;
import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class ApplicationSettingsForm {
	private static final Logger LOG = Logger.getInstance(ApplicationSettingsForm.class);

	protected DefaultListModel<Goal> goalsModel;
	protected DefaultListModel<Goal> pluginsModel;

	protected ApplicationSettings settings;
	private JList goals;
	private JComponent rootComponent;
	private JList pluginAwareGoals;
	private JCheckBox useIgnoredPoms;
	private JButton donate;
	private JPanel myPathVariablesPanel;
	private JPanel goalsPanel;
	private JPanel pluginAwareGoalsPanel;
	private JSplitPane split;
	private JCheckBox enableDelete;
	private JCheckBox resolveWorkspaceArtifactsCheckBox;
	private JLabel searchBackgroundColorNameLabel;
	private JLabel searchBackgroundColorPickerLabel;
	private JLabel conflictsForegroundColorNameLabel;
	private JLabel conflictsForegroundColorPickerLabel;

	protected JBList focusedComponent;
	private AliasTable aliasTable;

	public ApplicationSettingsForm(ApplicationSettings original) {
		this.settings = original.clone();
		aliasTable = new AliasTable(this.settings);
		myPathVariablesPanel.add(
			ToolbarDecorator.createDecorator(aliasTable)
				.setAddAction(new AnActionButtonRunnable() {
					@Override
					public void run(AnActionButton button) {
						aliasTable.addAlias();
					}
				}).setRemoveAction(new AnActionButtonRunnable() {
				@Override
				public void run(AnActionButton button) {
					aliasTable.removeSelectedAliases();
				}
			}).setEditAction(new AnActionButtonRunnable() {
				@Override
				public void run(AnActionButton button) {
					aliasTable.editAlias();
				}
			}).setMoveUpAction(new AnActionButtonRunnable() {
				@Override
				public void run(AnActionButton anActionButton) {
					aliasTable.moveUp();
				}
			}).setMoveDownAction(new AnActionButtonRunnable() {
				@Override
				public void run(AnActionButton anActionButton) {
					aliasTable.moveDown();
				}
			}).addExtraAction(new AnActionButton("Reset Default Aliases", AllIcons.Actions.Rollback) {
				@Override
				public void actionPerformed(@NotNull AnActionEvent anActionEvent) {
					aliasTable.resetDefaultAliases();
				}
			}).createPanel(), BorderLayout.CENTER);

		new DoubleClickListener() {
			@Override
			protected boolean onDoubleClick(MouseEvent e) {
				return aliasTable.editAlias();
			}
		}.installOn(aliasTable);

		goalsModel = new DefaultListModel<>();
		pluginsModel = new DefaultListModel<>();
		goals = createJBList(goalsModel);
		pluginAwareGoals = createJBList(pluginsModel);
		new DoubleClickListener() {
			@Override
			protected boolean onDoubleClick(MouseEvent e) {
				return editGoal(goals);
			}
		}.installOn(goals);

		new DoubleClickListener() {
			@Override
			protected boolean onDoubleClick(MouseEvent e) {
				return editGoal(pluginAwareGoals);
			}
		}.installOn(pluginAwareGoals);

		goalsPanel.add(
			ToolbarDecorator.createDecorator(goals)
				.setAddAction(new AnActionButtonRunnable() {
					@Override
					public void run(AnActionButton button) {
						Goal o = newGoal(ApplicationSettingsForm.this.settings);
						if (o != null) {
							goalsModel.addElement(o);
						}
					}
				}).setRemoveAction(new AnActionButtonRunnable() {
				@Override
				public void run(AnActionButton button) {
					delete(goalsModel);
				}
			}).setEditAction(new AnActionButtonRunnable() {
				@Override
				public void run(AnActionButton button) {
					editGoal(goals);
				}
			}).createPanel(), BorderLayout.CENTER);


		pluginAwareGoalsPanel.add(
			ToolbarDecorator.createDecorator(pluginAwareGoals)
				.setAddAction(new AnActionButtonRunnable() {
					@Override
					public void run(AnActionButton button) {
						Goal o = newGoal(ApplicationSettingsForm.this.settings);
						if (o != null) {
							pluginsModel.addElement(o);
						}
					}
				}).setRemoveAction(new AnActionButtonRunnable() {
				@Override
				public void run(AnActionButton button) {
					delete(pluginsModel);
				}
			}).setEditAction(new AnActionButtonRunnable() {
				@Override
				public void run(AnActionButton button) {
					editGoal(pluginAwareGoals);
				}
			}).createPanel(), BorderLayout.CENTER);


		final FocusAdapter focusListener = getFocusListener();
		pluginAwareGoals.addFocusListener(focusListener);
		goals.addFocusListener(focusListener);

		final KeyAdapter keyAdapter = getDeleteKeyListener();
		goals.addKeyListener(keyAdapter);
		pluginAwareGoals.addKeyListener(keyAdapter);

		useIgnoredPoms.setSelected(this.settings.isUseIgnoredPoms());
		Donate.init(rootComponent, donate);

		searchBackgroundColorPickerLabel.setPreferredSize(new Dimension(20, 20));
		searchBackgroundColorPickerLabel.setOpaque(true);
		searchBackgroundColorPickerLabel.setBackground(new JBColor(new Color(settings.getSearchBackgroundColor()), new Color(settings.getSearchBackgroundColor())));

		conflictsForegroundColorPickerLabel.setPreferredSize(new Dimension(20, 20));
		conflictsForegroundColorPickerLabel.setOpaque(true);
		conflictsForegroundColorPickerLabel.setBackground(new JBColor(new Color(settings.getConflictsForegroundColor()), new Color(settings.getConflictsForegroundColor())));

		searchBackgroundColorPickerLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Color color = ColorPicker.showDialog(rootComponent, "ColorPicker", new JBColor(new Color(settings.getSearchBackgroundColor()), new Color(settings.getSearchBackgroundColor())), true, new ArrayList<>(), true);
				if (color != null) {
					searchBackgroundColorPickerLabel.setBackground(color);
					settings.setSearchBackgroundColor(color.getRGB());
				} else {
					searchBackgroundColorPickerLabel.setBackground(new JBColor(new Color(settings.getSearchBackgroundColor()), new Color(settings.getSearchBackgroundColor())));
					settings.setSearchBackgroundColor(settings.getSearchBackgroundColor());
				}
				super.mouseClicked(e);
			}
		});

		conflictsForegroundColorPickerLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Color color = ColorPicker.showDialog(rootComponent, "ColorPicker", new JBColor(new Color(settings.getConflictsForegroundColor()), new Color(settings.getConflictsForegroundColor())), true, new ArrayList<>(), true);
				if (color != null) {
					conflictsForegroundColorPickerLabel.setBackground(color);
					settings.setConflictsForegroundColor(color.getRGB());
				} else {
					conflictsForegroundColorPickerLabel.setBackground(new JBColor(new Color(settings.getConflictsForegroundColor()), new Color(settings.getConflictsForegroundColor())));
					settings.setConflictsForegroundColor(settings.getConflictsForegroundColor());
				}
				super.mouseClicked(e);
			}
		});
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

	public Goal newGoal(final ApplicationSettings settings1) {
		Goal o = null;

		GoalEditor editor = new GoalEditor("New Goal", "", settings1, false, null, null);
		if (editor.showAndGet()) {
			String s = editor.getCmd();
			if (StringUtils.isNotBlank(s)) {
				o = new Goal(s);
			}
		}
		return o;
	}

	private boolean editGoal(JList goals) {
		Object selectedValue = goals.getSelectedValue();
		if (selectedValue != null) {
			GoalEditor.editGoal("Edit Goal", settings, (Goal) selectedValue);
			return true;
		}
		return false;
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
		List selectedValues = focusedComponent.getSelectedValuesList();
		for (Object goal : selectedValues) {
			goals.removeElement(goal);
		}
	}

	private void createUIComponents() {
	}

	private JBList createJBList(DefaultListModel pluginsModel) {
		JBList jbList = new JBList(pluginsModel);
		jbList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
														  boolean cellHasFocus) {
				final Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				Goal goal = (Goal) value;

				setText(goal.getPresentableName());
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
		setData(settings);
		aliasTable.reset(settings);
	}

	private void removeListeners(final DefaultListModel listModel) {
		EventListener[] listeners = listModel.getListeners(MyListDataListener.class);
		for (EventListener listDataListener : listeners) {
			listModel.removeListDataListener((ListDataListener) listDataListener);
		}
	}

	private void addModelListeners(ApplicationSettings settings) {
		goalsModel.addListDataListener(new MyListDataListener(goalsModel, settings.getGoals()));
		pluginsModel.addListDataListener(new MyListDataListener(pluginsModel, settings.getPluginAwareGoals()));
	}

	public ApplicationSettings getSettings() {
		aliasTable.commit(settings);
		getData(settings);
		return settings;
	}

	public JComponent getRootComponent() {
		return rootComponent;
	}

	public boolean isSettingsModified(ApplicationSettings settings) {
		if (aliasTable.isModified(settings)) return true;
		return !this.settings.equals(settings) || isModified(settings);
	}

	public void importFrom(ApplicationSettings settings) {
		this.settings = settings.clone();
		initializeModel();
	}

	public void setData(ApplicationSettings data) {
		useIgnoredPoms.setSelected(data.isUseIgnoredPoms());
		enableDelete.setSelected(data.isEnableDelete());
		resolveWorkspaceArtifactsCheckBox.setSelected(data.isResolveWorkspaceArtifacts());
	}

	public void getData(ApplicationSettings data) {
		data.setUseIgnoredPoms(useIgnoredPoms.isSelected());
		data.setEnableDelete(enableDelete.isSelected());
		data.setResolveWorkspaceArtifacts(resolveWorkspaceArtifactsCheckBox.isSelected());
	}

	public boolean isModified(ApplicationSettings data) {      
		if (useIgnoredPoms.isSelected() != data.isUseIgnoredPoms()) return true;
		if (enableDelete.isSelected() != data.isEnableDelete()) return true;
		if (resolveWorkspaceArtifactsCheckBox.isSelected() != data.isResolveWorkspaceArtifacts()) return true;
		return false;
	}
}
