package krasa.mavenhelper.gui;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Class Name is BorderLayoutPanel
 *
 * @author LiJun
 * Created on 2022/4/18 16:31
 */
public class BorderLayoutPanel extends JPanel {

    public BorderLayoutPanel() {
        super(new BorderLayout());
    }

    public BorderLayoutPanel(int hgap, int vgap) {
        super(new BorderLayout(hgap, vgap));
    }

    public static BorderLayoutPanel getInstance() {
        return new BorderLayoutPanel();
    }

    public static BorderLayoutPanel getInstance(int hgap, int vgap) {
        return new BorderLayoutPanel(hgap, vgap);
    }

    @NotNull
    public final BorderLayoutPanel center(@NotNull Component comp) {
        add(comp, BorderLayout.CENTER);
        return this;
    }


    @NotNull
    public final BorderLayoutPanel right(@NotNull Component comp) {
        add(comp, BorderLayout.EAST);
        return this;
    }

    @NotNull
    public final BorderLayoutPanel left(@NotNull Component comp) {
        add(comp, BorderLayout.WEST);
        return this;
    }

    @NotNull
    public final BorderLayoutPanel top(@NotNull Component comp) {
        add(comp, BorderLayout.NORTH);
        return this;
    }

    @NotNull
    public final BorderLayoutPanel bottom(@NotNull Component comp) {
        add(comp, BorderLayout.SOUTH);
        return this;
    }

}