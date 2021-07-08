// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package krasa.mavenhelper.gui;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.ui.JBColor;
import com.intellij.ui.table.JBTable;
import krasa.mavenhelper.model.Alias;
import krasa.mavenhelper.model.ApplicationSettings;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AliasTable extends JBTable {
	private static final Logger LOG = Logger.getInstance(AliasTable.class);
	private final MyTableModel myTableModel = new MyTableModel();
	private static final int NAME_COLUMN = 0;
	private static final int VALUE_COLUMN = 1;

	private final List<Alias> myAliases = new ArrayList<>();

	public AliasTable(ApplicationSettings original) {
		setModel(myTableModel);
		TableColumn column = getColumnModel().getColumn(NAME_COLUMN);
		column.setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				final Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				final String macroValue = getAliasValueAt(row);
				component.setForeground(macroValue.length() == 0
					? new JBColor(original.getConflictsForegroundColor(), original.getConflictsForegroundColor())
					: isSelected ? table.getSelectionForeground() : table.getForeground());
				return component;
			}
		});
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}

	public String getAliasValueAt(int row) {
		return (String) getValueAt(row, VALUE_COLUMN);
	}

	public void addAlias() {
		final AliasEditor macroEditor = new AliasEditor("Add alias", "", "", new EditValidator());
		if (macroEditor.showAndGet()) {
			final String name = macroEditor.getFrom();
			myAliases.add(Alias.of(name, macroEditor.getTo()));
			final int index = indexOfAliasWithName(name);
			LOG.assertTrue(index >= 0);
			myTableModel.fireTableDataChanged();
			setRowSelectionInterval(index, index);
		}
	}

	private boolean isValidRow(int selectedRow) {
		return selectedRow >= 0 && selectedRow < myAliases.size();
	}

	public void moveUp() {
		int selectedRow = getSelectedRow();
		int index1 = selectedRow - 1;
		if (selectedRow != -1) {
			Collections.swap(myAliases, selectedRow, index1);
		}
		setRowSelectionInterval(index1, index1);
	}

	public void moveDown() {
		int selectedRow = getSelectedRow();
		int index1 = selectedRow + 1;
		if (selectedRow != -1) {
			Collections.swap(myAliases, selectedRow, index1);
		}
		setRowSelectionInterval(index1, index1);
	}


	public void removeSelectedAliases() {
		final int[] selectedRows = getSelectedRows();
		if (selectedRows.length == 0) return;
		Arrays.sort(selectedRows);
		final int originalRow = selectedRows[0];
		for (int i = selectedRows.length - 1; i >= 0; i--) {
			final int selectedRow = selectedRows[i];
			if (isValidRow(selectedRow)) {
				myAliases.remove(selectedRow);
			}
		}
		myTableModel.fireTableDataChanged();
		if (originalRow < getRowCount()) {
			setRowSelectionInterval(originalRow, originalRow);
		} else if (getRowCount() > 0) {
			final int index = getRowCount() - 1;
			setRowSelectionInterval(index, index);
		}
	}

	public void commit(ApplicationSettings settings) {
		settings.getAliases().setAliases(new ArrayList<>(myAliases));
	}

	public void resetDefaultAliases() {
		ApplicationSettings.resetDefaultAliases(myAliases);
		myTableModel.fireTableDataChanged();
	}

	public void reset(ApplicationSettings settings) {
		obtainAliases(myAliases, settings);
		myTableModel.fireTableDataChanged();
	}


	private int indexOfAliasWithName(String name) {
		for (int i = 0; i < myAliases.size(); i++) {
			final Alias pair = myAliases.get(i);
			if (name.equals(pair.getFrom())) {
				return i;
			}
		}
		return -1;
	}

	private void obtainAliases(@NotNull List<Alias> aliases, ApplicationSettings settings) {
		aliases.clear();
		aliases.addAll(settings.getAliases().getAliases());
	}

	public boolean editAlias() {
		if (getSelectedRowCount() != 1) {
			return false;
		}
		final int selectedRow = getSelectedRow();
		final Alias alias = myAliases.get(selectedRow);
		final AliasEditor editor = new AliasEditor("Edit Alias", alias.getFrom(), alias.getTo(), new EditValidator());
		if (editor.showAndGet()) {
			alias.setFrom(editor.getFrom());
			alias.setTo(editor.getTo());
			myTableModel.fireTableDataChanged();
		}
		return true;
	}

	public boolean isModified(ApplicationSettings settings) {
		final ArrayList<Alias> aliases = new ArrayList<>();
		obtainAliases(aliases, settings);
		return !aliases.equals(myAliases);
	}


	private class MyTableModel extends AbstractTableModel {
		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return myAliases.size();
		}

		@Override
		public Class getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			final Alias pair = myAliases.get(rowIndex);
			switch (columnIndex) {
				case NAME_COLUMN:
					return pair.getFrom();
				case VALUE_COLUMN:
					return pair.getTo();
			}
			LOG.error("Wrong indices");
			return null;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
				case NAME_COLUMN:
					return "From";
				case VALUE_COLUMN:
					return "To";
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}
	}

	private static class EditValidator implements AliasEditor.Validator {

		@Override
		public boolean isOK(String name, String value) {
			return !name.isEmpty() && !value.isEmpty();
		}
	}
}
