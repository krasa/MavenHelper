package krasa.mavenhelper;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.diagnostic.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Donate {
	private static final Logger LOG = Logger.getInstance(Donate.class);

	public static JComponent newDonateButton(JComponent donatePanel) {
		JButton donate = new JButton();
		init(donatePanel, donate);
		return donate;
	}

	public static void init(JComponent donatePanel, JButton donate) {
		donate.setBorder(null);
		donate.setMargin(new Insets(0, 0, 0, 0));
		donate.setContentAreaFilled(false);
		donate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BrowserUtil.browse("https://www.paypal.me/VojtechKrasa");
			}
		});
		donate.putClientProperty("JButton.backgroundColor", donatePanel.getBackground());
	}
}
