package krasa.mavenrun.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.*;

import com.intellij.ui.components.JBList;

/**
 * @author Vojtech Krasa
 */
class MyListDropHandler extends TransferHandler {
	JBList list;

	public MyListDropHandler(JBList list) {
		this.list = list;
	}

	public boolean canImport(TransferHandler.TransferSupport support) {
		if (!support.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			return false;
		}
		JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
		if (dl.getIndex() == -1) {
			return false;
		} else {
			return true;
		}
	}

	public boolean importData(TransferHandler.TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}

		Transferable transferable = support.getTransferable();
		String indexString;
		try {
			indexString = (String) transferable.getTransferData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			return false;
		}

		int index = Integer.parseInt(indexString);
		JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();
		int dropTargetIndex = dl.getIndex();

		DefaultListModel model = (DefaultListModel) list.getModel();
		Object elementAt = model.getElementAt(index);

		model.insertElementAt(elementAt, dropTargetIndex);
		model.remove(getActualRemoveIndex(index, dropTargetIndex));

		int actualDropIndex = getActualDropIndex(index, dropTargetIndex);
		list.getSelectionModel().setSelectionInterval(actualDropIndex, actualDropIndex);
		// list.repaint();
		return true;
	}

	private int getActualRemoveIndex(int index, int dropTargetIndex) {
		int index1;
		if (dropTargetIndex < index) {
			index1 = index + 1;
		} else {
			index1 = index;
		}
		return index1;
	}

	private int getActualDropIndex(int index, int dropTargetIndex) {
		int index1;
		if (dropTargetIndex < index) {
			index1 = dropTargetIndex;
		} else {
			index1 = dropTargetIndex - 1;
		}
		return index1;
	}
}
