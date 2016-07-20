package se.freedrikp.econview.gui.dialogs;

import java.text.NumberFormat;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import se.freedrikp.econview.common.Common;
import se.freedrikp.econview.common.Language;
import se.freedrikp.econview.database.SQLiteDatabase;
import se.freedrikp.econview.gui.frames.MainFrame;

public class AccountDialog extends DatabaseDialog {
	private JTextField nameField;
	private JTextField balanceField;
	private JCheckBox hiddenBox;
	private String oldName;

	public AccountDialog(SQLiteDatabase db) {
		super(db, Language.getString("ACCOUNT_DETAILS"), Language
				.getString("ADD_ACCOUNT_CHAIN"));
	}

	protected JComponent createDialog(JPanel dialogPanel) {
		nameField = new JTextField("", 15);
		JPanel namePanel = new JPanel();
		namePanel.add(new JLabel(Language.getString("ADD_ACCOUNT_NAME") + ":"));
		namePanel.add(nameField);
		dialogPanel.add(namePanel);

		balanceField = new JTextField("", 7);
		JPanel balancePanel = new JPanel();
		balancePanel.add(new JLabel(Language.getString("ADD_ACCOUNT_BALANCE")
				+ ":"));
		balancePanel.add(balanceField);
		dialogPanel.add(balancePanel);

		hiddenBox = new JCheckBox(Language.getString("ADD_ACCOUNT_HIDDEN")
				+ " ", false);
		dialogPanel.add(hiddenBox);
		return dialogPanel;
	}

	protected void doAddDatabaseAction() {
		db.addAccount(nameField.getText(),
				MainFrame.parseAmount(balanceField.getText()),
				hiddenBox.isSelected());
	}

	protected void doEditDatabaseAction() {
		db.editAccount(oldName, nameField.getText(), MainFrame
				.parseAmount(balanceField.getText()), hiddenBox.isSelected(),
				Common.getFlattenCalendar(null).getTime());
	}

	protected void setEditSpecifics(JPanel dialogPanel, Object[] input) {
		oldName = (String) input[0];
		nameField.setText((String) input[0]);
		balanceField.setText((String) input[1]);
		hiddenBox.setSelected((boolean) input[2]);
	}

	protected void setAddSpecifics(JPanel dialogPanel) {
	}

	protected void setAddSpecifics(JPanel dialogPanel, Object[] input) {
		nameField.setText((String) input[0]);
		balanceField.setText(NumberFormat.getCurrencyInstance().format(
				(double) input[1]));
		hiddenBox.setSelected((boolean) input[2]);
	}

	// private JTextField nameField;
	// private JTextField balanceField;
	// private JCheckBox hiddenBox;
	// private JPanel dialogPanel;
	// private Database db;
	//
	// public AccountDialog(Database db) {
	// this.db = db;
	// }
	//
	// private void createDialog() {
	// dialogPanel = new JPanel();
	// dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
	// nameField = new JTextField("", 15);
	// JPanel namePanel = new JPanel();
	// namePanel
	// .add(new JLabel(Utilities.getString("ADD_ACCOUNT_NAME") + ":"));
	// namePanel.add(nameField);
	// dialogPanel.add(namePanel);
	//
	// balanceField = new JTextField("", 7);
	// JPanel balancePanel = new JPanel();
	// balancePanel.add(new JLabel(Utilities.getString("ADD_ACCOUNT_BALANCE")
	// + ":"));
	// balancePanel.add(balanceField);
	// dialogPanel.add(balancePanel);
	//
	// hiddenBox = new JCheckBox(Utilities.getString("ADD_ACCOUNT_HIDDEN")
	// + " ", false);
	// dialogPanel.add(hiddenBox);
	//
	// dialogPanel.add(new JLabel(Utilities.getString("ADD_ACCOUNT_CHAIN")));
	// }
	//
	// private boolean showDialog(String selectedName) {
	// int result = JOptionPane.showConfirmDialog(null, dialogPanel,
	// Utilities.getString("ACCOUNT_DETAILS"),
	// JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
	// null);
	//
	// if (result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION)
	// {
	// if (selectedName == null) {
	// db.addAccount(nameField.getText(),
	// GUI.parseAmount(balanceField.getText()),
	// hiddenBox.isSelected());
	// } else {
	// db.editAccount(selectedName, nameField.getText(),
	// GUI.parseAmount(balanceField.getText()),
	// hiddenBox.isSelected());
	// }
	// }
	// return result == JOptionPane.YES_OPTION;
	// }
	//
	// public void showEditDialog(String selectedName, String selectedBalance,
	// boolean selectedHidden) {
	// createDialog();
	// nameField.setText(selectedName);
	// balanceField.setText(selectedBalance);
	// hiddenBox.setSelected(selectedHidden);
	// if (showDialog(selectedName)) {
	// showAddDialog();
	// }
	//
	// }
	//
	// public void showAddDialog() {
	// boolean chain;
	// do {
	// createDialog();
	// chain = showDialog(null);
	// } while (chain);
	// }

}
