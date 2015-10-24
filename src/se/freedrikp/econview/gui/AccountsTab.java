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

public class AccountsTab extends JPanel implements Observer{

	private Database db;
	private JScrollPane accountsPane;
	private JTable accountsTable;
	private JLabel totalBalanceLabel;
	private JLabel totalIncludedBalanceLabel;
	private static final String[] accountHeader = { "Account", "Balance", "Included" };

	public AccountsTab(final Database db){
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

		JButton btnAddAccount = new JButton("Add Account");
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
				String[] accountDetails = askUserAccount(null, null, Boolean.toString(true));
				if (accountDetails != null) {
					db.addAccount(accountDetails[0],
							GUI.parseAmount(accountDetails[1]),
							Boolean.parseBoolean(accountDetails[2]));
				}
			}
		});
		accountsButtonPanel.setLayout(new BoxLayout(accountsButtonPanel,
				BoxLayout.Y_AXIS));
		accountsButtonPanel.add(btnAddAccount);

		JButton btnEditAccount = new JButton("Edit Account");
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
				String[] accountDetails = askUserAccount(
						(String) accountsTable.getModel().getValueAt(
								accountsTable.getSelectedRow(), 0),
						(String) accountsTable.getModel().getValueAt(
								accountsTable.getSelectedRow(), 1),
						(String) accountsTable.getModel().getValueAt(
								accountsTable.getSelectedRow(), 2));
				if (accountDetails != null) {
					db.editAccount((String) accountsTable.getModel()
							.getValueAt(accountsTable.getSelectedRow(), 0),
							accountDetails[0], GUI.parseAmount(accountDetails[1]),
							Boolean.parseBoolean(accountDetails[2]));
				}
			}
		});
		accountsButtonPanel.add(btnEditAccount);

		JButton btnRemoveAccount = new JButton("Remove Account");
		btnRemoveAccount.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnRemoveAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (JOptionPane.showConfirmDialog(
						null,
						"Are you sure you want to remove this account? -- "
								+ (String) accountsTable.getModel().getValueAt(
										accountsTable.getSelectedRow(), 0),
						"Remove account", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
				db.removeAccount((String) accountsTable.getModel().getValueAt(
						accountsTable.getSelectedRow(), 0));
				}
			}
		});
		accountsButtonPanel.add(btnRemoveAccount);
		
		accountsButtonPanel.add(new JSeparator());
		
		JLabel totalIncludedBalanceLabelText = new JLabel("Total Included Balance:");
		accountsButtonPanel.add(totalIncludedBalanceLabelText);
		totalIncludedBalanceLabelText.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		totalIncludedBalanceLabel = new JLabel("");
		totalIncludedBalanceLabel.setForeground(Color.RED);
		accountsButtonPanel.add(totalIncludedBalanceLabel);
		totalIncludedBalanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		accountsButtonPanel.add(new JSeparator());
		
		JLabel totalBalanceLabelText = new JLabel("Total Balance:");
		accountsButtonPanel.add(totalBalanceLabelText);
		totalBalanceLabelText.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		totalBalanceLabel = new JLabel("");
		totalBalanceLabel.setForeground(Color.RED);
		accountsButtonPanel.add(totalBalanceLabel);
		totalBalanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		update(db,null);
	}
	
	private String[] askUserAccount(String selectedName,
			String selectedBalance, String selectedIncluded) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JTextField nameField = new JTextField("", 15);
		nameField.setText(selectedName);
		JPanel namePanel = new JPanel();
		namePanel.add(new JLabel("Name:"));
		namePanel.add(nameField);
		panel.add(namePanel);

		JTextField balanceField = new JTextField("", 7);
		balanceField.setText(selectedBalance);
		JPanel balancePanel = new JPanel();
		balancePanel.add(new JLabel("Balance:"));
		balancePanel.add(balanceField);
		panel.add(balancePanel);
		JCheckBox includedBox = new JCheckBox("Include in statistics? ",Boolean.parseBoolean(selectedIncluded));
		panel.add(includedBox);

		int result = JOptionPane.showConfirmDialog(null, panel,
				"Account Details", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null);

		if (result == JOptionPane.OK_OPTION) {
			String[] details = new String[3];
			details[0] = nameField.getText();
			details[1] = balanceField.getText();
			details[2] = Boolean.toString(includedBox.isSelected());
 			return details;
		}
		return null;
	}

	
	public void update(Observable o, Object arg) {
		updateAccountList();
		totalIncludedBalanceLabel.setText((NumberFormat.getCurrencyInstance()
				.format(Double.parseDouble(db.getAccountBalanceSum(true)))));
		totalBalanceLabel.setText((NumberFormat.getCurrencyInstance()
				.format(Double.parseDouble(db.getAccountBalanceSum(false)))));
		accountsPane.getVerticalScrollBar().setValue(
				accountsPane.getVerticalScrollBar().getMaximum());
		GUI.resizeTable(accountsTable);
		repaint();
	}
	
	private void updateAccountList() {
		Model m = new Model(accountHeader, 0);
		for (String[] row : db.getAccounts()) {
			row[1] = NumberFormat.getCurrencyInstance().format(
					Double.parseDouble(row[1]));
			m.addRow(row);
		}
		accountsTable.setModel(m);
	}
}
