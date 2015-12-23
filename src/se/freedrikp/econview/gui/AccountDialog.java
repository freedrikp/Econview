package se.freedrikp.econview.gui;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class AccountDialog {

	
	public static Object[] showEditDialog(String selectedName,
			String selectedBalance, boolean selectedIncluded) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JTextField nameField = new JTextField("", 15);
		nameField.setText(selectedName);
		JPanel namePanel = new JPanel();
		namePanel
				.add(new JLabel(Utilities.getString("ADD_ACCOUNT_NAME") + ":"));
		namePanel.add(nameField);
		panel.add(namePanel);

		JTextField balanceField = new JTextField("", 7);
		balanceField.setText(selectedBalance);
		JPanel balancePanel = new JPanel();
		balancePanel.add(new JLabel(Utilities.getString("ADD_ACCOUNT_BALANCE")
				+ ":"));
		balancePanel.add(balanceField);
		panel.add(balancePanel);
		JCheckBox hiddenBox = new JCheckBox(
				Utilities.getString("ADD_ACCOUNT_HIDDEN") + " ",
				selectedIncluded);
		panel.add(hiddenBox);

		int result = JOptionPane.showConfirmDialog(null, panel,
				Utilities.getString("ACCOUNT_DETAILS"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null);

		if (result == JOptionPane.OK_OPTION) {
			Object[] details = new Object[3];
			details[0] = nameField.getText();
			details[1] = balanceField.getText();
			details[2] = hiddenBox.isSelected();
			return details;
		}
		return null;
	}
	
	public static Object[] showAddDialog() {
		return showEditDialog(null,null,true);
	}
	
}
