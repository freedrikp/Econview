package se.freedrikp.econview.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.GUI;
import se.freedrikp.econview.gui.GUI.Model;
import se.freedrikp.econview.gui.Language;
import se.freedrikp.econview.gui.dialogs.NormalTransactionDialog;
import se.freedrikp.econview.gui.dialogs.StoredTransactionDialog;

public class StoredTransactionsMenu extends JMenu implements Observer {
	private Database db;
	// private TransactionDialog td;
	private JMenu editStoredTransaction;
	private static final String[] transactionHeader = {
			Language.getString("TRANSACTION_HEADER_ID"),
			Language.getString("TRANSACTION_HEADER_ACCOUNT"),
			Language.getString("TRANSACTION_HEADER_AMOUNT"),
			Language.getString("TRANSACTION_HEADER_COMMENT") };
	private JMenuItem addStoredTransaction;
	private JMenuItem removeStoredTransaction;

	public StoredTransactionsMenu(final Database db) {
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
				JTable transactionsTable = new JTable();
				transactionsTable
						.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
				transactionsPane.setViewportView(transactionsTable);

				Model m = new Model(transactionHeader, 0);
				for (Object[] row : db.getStoredTransactions()) {
					row[2] = NumberFormat.getCurrencyInstance().format(row[2]);
					m.addRow(row);
				}
				transactionsTable.setModel(m);
				TableRowSorter<Model> sorter = new TableRowSorter<Model>(m);
				transactionsTable.setRowSorter(sorter);
				sorter.setComparator(0, new Comparator<Long>() {
					public int compare(Long o1, Long o2) {
						return o1.compareTo(o2);
					}
				});
				sorter.setComparator(2, new Comparator<String>() {
					public int compare(String o1, String o2) {
						return Double.compare(GUI.parseAmount(o1),
								GUI.parseAmount(o2));
					}
				});

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
