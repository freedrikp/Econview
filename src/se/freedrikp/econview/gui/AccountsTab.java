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
	private JLabel totalVisibleBalanceLabel;
	private JLabel totalHiddenBalanceLabel;
	private static final String[] accountHeader = {
			Utilities.getString("ACCOUNT_HEADER_ACCOUNT"),
			Utilities.getString("ACCOUNT_HEADER_BALANCE"),
			Utilities.getString("ACCOUNT_HEADER_HIDDEN") };
	private JLabel totalBalanceLabelText;
	private JLabel totalHiddenBalanceLabelText;

	public AccountsTab(final Database db) {
		super();
		this.db = db;
		db.addObserver(this);
		setLayout(new GridLayout(0, 2, 0, 0));
		
		final AccountDialog accDialog = new AccountDialog(db);

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
				accDialog.showAddDialog();
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
				accDialog.showEditDialog(
						(String) accountsTable.getModel().getValueAt(
								accountsTable.getSelectedRow(), 0),
						(String) accountsTable.getModel().getValueAt(
								accountsTable.getSelectedRow(), 1),
						(boolean) accountsTable.getModel().getValueAt(
								accountsTable.getSelectedRow(), 2));
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

		JLabel totalVisibleBalanceLabelText = new JLabel(
				Utilities.getString("TOTAL_VISIBLE_BALANCE") + ":");
		accountsButtonPanel.add(totalVisibleBalanceLabelText);
		totalVisibleBalanceLabelText.setAlignmentX(Component.CENTER_ALIGNMENT);

		totalVisibleBalanceLabel = new JLabel("");
		totalVisibleBalanceLabel.setForeground(Color.RED);
		accountsButtonPanel.add(totalVisibleBalanceLabel);
		totalVisibleBalanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

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

		totalHiddenBalanceLabelText = new JLabel(
				Utilities.getString("TOTAL_HIDDEN_BALANCE") + ":");
		accountsButtonPanel.add(totalHiddenBalanceLabelText);
		totalHiddenBalanceLabelText
				.setAlignmentX(Component.CENTER_ALIGNMENT);

		totalHiddenBalanceLabel = new JLabel("");
		totalHiddenBalanceLabel.setForeground(Color.RED);
		accountsButtonPanel.add(totalHiddenBalanceLabel);
		totalHiddenBalanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		update(db, null);
	}

	public void update(Observable o, Object arg) {
		updateAccountList();
		totalVisibleBalanceLabel.setText(NumberFormat.getCurrencyInstance()
				.format(db.getVisibleAccountBalanceSum()));
		totalBalanceLabel.setText(NumberFormat.getCurrencyInstance().format(
				db.getTotalAccountBalanceSum()));
		totalHiddenBalanceLabel.setText(NumberFormat.getCurrencyInstance()
				.format(db.getHiddenAccountBalanceSum()));
		totalBalanceLabel.setVisible(db.getShowHidden());
		totalHiddenBalanceLabel.setVisible(db.getShowHidden());
		totalBalanceLabelText.setVisible(db.getShowHidden());
		totalHiddenBalanceLabelText.setVisible(db.getShowHidden());
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
