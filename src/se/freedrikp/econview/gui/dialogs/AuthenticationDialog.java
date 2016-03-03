package se.freedrikp.econview.gui.dialogs;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import se.freedrikp.econview.common.Language;

public class AuthenticationDialog {
	private String username;
	private String password;
	
	public AuthenticationDialog(){
		
	}
	
	public String getUsername(){
		return username;
	}
	
	public String getPassword(){
		return password;
	}

	
	public boolean showDialog(){
		JPanel promptPanel = new JPanel();
		promptPanel.setLayout(new GridLayout(2, 2, 0, 0));
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
			username = userField.getText();
			password = new String(passField.getPassword());
			return true;
		}
		return false;
	}
	
	public void showFailedDialog(boolean exit){
		JOptionPane.showMessageDialog(null,
		Language.getString("PROMPT_ACCESS_DENIED"),
		Language.getString("USER_DETAILS_PROMPT"),
		JOptionPane.WARNING_MESSAGE);
		if (exit){
			System.exit(0);
		}
	}
}
