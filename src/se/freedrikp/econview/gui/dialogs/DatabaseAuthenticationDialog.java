package se.freedrikp.econview.gui.dialogs;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import se.freedrikp.econview.common.Language;

public class DatabaseAuthenticationDialog {
	private String database;
	private String username;
	private String password;

	public DatabaseAuthenticationDialog() {

	}

	public String getDatabase() {
		return database;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public boolean showDialog() {
		JPanel promptPanel = new JPanel();
		promptPanel.setLayout(new GridLayout(3, 2, 0, 0));
		promptPanel
				.add(new JLabel(Language.getString("PROMPT_DATABASE") + ":"));
		JTextField databaseField = new JTextField(15);
		promptPanel.add(databaseField);
		promptPanel
				.add(new JLabel(Language.getString("PROMPT_USERNAME") + ":"));
		JTextField userField = new JTextField(15);
		promptPanel.add(userField);
		promptPanel
				.add(new JLabel(Language.getString("PROMPT_PASSWORD") + ":"));
		JPasswordField passField = new JPasswordField(15);
		promptPanel.add(passField);
		int result = JOptionPane.showConfirmDialog(null, promptPanel,
				Language.getString("USER_DETAILS_PROMPT"),
				JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			database = databaseField.getText();
			username = userField.getText();
			password = new String(passField.getPassword());
			return true;
		}
		return false;
	}
}
