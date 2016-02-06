package se.freedrikp.econview.gui.menubar;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import se.freedrikp.econview.gui.Configuration;
import se.freedrikp.econview.gui.Language;

public class SettingsMenu extends JMenu {

	public SettingsMenu() {
		super(Language.getString("MENUBAR_SETTINGS"));

		JMenuItem mntmConfig = new JMenuItem(
				Language.getString("MENUBAR_SETTINGS_CONFIGURATION"));
		mntmConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showSettingsPanel(true);
			}
		});
		add(mntmConfig);

		JMenuItem mntmLanguage = new JMenuItem(
				Language.getString("MENUBAR_SETTINGS_LANGUAGE"));
		mntmLanguage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showSettingsPanel(false);
			}
		});
		add(mntmLanguage);
	}

	private void showSettingsPanel(boolean configurationNotLanguage) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane();
		Map<String, JTextField> map = new HashMap<String, JTextField>();
		scrollPane.setViewportView(panel);
		Map<String, String> list = configurationNotLanguage ? Configuration
				.listAllConfigs() : Language.listAllStrings();
		Map<String,String> temp = new TreeMap<String,String>();
		for (Map.Entry<String, String> entry : list.entrySet()) {
			temp.put(entry.getKey(), entry.getValue());
		}
		list = temp;
		for (Map.Entry<String, String> entry : list.entrySet()) {
			JPanel pan = new JPanel();
			pan.add(new JLabel(entry.getKey() + ":"));
			int size = configurationNotLanguage ? 10 : 20;
			JTextField field = new JTextField(entry.getValue(), size);
			pan.add(field);
			panel.add(pan);
			map.put(entry.getKey(), field);
		}
		scrollPane.setPreferredSize(new Dimension(Configuration
				.getInt("SETTINGS_PANEL_WIDTH"), Configuration
				.getInt("SETTINGS_PANEL_HEIGHT")));
		String title = configurationNotLanguage ? Language
				.getString("SETTINGS_CONFIGURATION") : Language
				.getString("SETTINGS_LANGUAGE");
		int result = JOptionPane.showConfirmDialog(null, scrollPane, title,
				JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			for (Map.Entry<String, JTextField> entry : map.entrySet()) {
				if (configurationNotLanguage) {
					Configuration.putConfig(entry.getKey(), entry.getValue()
							.getText());
				} else {
					Language.putString(entry.getKey(), entry.getValue()
							.getText());
				}
			}
		}
	}
}
