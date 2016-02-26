package se.freedrikp.econview.gui.dialogs;

import java.awt.GridLayout;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import se.freedrikp.econview.common.Language;
import se.freedrikp.econview.database.Security;

public class AddUserDialog {
	private Security sec;
	private boolean firstUser;
	
	public AddUserDialog(Security sec,boolean firstUser){
		this.sec = sec;		
		this.firstUser = firstUser;
	}
	
	public void showDialog(){
		JPanel promptPanel = new JPanel();
		promptPanel.setLayout(new GridLayout(3, 2, 0, 0));
		promptPanel.add(new JLabel(Language
				.getString("PROMPT_USERNAME") + ":"));
		JTextField userField = new JTextField(15);
		promptPanel.add(userField);
		promptPanel.add(new JLabel(Language
				.getString("PROMPT_PASSWORD") + ":"));
		JPasswordField passField = new JPasswordField(15);
		promptPanel.add(passField);
		promptPanel.add(new JLabel(Language
				.getString("PROMPT_PASSWORD") + ":"));
		JPasswordField passField2 = new JPasswordField(15);
		promptPanel.add(passField2);
		boolean matched = false;
		while (!matched) {
			int result = JOptionPane.showConfirmDialog(null,
					promptPanel,
					Language.getString("USER_DETAILS_PROMPT"),
					JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				if (!Arrays.equals(passField.getPassword(),
						passField2.getPassword())) {
					JOptionPane.showMessageDialog(null,
							Language.getString("PASSWORDS_NOT_MATCH"),
							Language.getString("PASSWORD_ERROR"),
							JOptionPane.ERROR_MESSAGE);
				} else {
					if (!sec.addUser(userField.getText(), new String(
							passField.getPassword()), firstUser)) {
						JOptionPane.showMessageDialog(null,
								Language.getString("USER_EXISTS"),
								Language.getString("USER_ERROR"),
								JOptionPane.ERROR_MESSAGE);
					}
					matched = true;
				}
			} else {
				if (firstUser){
					String user = "admin";
					String password = "1234";
					sec.addUser(user, password, firstUser);
				}
				matched = true;
			}
		}
	}

}
