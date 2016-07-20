package se.freedrikp.econview.gui.tabs;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import se.freedrikp.econview.common.Common;
import se.freedrikp.econview.common.Language;
import se.freedrikp.econview.database.SQLiteDatabase;
import se.freedrikp.econview.gui.dialogs.AccountDialog;
import se.freedrikp.econview.gui.tables.AccountsTable;

public class AccountsTab extends JPanel implements Observer {

	private SQLiteDatabase db;
	private JScrollPane accountsPane;
	private AccountsTable accountsTable;
	private JLabel totalBalanceLabel;
	private JLabel totalVisibleBalanceLabel;
	private JLabel totalHiddenBalanceLabel;
	private JLabel totalBalanceLabelText;
	private JLabel totalHiddenBalanceLabelText;

	public AccountsTab(final SQLiteDatabase db) {
		super();
		this.db = db;
		db.addObserver(this);
		setLayout(new GridLayout(0, 2, 0, 0));

		// final AccountDialog accDialog = new AccountDialog(db);

		accountsPane = new JScrollPane();
		add(accountsPane);

		accountsTable = new AccountsTable(db);
		// accountsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// accountsTable.setAutoCreateRowSorter(true);
		accountsPane.setViewportView(accountsTable);

		JPanel accountsButtonPanel = new JPanel();
		add(accountsButtonPanel);

		JButton btnAddAccount = new JButton(Language.getString("ADD_ACCOUNT"));
		btnAddAccount.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnAddAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// accDialog.showAddDialog();
				new AccountDialog(db).showAddDialog();
			}
		});
		accountsButtonPanel.setLayout(new BoxLayout(accountsButtonPanel,
				BoxLayout.Y_AXIS));
		accountsButtonPanel.add(btnAddAccount);

		JButton btnEditAccount = new JButton(Language.getString("EDIT_ACCOUNT"));
		btnEditAccount.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnEditAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// accDialog.showEditDialog(
				// (String) accountsTable.getModel().getValueAt(
				// accountsTable.convertRowIndexToModel(accountsTable.getSelectedRow()),
				// 0),
				// (String) accountsTable.getModel().getValueAt(
				// accountsTable.convertRowIndexToModel(accountsTable.getSelectedRow()),
				// 1),
				// (boolean) accountsTable.getModel().getValueAt(
				// accountsTable.convertRowIndexToModel(accountsTable.getSelectedRow()),
				// 2));
				new AccountDialog(db).showEditDialog(new Object[] {
						accountsTable.getSelectedColumn(0),
						accountsTable.getSelectedColumn(1),
						accountsTable.getSelectedColumn(2) });
			}
		});
		accountsButtonPanel.add(btnEditAccount);

		JButton btnRemoveAccount = new JButton(
				Language.getString("REMOVE_ACCOUNT"));
		btnRemoveAccount.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnRemoveAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (JOptionPane.showConfirmDialog(null,
						Language.getString("REMOVE_ACCOUNT_PROMPT") + " -- "
								+ (String) accountsTable.getSelectedColumn(0),
						Language.getString("REMOVE_ACCOUNT"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					db.removeAccount((String) accountsTable
							.getSelectedColumn(0));
				}
			}
		});
		accountsButtonPanel.add(btnRemoveAccount);

		accountsButtonPanel.add(new JSeparator());

		JLabel totalVisibleBalanceLabelText = new JLabel(
				Language.getString("TOTAL_VISIBLE_BALANCE") + ":");
		accountsButtonPanel.add(totalVisibleBalanceLabelText);
		totalVisibleBalanceLabelText.setAlignmentX(Component.CENTER_ALIGNMENT);

		totalVisibleBalanceLabel = new JLabel("");
		totalVisibleBalanceLabel.setForeground(Color.RED);
		accountsButtonPanel.add(totalVisibleBalanceLabel);
		totalVisibleBalanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		accountsButtonPanel.add(new JSeparator());

		totalBalanceLabelText = new JLabel(Language.getString("TOTAL_BALANCE")
				+ ":");
		accountsButtonPanel.add(totalBalanceLabelText);
		totalBalanceLabelText.setAlignmentX(Component.CENTER_ALIGNMENT);

		totalBalanceLabel = new JLabel("");
		totalBalanceLabel.setForeground(Color.RED);
		accountsButtonPanel.add(totalBalanceLabel);
		totalBalanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		accountsButtonPanel.add(new JSeparator());

		totalHiddenBalanceLabelText = new JLabel(
				Language.getString("TOTAL_HIDDEN_BALANCE") + ":");
		accountsButtonPanel.add(totalHiddenBalanceLabelText);
		totalHiddenBalanceLabelText.setAlignmentX(Component.CENTER_ALIGNMENT);

		totalHiddenBalanceLabel = new JLabel("");
		totalHiddenBalanceLabel.setForeground(Color.RED);
		accountsButtonPanel.add(totalHiddenBalanceLabel);
		totalHiddenBalanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		update(db, null);
	}

	public void update(Observable o, Object arg) {
		accountsTable.updateAccountList();
		Calendar cal = Common.getFlattenCalendar(null);
		totalVisibleBalanceLabel.setText(NumberFormat.getCurrencyInstance()
				.format(db.getVisibleAccountBalanceSum(cal.getTime())));
		totalBalanceLabel.setText(NumberFormat.getCurrencyInstance().format(
				db.getTotalAccountBalanceSum(cal.getTime())));
		totalHiddenBalanceLabel.setText(NumberFormat.getCurrencyInstance()
				.format(db.getHiddenAccountBalanceSum(cal.getTime())));
		totalBalanceLabel.setVisible(db.getShowHidden());
		totalHiddenBalanceLabel.setVisible(db.getShowHidden());
		totalBalanceLabelText.setVisible(db.getShowHidden());
		totalHiddenBalanceLabelText.setVisible(db.getShowHidden());
		accountsPane.getVerticalScrollBar().setValue(
				accountsPane.getVerticalScrollBar().getMaximum());
		// GUI.resizeTable(accountsTable);
		repaint();
	}
}
