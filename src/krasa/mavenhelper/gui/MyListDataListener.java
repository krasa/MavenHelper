package krasa.mavenhelper.gui;

import krasa.mavenhelper.model.Goal;
import krasa.mavenhelper.model.Goals;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.List;

/**
 * @author Vojtech Krasa
 */
public class MyListDataListener implements ListDataListener {
	private DefaultListModel<Goal> model;
	private Goals goals;

	public MyListDataListener(DefaultListModel<Goal> model, Goals goals) {
		this.model = model;
		this.goals = goals;
	}

	@Override
	public void intervalAdded(ListDataEvent e) {
		listChanged();
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		listChanged();
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		listChanged();
	}

	private void listChanged() {
		List<Goal> list = this.goals.getGoals();
		list.clear();
		for (int i = 0; i < model.getSize(); i++) {
			list.add(model.getElementAt(i));
		}
	}
}
