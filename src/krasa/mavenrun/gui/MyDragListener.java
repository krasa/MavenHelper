package krasa.mavenrun.gui;

import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;

import javax.swing.*;

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

	public void dragGestureRecognized(DragGestureEvent dge) {
		StringSelection transferable = new StringSelection(Integer.toString(list.getSelectedIndex()));
		ds.startDrag(dge, DragSource.DefaultMoveDrop, transferable, this);
	}

	public void dragEnter(DragSourceDragEvent dsde) {
	}

	public void dragExit(DragSourceEvent dse) {
	}

	public void dragOver(DragSourceDragEvent dsde) {
	}

	public void dragDropEnd(DragSourceDropEvent dsde) {
		if (dsde.getDropSuccess()) {
			System.out.println("Succeeded");
		} else {
			System.out.println("Failed");
		}
	}

	public void dropActionChanged(DragSourceDragEvent dsde) {
	}
}
