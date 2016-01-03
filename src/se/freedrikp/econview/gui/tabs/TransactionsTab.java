package se.freedrikp.econview.gui.tabs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.GUI;
import se.freedrikp.econview.gui.GUI.Model;
import se.freedrikp.econview.gui.Utilities;
import se.freedrikp.econview.gui.dialogs.TransactionDialog;

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
	private JDateChooser toDateChooser;
	private JDateChooser fromDateChooser;
	private JCheckBox allAccounts;
	private JCheckBox[] selectedAccounts;
	private JPanel transactionsViewAccountPanel;

	public TransactionsTab(final Database db) {
		super();
		this.db = db;
		db.addObserver(this);
		dateFormat = new SimpleDateFormat(
				Utilities.getConfig("FULL_DATE_FORMAT"));
		// setLayout(new GridLayout(0, 3, 0, 0));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		final TransactionDialog transDialog = new TransactionDialog(db);
		transactionsPane = new JScrollPane();
		add(transactionsPane);

		transactionsTable = new JTable();
		transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// transactionsTable.setAutoCreateRowSorter(true);
		// transactionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		transactionsPane.setViewportView(transactionsTable);

		JPanel transactionsViewPanel = new JPanel();
		transactionsViewPanel.setLayout(new BoxLayout(transactionsViewPanel,
				BoxLayout.Y_AXIS));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		fromDateChooser = new JDateChooser(cal.getTime(),
				Utilities.getConfig("FULL_DATE_FORMAT"));
		fromDateChooser.setMaximumSize(new Dimension(Integer.parseInt(Utilities
				.getConfig("DATE_FIELD_WIDTH")), Integer.parseInt(Utilities
				.getConfig("DATE_FIELD_HEIGHT"))));
		transactionsViewPanel.add(fromDateChooser);
		JLabel dateSepLabel = new JLabel("<->");
		transactionsViewPanel.add(dateSepLabel);
		toDateChooser = new JDateChooser(Calendar.getInstance().getTime(),
				Utilities.getConfig("FULL_DATE_FORMAT"));
		toDateChooser.setMaximumSize(new Dimension(Integer.parseInt(Utilities
				.getConfig("DATE_FIELD_WIDTH")), Integer.parseInt(Utilities
				.getConfig("DATE_FIELD_HEIGHT"))));
		transactionsViewPanel.add(toDateChooser);

		allAccounts = new JCheckBox(Utilities.getString("ALL_ACCOUNTS"), true);

		allAccounts.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean select = e.getStateChange() == ItemEvent.SELECTED;
				for (JCheckBox checkBox : selectedAccounts) {
					checkBox.setSelected(select);
				}
			}

		});

		transactionsViewAccountPanel = new JPanel();
		transactionsViewAccountPanel.setLayout(new BoxLayout(
				transactionsViewAccountPanel, BoxLayout.Y_AXIS));
		transactionsViewPanel
				.add(new JScrollPane(transactionsViewAccountPanel));
		JButton updateTransactionsView = new JButton(
				Utilities.getString("UPDATE_TRANSACTIONS_VIEW"));
		updateTransactionsView.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update(db, null);
			}
		});
		transactionsViewPanel.add(updateTransactionsView);
		updateTransactionsView.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		dateSepLabel.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		add(transactionsViewPanel);

		JPanel transactionsButtonPanel = new JPanel();
		add(transactionsButtonPanel);

		JButton btnAddTransaction = new JButton(
				Utilities.getString("ADD_TRANSACTION"));
		btnAddTransaction.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnAddTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				transDialog.showAddDialog();
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
					transDialog.showEditDialog(
							(Date) dateFormat.parse((String) transactionsTable
									.getModel().getValueAt(
											transactionsTable.convertRowIndexToModel(transactionsTable.getSelectedRow()),
											3)),
							(String) transactionsTable.getModel().getValueAt(
									transactionsTable.convertRowIndexToModel(transactionsTable.getSelectedRow()), 4));
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
										transactionsTable.convertRowIndexToModel(transactionsTable.getSelectedRow()), 0),
						Utilities.getString("REMOVE_TRANSACTION"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					db.removeTransaction((long) transactionsTable.getModel()
							.getValueAt(transactionsTable.convertRowIndexToModel(transactionsTable.getSelectedRow()), 0));
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
		validate();
		repaint();
	}

	private void updateTransactionList() {
		transactionsViewAccountPanel.removeAll();
		List<String> accounts = db.getAccountNames();
		HashSet<String> oldSelectedAccounts = new HashSet<String>();
		if (selectedAccounts != null) {
			for (JCheckBox checkBox : selectedAccounts) {
				if (checkBox.isSelected()) {
					oldSelectedAccounts.add(checkBox.getText());
				}
			}
		}

		transactionsViewAccountPanel.add(allAccounts);
		transactionsViewAccountPanel.add(new JSeparator());
		if (selectedAccounts == null) {
			selectedAccounts = new JCheckBox[accounts.size()];
			for (int i = 0; i < accounts.size(); i++) {
				String account = accounts.get(i);
				selectedAccounts[i] = new JCheckBox(account, true);
				oldSelectedAccounts.add(account);
				transactionsViewAccountPanel.add(selectedAccounts[i]);
			}
		} else {
			selectedAccounts = new JCheckBox[accounts.size()];
			for (int i = 0; i < accounts.size(); i++) {
				String account = accounts.get(i);
				selectedAccounts[i] = new JCheckBox(account,
						oldSelectedAccounts.contains(account));
				transactionsViewAccountPanel.add(selectedAccounts[i]);
			}
		}

		Model m = new Model(transactionHeader, 0);
		for (Object[] row : db.getTransactions(fromDateChooser.getDate(),
				toDateChooser.getDate(), oldSelectedAccounts)) {
			row[2] = NumberFormat.getCurrencyInstance().format(row[2]);
			row[3] = dateFormat.format(row[3]);
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
				return Double.compare(GUI.parseAmount(o1), GUI.parseAmount(o2));
			}
		});
	}
}
