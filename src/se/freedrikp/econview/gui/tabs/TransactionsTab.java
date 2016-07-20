package se.freedrikp.econview.gui.tabs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import se.freedrikp.econview.common.Configuration;
import se.freedrikp.econview.common.Language;
import se.freedrikp.econview.database.SQLiteDatabase;
import se.freedrikp.econview.gui.dialogs.NormalTransactionDialog;
import se.freedrikp.econview.gui.panels.AccountSelectorPanel;
import se.freedrikp.econview.gui.tables.TransactionsTable;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JSpinnerDateEditor;

public class TransactionsTab extends JPanel implements Observer {

	private SQLiteDatabase db;
	private JScrollPane transactionsPane;
	private TransactionsTable transactionsTable;
	private JLabel oldestDate;
	private JLabel newestDate;
	private JLabel numTransactions;
	private JLabel numDeposits;
	private JLabel numWithdrawals;
	private final SimpleDateFormat dateFormat;
	private JDateChooser toDateChooser;
	private JDateChooser fromDateChooser;
	private AccountSelectorPanel accountSelectorPanel;

	public TransactionsTab(final SQLiteDatabase db) {
		super();
		this.db = db;
		db.addObserver(this);
		dateFormat = new SimpleDateFormat(
				Configuration.getString("FULL_DATE_FORMAT"));
		// setLayout(new GridLayout(0, 3, 0, 0));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		// final TransactionDialog transDialog = new TransactionDialog(db);
		transactionsPane = new JScrollPane();
		add(transactionsPane);

		transactionsTable = new TransactionsTable(db);
		// transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		// transactionsTable.setAutoCreateRowSorter(true);
		// transactionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		transactionsPane.setViewportView(transactionsTable);

		JPanel transactionsViewPanel = new JPanel();
		transactionsViewPanel.setLayout(new BoxLayout(transactionsViewPanel,
				BoxLayout.Y_AXIS));
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		fromDateChooser = new JDateChooser(new JSpinnerDateEditor());
		fromDateChooser.setDateFormatString(Configuration
				.getString("FULL_DATE_FORMAT"));
		fromDateChooser.setDate(cal.getTime());
		fromDateChooser.setMaximumSize(new Dimension(Integer
				.parseInt(Configuration.getString("DATE_FIELD_WIDTH")), Integer
				.parseInt(Configuration.getString("DATE_FIELD_HEIGHT"))));
		transactionsViewPanel.add(fromDateChooser);
		JLabel dateSepLabel = new JLabel("<->");
		transactionsViewPanel.add(dateSepLabel);
		toDateChooser = new JDateChooser(new JSpinnerDateEditor());
		toDateChooser.setDateFormatString(Configuration
				.getString("FULL_DATE_FORMAT"));
		toDateChooser.setDate(Calendar.getInstance().getTime());
		toDateChooser.setMaximumSize(new Dimension(Integer
				.parseInt(Configuration.getString("DATE_FIELD_WIDTH")), Integer
				.parseInt(Configuration.getString("DATE_FIELD_HEIGHT"))));
		transactionsViewPanel.add(toDateChooser);

		accountSelectorPanel = new AccountSelectorPanel(db, true, false);

		transactionsViewPanel.add(new JScrollPane(accountSelectorPanel));
		JButton updateTransactionsView = new JButton(
				Language.getString("UPDATE_TRANSACTIONS_VIEW"));
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
				Language.getString("ADD_TRANSACTION"));
		btnAddTransaction.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnAddTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// transDialog.showAddDialog();
				new NormalTransactionDialog(db).showAddDialog();
			}
		});
		transactionsButtonPanel.setLayout(new BoxLayout(
				transactionsButtonPanel, BoxLayout.Y_AXIS));
		transactionsButtonPanel.add(btnAddTransaction);

		JButton btnEditTransaction = new JButton(
				Language.getString("EDIT_TRANSACTION"));
		btnEditTransaction.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnEditTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					// transDialog.showEditDialog(
					// (Date) dateFormat.parse((String) transactionsTable
					// .getModel().getValueAt(
					// transactionsTable.convertRowIndexToModel(transactionsTable.getSelectedRow()),
					// 3)),
					// (String) transactionsTable.getModel().getValueAt(
					// transactionsTable.convertRowIndexToModel(transactionsTable.getSelectedRow()),
					// 4));

					new NormalTransactionDialog(db)
							.showEditDialog(new Object[] {
									transactionsTable.getSelectedColumn(4),
									dateFormat.parse((String) transactionsTable
											.getSelectedColumn(3)) });
				} catch (NumberFormatException | ParseException e1) {
					e1.printStackTrace();
				}

			}
		});
		transactionsButtonPanel.add(btnEditTransaction);

		JButton btnRemoveTransaction = new JButton(
				Language.getString("REMOVE_TRANSACTION"));
		btnRemoveTransaction.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnRemoveTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(
						null,
						Language.getString("REMOVE_TRANSACTION_PROMPT")
								+ " -- "
								+ transactionsTable.getSelectedColumn(0),
						Language.getString("REMOVE_TRANSACTION"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					db.removeTransaction((long) transactionsTable
							.getSelectedColumn(0));
				}
			}
		});
		transactionsButtonPanel.add(btnRemoveTransaction);
		JPanel statisticsPanel = new JPanel();
		statisticsPanel.setLayout(new BoxLayout(statisticsPanel,
				BoxLayout.Y_AXIS));

		JPanel oldestDatePanel = new JPanel();
		oldestDatePanel.add(new JLabel(Language
				.getString("OLDEST_TRANSACTION_DATE") + ": "));
		oldestDate = new JLabel();
		oldestDate.setAlignmentX(Component.CENTER_ALIGNMENT);
		oldestDate.setForeground(Color.RED);
		oldestDatePanel.add(oldestDate);
		statisticsPanel.add(oldestDatePanel);

		JPanel newestDatePanel = new JPanel();
		newestDatePanel.add(new JLabel(Language
				.getString("NEWEST_TRANSACTION_DATE") + ": "));
		newestDate = new JLabel();
		newestDate.setAlignmentX(Component.CENTER_ALIGNMENT);
		newestDate.setForeground(Color.RED);
		newestDatePanel.add(newestDate);
		statisticsPanel.add(newestDatePanel);

		JPanel numTransactionsPanel = new JPanel();
		numTransactionsPanel.add(new JLabel(Language
				.getString("NUMBER_OF_TRANSACTIONS") + ": "));
		numTransactions = new JLabel();
		numTransactions.setAlignmentX(Component.CENTER_ALIGNMENT);
		numTransactions.setForeground(Color.RED);
		numTransactionsPanel.add(numTransactions);
		statisticsPanel.add(numTransactionsPanel);

		JPanel numDepositsPanel = new JPanel();
		numDepositsPanel.add(new JLabel(Language
				.getString("NUMBER_OF_DEPOSITS") + ": "));
		numDeposits = new JLabel();
		numDeposits.setAlignmentX(Component.CENTER_ALIGNMENT);
		numDeposits.setForeground(Color.RED);
		numDepositsPanel.add(numDeposits);
		statisticsPanel.add(numDepositsPanel);

		JPanel numWithDrawalsPanel = new JPanel();
		numWithDrawalsPanel.add(new JLabel(Language
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
		transactionsTable.updateTransactionList(db.getTransactions(
				fromDateChooser.getDate(), toDateChooser.getDate(),
				accountSelectorPanel.getSelectedAccounts()));
		transactionsPane.getVerticalScrollBar().setValue(
				transactionsPane.getVerticalScrollBar().getMaximum());
		// GUI.resizeTable(transactionsTable);
		Date oldest = db.getOldestTransactionDate();
		if (oldest != null) {
			oldestDate.setText(dateFormat.format(oldest));
		} else {
			oldestDate.setText(Language.getString("UNKNOWN"));
		}
		Date newest = db.getNewestTransactionDate();
		if (newest != null) {
			newestDate.setText(dateFormat.format(newest));
		} else {
			newestDate.setText(Language.getString("UNKNOWN"));
		}
		numTransactions.setText(Long.toString(db.getNumberOfTransactions()));
		numDeposits.setText(Long.toString(db.getNumberOfDeposits()));
		numWithdrawals.setText(Long.toString(db.getNumberOfWithdrawals()));
		validate();
		repaint();
	}
}
