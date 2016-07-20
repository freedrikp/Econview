package se.freedrikp.econview.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.MenuSelectionManager;

import se.freedrikp.econview.common.Language;
import se.freedrikp.econview.database.SQLiteDatabase;
import se.freedrikp.econview.gui.dialogs.NormalTransactionDialog;
import se.freedrikp.econview.gui.dialogs.StoredTransactionDialog;
import se.freedrikp.econview.gui.tables.StoredTransactionsTable;

public class StoredTransactionsMenu extends JMenu implements Observer {
	private SQLiteDatabase db;
	// private TransactionDialog td;
	private JMenu editStoredTransaction;
	private JMenuItem addStoredTransaction;
	private JMenuItem removeStoredTransaction;

	public StoredTransactionsMenu(final SQLiteDatabase db) {
		super(Language.getString("MENUBAR_STORED_TRANSACTIONS"));
		this.db = db;
		db.addObserver(this);
		// td = new TransactionDialog(db);

		addStoredTransaction = new JMenuItem(
				Language.getString("MENUBAR_STORED_TRANSACTIONS_ADD"));
		addStoredTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// td.showAddStoredDialog();
				new StoredTransactionDialog(db).showAddDialog();

			}
		});

		editStoredTransaction = new JMenu(
				Language.getString("MENUBAR_STORED_TRANSACTIONS_EDIT"));

		removeStoredTransaction = new JMenuItem(
				Language.getString("MENUBAR_STORED_TRANSACTIONS_REMOVE"));
		removeStoredTransaction.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JScrollPane transactionsPane = new JScrollPane();
				StoredTransactionsTable transactionsTable = new StoredTransactionsTable(
						db);
				transactionsTable.updateStoredTransactionsList();
				// transactionsTable
				// .setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				transactionsPane.setViewportView(transactionsTable);

				int result = JOptionPane.showConfirmDialog(
						null,
						transactionsPane,
						Language.getString("MENUBAR_STORED_TRANSACTIONS_REMOVE"),
						JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					int rows[] = transactionsTable.getSelectedRows();
					if (rows.length > 0) {
						for (int row : rows) {
							row = transactionsTable.convertRowIndexToModel(row);
							db.removeStoredTransaction((long) transactionsTable
									.getModel().getValueAt(row, 0));
						}
					}
				}
			}
		});

		update(db, null);
	}

	public void update(Observable o, Object arg) {
		removeAll();
		add(addStoredTransaction);
		add(editStoredTransaction);
		add(removeStoredTransaction);
		addSeparator();
		editStoredTransaction.removeAll();
		List<String> stored = db.getStoredTransactionNames();
		for (final String transaction : stored) {
			final JMenuItem addItem = new JMenuItem(transaction);
			add(addItem);
			addItem.addMouseListener(new MouseAdapter() {

				public void mousePressed(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1) {
						for (Object[] trans : db
								.getStoredTransaction(transaction)) {
							db.addTransaction((String) trans[1],
									(double) trans[2], new Date(), transaction);
						}
					} else {
						// td.showMakeStoredDialog(transaction);
						List<Object[]> multiTransactions = db
								.getStoredTransaction(transaction);
						Object[] input = new Object[multiTransactions.size() * 2 + 2];
						int i = 0;
						for (Object[] trans : multiTransactions) {
							input[i++] = trans[1];
							input[i++] = trans[2];
						}
						input[multiTransactions.size() * 2] = transaction;
						input[multiTransactions.size() * 2 + 1] = new Date();
						new NormalTransactionDialog(db).showAddDialog(input);
					}
					MenuSelectionManager.defaultManager().clearSelectedPath();
				}
			});

			JMenuItem editItem = new JMenuItem(transaction);
			editStoredTransaction.add(editItem);
			editItem.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					// td.showEditStoredDialog(transaction);
					new StoredTransactionDialog(db)
							.showEditDialog(new Object[] { transaction });
				}
			});
		}
	}

}
