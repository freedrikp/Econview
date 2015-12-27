package se.freedrikp.econview.gui;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import se.freedrikp.econview.database.Database;

public class AccountDialog {
	private JTextField nameField;
	private JTextField balanceField;
	private JCheckBox hiddenBox;
	private JPanel dialogPanel;
	private Database db;
	
	public AccountDialog(Database db){
		this.db = db;
	}

	private void createDialog(){
		dialogPanel = new JPanel();
		dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
		nameField = new JTextField("", 15);
		JPanel namePanel = new JPanel();
		namePanel
				.add(new JLabel(Utilities.getString("ADD_ACCOUNT_NAME") + ":"));
		namePanel.add(nameField);
		dialogPanel.add(namePanel);
		
		balanceField = new JTextField("", 7);
		JPanel balancePanel = new JPanel();
		balancePanel.add(new JLabel(Utilities.getString("ADD_ACCOUNT_BALANCE")
				+ ":"));
		balancePanel.add(balanceField);
		dialogPanel.add(balancePanel);
		
		hiddenBox = new JCheckBox(
				Utilities.getString("ADD_ACCOUNT_HIDDEN") + " ",
				false);
		dialogPanel.add(hiddenBox);
	}
	
	private void showDialog(String selectedName){
		int result = JOptionPane.showConfirmDialog(null, dialogPanel,
				Utilities.getString("ACCOUNT_DETAILS"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null);

		if (result == JOptionPane.OK_OPTION) {
			if (selectedName == null){
				db.addAccount(nameField.getText(), GUI.parseAmount(balanceField.getText()), hiddenBox.isSelected());
			}else{
				db.editAccount(selectedName, nameField.getText(), GUI.parseAmount(balanceField.getText()), hiddenBox.isSelected());
			}
		}
	}
	
	public void showEditDialog(String selectedName,
			String selectedBalance, boolean selectedHidden) {
		createDialog();
		nameField.setText(selectedName);
		balanceField.setText(selectedBalance);
		hiddenBox.setSelected(selectedHidden);
		showDialog(selectedName);
	}
	
	public void showAddDialog() {
		createDialog();
		showDialog(null);
	}
	
}
