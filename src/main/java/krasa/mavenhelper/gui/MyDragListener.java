package krasa.mavenhelper.gui;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;

/**
 * @author Vojtech Krasa
 */

class MyDragListener implements DragSourceListener, DragGestureListener {
	JList list;

	DragSource ds = new DragSource();

	public MyDragListener(JList list) {
		this.list = list;
		DragGestureRecognizer dgr = ds.createDefaultDragGestureRecognizer(list, DnDConstants.ACTION_MOVE, this);

	}

	@Override
	public void dragGestureRecognized(DragGestureEvent dge) {
		StringSelection transferable = new StringSelection(Integer.toString(list.getSelectedIndex()));
		ds.startDrag(dge, DragSource.DefaultMoveDrop, transferable, this);
	}

	@Override
	public void dragEnter(DragSourceDragEvent dsde) {
	}

	@Override
	public void dragExit(DragSourceEvent dse) {
	}

	@Override
	public void dragOver(DragSourceDragEvent dsde) {
	}

	@Override
	public void dragDropEnd(DragSourceDropEvent dsde) {
	}

	@Override
	public void dropActionChanged(DragSourceDragEvent dsde) {
	}
}
