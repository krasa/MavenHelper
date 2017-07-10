package krasa.mavenhelper.gui;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import krasa.mavenhelper.model.Goal;
import krasa.mavenhelper.model.Goals;

/**
 * @author Vojtech Krasa
 */
public class MyListDataListener implements ListDataListener {
	private DefaultListModel model;
	private Goals goals;

	public MyListDataListener(DefaultListModel model, Goals goals) {
		this.model = model;
		this.goals = goals;
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		goals.getGoals().add(e.getIndex0(), (Goal) model.getElementAt(e.getIndex0()));
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		goals.getGoals().remove(e.getIndex0());
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
	}
}
