package se.freedrikp.econview.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.GUI.Model;

public class AccountsTab extends JPanel implements Observer {

	private Database db;
	private JScrollPane accountsPane;
	private JTable accountsTable;
	private JLabel totalBalanceLabel;
	private JLabel totalIncludedBalanceLabel;
	private JLabel totalNotIncludedBalanceLabel;
	private static final String[] accountHeader = {
			Utilities.getString("ACCOUNT_HEADER_ACCOUNT"),
			Utilities.getString("ACCOUNT_HEADER_BALANCE"),
			Utilities.getString("ACCOUNT_HEADER_INCLUDED") };
	private JLabel totalBalanceLabelText;
	private JLabel totalNotIncludedBalanceLabelText;

	public AccountsTab(final Database db) {
		super();
		this.db = db;
		db.addObserver(this);
		setLayout(new GridLayout(0, 2, 0, 0));

		accountsPane = new JScrollPane();
		add(accountsPane);

		accountsTable = new JTable();
		accountsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		accountsPane.setViewportView(accountsTable);

		JPanel accountsButtonPanel = new JPanel();
		add(accountsButtonPanel);

		JButton btnAddAccount = new JButton(Utilities.getString("ADD_ACCOUNT"));
		btnAddAccount.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnAddAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// String accountName = askUser("Account Name",
				// "What is the name of the new account?", null, null);
				// if (accountName == null) {
				// return;
				// }
				// String accountBalance = askUser("Account Balance",
				// "What is the balance of the new account?", null, null);
				// if (accountBalance == null) {
				// return;
				// }
				// db.addAccount(accountName,
				// Double.parseDouble(accountBalance));
				Object[] accountDetails = askUserAccount(null, null, true);
				if (accountDetails != null) {
					db.addAccount((String)accountDetails[0],
							GUI.parseAmount((String)accountDetails[1]),
							(boolean)accountDetails[2]);
				}
			}
		});
		accountsButtonPanel.setLayout(new BoxLayout(accountsButtonPanel,
				BoxLayout.Y_AXIS));
		accountsButtonPanel.add(btnAddAccount);

		JButton btnEditAccount = new JButton(
				Utilities.getString("EDIT_ACCOUNT"));
		btnEditAccount.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnEditAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// String accountName = askUser(
				// "Account Name",
				// "What is the name of the account?",
				// (String) accountsTable.getModel().getValueAt(
				// accountsTable.getSelectedRow(), 0), null);
				// if (accountName == null) {
				// return;
				// }
				// String accountBalance = askUser(
				// "Account Balance",
				// "What is the balance of the account?",
				// (String) accountsTable.getModel().getValueAt(
				// accountsTable.getSelectedRow(), 1), null);
				// if (accountBalance == null) {
				// return;
				// }
				// db.editAccount(
				// (String) accountsTable.getModel().getValueAt(
				// accountsTable.getSelectedRow(), 0),
				// accountName, Double.parseDouble(accountBalance));
				Object[] accountDetails = askUserAccount(
						(String) accountsTable.getModel().getValueAt(
								accountsTable.getSelectedRow(), 0),
						(String) accountsTable.getModel().getValueAt(
								accountsTable.getSelectedRow(), 1),
						(boolean) accountsTable.getModel().getValueAt(
								accountsTable.getSelectedRow(), 2));
				if (accountDetails != null) {
					db.editAccount((String) accountsTable.getModel()
							.getValueAt(accountsTable.getSelectedRow(), 0),
							(String)accountDetails[0], GUI
									.parseAmount((String)accountDetails[1]), (boolean)accountDetails[2]);
				}
			}
		});
		accountsButtonPanel.add(btnEditAccount);

		JButton btnRemoveAccount = new JButton(
				Utilities.getString("REMOVE_ACCOUNT"));
		btnRemoveAccount.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnRemoveAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (JOptionPane.showConfirmDialog(
						null,
						Utilities.getString("REMOVE_ACCOUNT_PROMPT")
								+ " -- "
								+ (String) accountsTable.getModel().getValueAt(
										accountsTable.getSelectedRow(), 0),
						Utilities.getString("REMOVE_ACCOUNT"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					db.removeAccount((String) accountsTable.getModel()
							.getValueAt(accountsTable.getSelectedRow(), 0));
				}
			}
		});
		accountsButtonPanel.add(btnRemoveAccount);

		accountsButtonPanel.add(new JSeparator());

		JLabel totalIncludedBalanceLabelText = new JLabel(
				Utilities.getString("TOTAL_INCLUDED_BALANCE") + ":");
		accountsButtonPanel.add(totalIncludedBalanceLabelText);
		totalIncludedBalanceLabelText.setAlignmentX(Component.CENTER_ALIGNMENT);

		totalIncludedBalanceLabel = new JLabel("");
		totalIncludedBalanceLabel.setForeground(Color.RED);
		accountsButtonPanel.add(totalIncludedBalanceLabel);
		totalIncludedBalanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		accountsButtonPanel.add(new JSeparator());

		totalBalanceLabelText = new JLabel(Utilities.getString("TOTAL_BALANCE")
				+ ":");
		accountsButtonPanel.add(totalBalanceLabelText);
		totalBalanceLabelText.setAlignmentX(Component.CENTER_ALIGNMENT);

		totalBalanceLabel = new JLabel("");
		totalBalanceLabel.setForeground(Color.RED);
		accountsButtonPanel.add(totalBalanceLabel);
		totalBalanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		accountsButtonPanel.add(new JSeparator());

		totalNotIncludedBalanceLabelText = new JLabel(
				Utilities.getString("TOTAL_NOT_INCLUDED_BALANCE") + ":");
		accountsButtonPanel.add(totalNotIncludedBalanceLabelText);
		totalNotIncludedBalanceLabelText
				.setAlignmentX(Component.CENTER_ALIGNMENT);

		totalNotIncludedBalanceLabel = new JLabel("");
		totalNotIncludedBalanceLabel.setForeground(Color.RED);
		accountsButtonPanel.add(totalNotIncludedBalanceLabel);
		totalNotIncludedBalanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		update(db, null);
	}

	private Object[] askUserAccount(String selectedName,
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
		JCheckBox includedBox = new JCheckBox(
				Utilities.getString("ADD_ACCOUNT_INCLUDE") + " ",
				selectedIncluded);
		panel.add(includedBox);

		int result = JOptionPane.showConfirmDialog(null, panel,
				Utilities.getString("ACCOUNT_DETAILS"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null);

		if (result == JOptionPane.OK_OPTION) {
			Object[] details = new Object[3];
			details[0] = nameField.getText();
			details[1] = balanceField.getText();
			details[2] = includedBox.isSelected();
			return details;
		}
		return null;
	}

	public void update(Observable o, Object arg) {
		updateAccountList();
		totalIncludedBalanceLabel.setText(NumberFormat.getCurrencyInstance()
				.format(db.getIncludedAccountBalanceSum()));
		totalBalanceLabel.setText(NumberFormat.getCurrencyInstance().format(
				db.getTotalAccountBalanceSum()));
		totalNotIncludedBalanceLabel.setText(NumberFormat.getCurrencyInstance()
				.format(db.getNotIncludedAccountBalanceSum()));
		totalBalanceLabel.setVisible(!db.getOnlyIncluded());
		totalNotIncludedBalanceLabel.setVisible(!db.getOnlyIncluded());
		totalBalanceLabelText.setVisible(!db.getOnlyIncluded());
		totalNotIncludedBalanceLabelText.setVisible(!db.getOnlyIncluded());
		accountsPane.getVerticalScrollBar().setValue(
				accountsPane.getVerticalScrollBar().getMaximum());
		GUI.resizeTable(accountsTable);
		repaint();
	}

	private void updateAccountList() {
		Model m = new Model(accountHeader, 0);
		for (Object[] row : db.getAccounts()) {
			row[1] = NumberFormat.getCurrencyInstance().format(row[1]);
			m.addRow(row);
		}
		accountsTable.setModel(m);
	}
}
