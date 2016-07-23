package se.freedrikp.econview.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import se.freedrikp.econview.common.Language;
import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.database.SQLiteDatabase;
import se.freedrikp.econview.gui.panels.SearchTransactionsControlPanel;
import se.freedrikp.econview.gui.tables.TransactionsTable;

public class DeleteMenu extends JMenu {

	public DeleteMenu(final Database db) {
		super(Language.getString("MENUBAR_DELETE"));

		JMenuItem deleteAccounts = new JMenuItem(
				Language.getString("MENUBAR_DELETE_ACCOUNTS"));
		add(deleteAccounts);
		deleteAccounts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(null,
						Language.getString("PROMPT_DELETE_ACCOUNTS"),
						Language.getString("MENUBAR_DELETE_ACCOUNTS"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					db.deleteAccounts();
				}
			}
		});

		JMenuItem deleteTransactions = new JMenuItem(
				Language.getString("MENUBAR_DELETE_TRANSACTIONS"));
		add(deleteTransactions);
		deleteTransactions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(null,
						Language.getString("PROMPT_DELETE_TRANSACTIONS"),
						Language.getString("MENUBAR_DELETE_TRANSACTIONS"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					db.deleteTransactions();
				}
			}
		});

		JMenuItem deleteStoredTransactions = new JMenuItem(
				Language.getString("MENUBAR_DELETE_STORED_TRANSACTIONS"));
		add(deleteStoredTransactions);
		deleteStoredTransactions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(
						null,
						Language.getString("PROMPT_DELETE_STORED_TRANSACTIONS"),
						Language.getString("MENUBAR_DELETE_STORED_TRANSACTIONS"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					db.deleteStoredTransactions();
				}
			}
		});

		addSeparator();

		JMenuItem deleteTransactionsSearch = new JMenuItem(
				Language.getString("MENUBAR_DELETE_TRANSACTIONS_SEARCH"));
		add(deleteTransactionsSearch);
		deleteTransactionsSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TransactionsTable transactionsTable = new TransactionsTable(db);
				transactionsTable.setEnabled(false);
				SearchTransactionsControlPanel controlPanel = new SearchTransactionsControlPanel(
						db, transactionsTable);
				JPanel promptPanel = new JPanel();
				promptPanel.setLayout(new BoxLayout(promptPanel,
						BoxLayout.Y_AXIS));
				JLabel promptLabel = new JLabel(Language
						.getString("PROMPT_DELETE_TRANSACTIONS_SEARCH"));
				promptLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
				;
				promptPanel.add(promptLabel);
				promptPanel.add(Box.createVerticalStrut(10));
				promptPanel.add(controlPanel);

				if (JOptionPane.showConfirmDialog(null, promptPanel, Language
						.getString("MENUBAR_DELETE_TRANSACTIONS_SEARCH"),
						JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
					JPanel reviewPanel = new JPanel();
					reviewPanel.setLayout(new BoxLayout(reviewPanel,
							BoxLayout.Y_AXIS));
					JLabel reviewLabel = new JLabel(
							Language.getString("PROMPT_DELETE_TRANSACTIONS_SEARCH_REVIEW"));
					reviewLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
					reviewPanel.add(reviewLabel);
					reviewPanel.add(Box.createVerticalStrut(10));
					JScrollPane scrollPane = new JScrollPane();
					scrollPane.setViewportView(transactionsTable);
					reviewPanel.add(scrollPane);
					if (JOptionPane.showConfirmDialog(
							null,
							reviewPanel,
							Language.getString("MENUBAR_DELETE_TRANSACTIONS_SEARCH"),
							JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
						for (int i = 0; i < transactionsTable.getModel()
								.getRowCount(); i++) {
							db.removeTransaction((long) transactionsTable
									.getModel().getValueAt(i, 0));
						}
					}
				}
			}
		});
	}

}
