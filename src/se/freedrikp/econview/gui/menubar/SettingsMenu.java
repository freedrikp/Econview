package se.freedrikp.econview.gui.menubar;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import se.freedrikp.econview.gui.Utilities;

public class SettingsMenu extends JMenu {

	public SettingsMenu() {
		super(Utilities.getString("MENUBAR_SETTINGS"));

		JMenuItem mntmConfig = new JMenuItem(
				Utilities.getString("MENUBAR_SETTINGS_CONFIGURATION"));
		mntmConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				JScrollPane scrollPane = new JScrollPane();
				HashMap<String, JTextField> map = new HashMap<String, JTextField>();
				scrollPane.setViewportView(panel);
				for (Map.Entry<String, String> config : Utilities
						.listAllConfigs().entrySet()) {
					JPanel pan = new JPanel();
					pan.add(new JLabel(config.getKey() + ":"));
					JTextField field = new JTextField(config.getValue(), 10);
					pan.add(field);
					panel.add(pan);
					map.put(config.getKey(), field);
				}
				scrollPane.setPreferredSize(new Dimension(
						Integer.parseInt(Utilities
								.getConfig("SETTINGS_CONFIGURATION_PANEL_WIDTH")),
						Integer.parseInt(Utilities
								.getConfig("SETTINGS_CONFIGURATION_PANEL_HEIGHT"))));
				int result = JOptionPane.showConfirmDialog(null, scrollPane,
						Utilities.getString("SETTINGS_CONFIGURATION"),
						JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					for (Map.Entry<String, JTextField> entry : map.entrySet()) {
						Utilities.putConfig(entry.getKey(), entry.getValue()
								.getText());
					}
				}
			}
		});
		add(mntmConfig);
	}
}
