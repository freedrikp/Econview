package se.freedrikp.econview.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.GUI.Model;

import com.toedter.calendar.JDateChooser;

public class TransactionsTab extends JPanel implements Observer {

	private Database db;
	private JScrollPane transactionsPane;
	private JTable transactionsTable;
	private static final String[] transactionHeader = {
			Utilities.getString("TRANSACTION_HEADER_ID"),
			Utilities.getString("TRANSACTION_HEADER_ACCOUNT"),
			Utilities.getString("TRANSACTION_HEADER_AMOUNT"),
			Utilities.getString("TRANSACTION_HEADER_DATE"),
			Utilities.getString("TRANSACTION_HEADER_COMMENT") };
	private JLabel oldestDate;
	private JLabel newestDate;
	private JLabel numTransactions;
	private JLabel numDeposits;
	private JLabel numWithdrawals;
	private final SimpleDateFormat dateFormat;

	public TransactionsTab(final Database db) {
		super();
		this.db = db;
		db.addObserver(this);
		dateFormat = new SimpleDateFormat(
				Utilities.getConfig("FULL_DATE_FORMAT"));
		setLayout(new GridLayout(0, 2, 0, 0));

		transactionsPane = new JScrollPane();
		add(transactionsPane);

		transactionsTable = new JTable();
		transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// transactionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		transactionsPane.setViewportView(transactionsTable);

		JPanel transactionsButtonPanel = new JPanel();
		add(transactionsButtonPanel);

		JButton btnAddTransaction = new JButton(
				Utilities.getString("ADD_TRANSACTION"));
		btnAddTransaction.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnAddTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object[] transactionDetails = TransactionDialog.showAddDialog(db
						.getAccountNames().toArray());

				if (transactionDetails != null) {
					try {
						int size = transactionDetails.length;
						for (int i = 0; i < size - 2; i+=2){
							db.addTransaction(
									(String) transactionDetails[i],
									GUI.parseAmount((String) transactionDetails[i+1]),
									(Date) transactionDetails[size-2],
									(String) transactionDetails[size-1]);
						}
					} catch (NumberFormatException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		transactionsButtonPanel.setLayout(new BoxLayout(
				transactionsButtonPanel, BoxLayout.Y_AXIS));
		transactionsButtonPanel.add(btnAddTransaction);

		JButton btnEditTransaction = new JButton(
				Utilities.getString("EDIT_TRANSACTION"));
		btnEditTransaction.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnEditTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Object[] transactionDetails = TransactionDialog.showEditDialog(
							db.getAccountNames().toArray(),
							(String) transactionsTable.getModel().getValueAt(
									transactionsTable.getSelectedRow(), 1),
							(String) transactionsTable.getModel().getValueAt(
									transactionsTable.getSelectedRow(), 2),
							(Date) dateFormat.parse((String) transactionsTable
									.getModel().getValueAt(
											transactionsTable.getSelectedRow(),
											3)),
							(String) transactionsTable.getModel().getValueAt(
									transactionsTable.getSelectedRow(), 4));
					if (transactionDetails != null) {
						int size = transactionDetails.length;
						for(int i = 0; i < size-2; i+=2){
							if (i < 2){
								db.editTransaction(
										(long) transactionsTable.getModel().getValueAt(
												transactionsTable.getSelectedRow(), 0),
												(String) transactionDetails[i],
												GUI.parseAmount((String) transactionDetails[i+1]),
												(Date) transactionDetails[size-2],
												(String) transactionDetails[size-1]);
							}else{
								db.addTransaction((String) transactionDetails[i],
												GUI.parseAmount((String) transactionDetails[i+1]),
												(Date) transactionDetails[size-2],
												(String) transactionDetails[size-1]);
							}
						}
					}
				} catch (NumberFormatException | ParseException e1) {
					e1.printStackTrace();
				}

			}
		});
		transactionsButtonPanel.add(btnEditTransaction);

		JButton btnRemoveTransaction = new JButton(
				Utilities.getString("REMOVE_TRANSACTION"));
		btnRemoveTransaction.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnRemoveTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(
						null,
						Utilities.getString("REMOVE_TRANSACTION_PROMPT")
								+ " -- "
								+ transactionsTable.getModel().getValueAt(
										transactionsTable.getSelectedRow(), 0),
						Utilities.getString("REMOVE_TRANSACTION"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					db.removeTransaction((long) transactionsTable.getModel()
							.getValueAt(transactionsTable.getSelectedRow(), 0));
				}
			}
		});
		transactionsButtonPanel.add(btnRemoveTransaction);
		JPanel statisticsPanel = new JPanel();
		statisticsPanel.setLayout(new BoxLayout(statisticsPanel,
				BoxLayout.Y_AXIS));

		JPanel oldestDatePanel = new JPanel();
		oldestDatePanel.add(new JLabel(Utilities
				.getString("OLDEST_TRANSACTION_DATE") + ": "));
		oldestDate = new JLabel();
		oldestDate.setAlignmentX(Component.CENTER_ALIGNMENT);
		oldestDate.setForeground(Color.RED);
		oldestDatePanel.add(oldestDate);
		statisticsPanel.add(oldestDatePanel);

		JPanel newestDatePanel = new JPanel();
		newestDatePanel.add(new JLabel(Utilities
				.getString("NEWEST_TRANSACTION_DATE") + ": "));
		newestDate = new JLabel();
		newestDate.setAlignmentX(Component.CENTER_ALIGNMENT);
		newestDate.setForeground(Color.RED);
		newestDatePanel.add(newestDate);
		statisticsPanel.add(newestDatePanel);

		JPanel numTransactionsPanel = new JPanel();
		numTransactionsPanel.add(new JLabel(Utilities
				.getString("NUMBER_OF_TRANSACTIONS") + ": "));
		numTransactions = new JLabel();
		numTransactions.setAlignmentX(Component.CENTER_ALIGNMENT);
		numTransactions.setForeground(Color.RED);
		numTransactionsPanel.add(numTransactions);
		statisticsPanel.add(numTransactionsPanel);

		JPanel numDepositsPanel = new JPanel();
		numDepositsPanel.add(new JLabel(Utilities
				.getString("NUMBER_OF_DEPOSITS") + ": "));
		numDeposits = new JLabel();
		numDeposits.setAlignmentX(Component.CENTER_ALIGNMENT);
		numDeposits.setForeground(Color.RED);
		numDepositsPanel.add(numDeposits);
		statisticsPanel.add(numDepositsPanel);

		JPanel numWithDrawalsPanel = new JPanel();
		numWithDrawalsPanel.add(new JLabel(Utilities
				.getString("NUMBER_OF_WITHDRAWALS") + ": "));
		numWithdrawals = new JLabel();
		numWithdrawals.setAlignmentX(Component.CENTER_ALIGNMENT);
		numWithdrawals.setForeground(Color.RED);
		numWithDrawalsPanel.add(numWithdrawals);
		statisticsPanel.add(numWithDrawalsPanel);

		transactionsButtonPanel.add(statisticsPanel);

		update(db, null);
	}

	public void update(Observable o, Object arg) {
		updateTransactionList();
		transactionsPane.getVerticalScrollBar().setValue(
				transactionsPane.getVerticalScrollBar().getMaximum());
		GUI.resizeTable(transactionsTable);
		Date oldest = db.getOldestTransactionDate();
		if (oldest != null) {
			oldestDate.setText(dateFormat.format(oldest));
		} else {
			oldestDate.setText(Utilities.getString("UNKNOWN"));
		}
		Date newest = db.getNewestTransactionDate();
		if (newest != null) {
			newestDate.setText(dateFormat.format(newest));
		} else {
			newestDate.setText(Utilities.getString("UNKNOWN"));
		}
		numTransactions.setText(Long.toString(db.getNumberOfTransactions()));
		numDeposits.setText(Long.toString(db.getNumberOfDeposits()));
		numWithdrawals.setText(Long.toString(db.getNumberOfWithdrawals()));
		repaint();
	}

	private void updateTransactionList() {
		Model m = new Model(transactionHeader, 0);
		for (Object[] row : db.getTransactions()) {
			row[2] = NumberFormat.getCurrencyInstance().format(row[2]);
			row[3] = dateFormat.format(row[3]);
			m.addRow(row);
		}
		transactionsTable.setModel(m);
	}
}
