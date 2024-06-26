package krasa.mavenhelper.gui;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Class Name is FlowLayoutPanel
 *
 * @author LiJun
 * Created on 2021/8/25 4:27 下午
 */
public class FlowLayoutPanel extends JPanel {

    public FlowLayoutPanel(int align) {
        super(new FlowLayout(align));
    }

    public FlowLayoutPanel(int align, int hgap, int vgap) {
        super(new FlowLayout(align, hgap, vgap));
    }

    public static FlowLayoutPanel getInstance(int align) {
        return new FlowLayoutPanel(align);
    }

    public static FlowLayoutPanel getInstance(int align, int hgap, int vgap) {
        return new FlowLayoutPanel(align, hgap, vgap);
    }

    public static FlowLayoutPanel leftInstance() {
        return new FlowLayoutPanel(FlowLayout.LEFT);
    }

    public static FlowLayoutPanel leftInstance(int hgap, int vgap) {
        return new FlowLayoutPanel(FlowLayout.LEFT, hgap, vgap);
    }

    public static FlowLayoutPanel centerInstance() {
        return new FlowLayoutPanel(FlowLayout.CENTER);
    }

    public static FlowLayoutPanel rightInstance() {
        return new FlowLayoutPanel(FlowLayout.RIGHT);
    }

    public FlowLayoutPanel add(JComponent component) {
        super.add(component);
        return this;
    }

    public FlowLayoutPanel addMore(JComponent... components) {
        for (JComponent component : components) {
            super.add(component);
        }
        return this;
    }

    public FlowLayoutPanel add(List<? extends JComponent> components) {
        for (JComponent component : components) {
            super.add(component);
        }
        return this;
    }

}
