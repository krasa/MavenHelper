package krasa.mavenhelper;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.IconLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Donate {
	private static final Logger LOG = Logger.getInstance(Donate.class);

	public static final Icon ICON = IconLoader.getIcon("/krasa/mavenhelper/icons/coins_in_hand.png", Donate.class);

	public static void init(JButton donate) {
		donate.setText("Donate");
		donate.setIcon(ICON);
		donate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BrowserUtil.browse("https://www.paypal.me/VojtechKrasa");
			}
		});
	}
}
