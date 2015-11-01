package se.freedrikp.econview.gui;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.GUI.Model;

import com.toedter.calendar.JDateChooser;

public class TransactionsTab extends JPanel implements Observer {

	private Database db;
	private JScrollPane transactionsPane;
	private JTable transactionsTable;
	private static final String[] transactionHeader = { Utilities.getString("TRANSACTION_HEADER_ID"),Utilities.getString("TRANSACTION_HEADER_ACCOUNT"),
		Utilities.getString("TRANSACTION_HEADER_AMOUNT"), Utilities.getString("TRANSACTION_HEADER_DATE"), Utilities.getString("TRANSACTION_HEADER_COMMENT")};

	public TransactionsTab(final Database db) {
		super();
		this.db = db;
		db.addObserver(this);
		setLayout(new GridLayout(0, 2, 0, 0));

		transactionsPane = new JScrollPane();
		add(transactionsPane);

		transactionsTable = new JTable();
		transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//transactionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		transactionsPane.setViewportView(transactionsTable);

		JPanel transactionsButtonPanel = new JPanel();
		add(transactionsButtonPanel);

		JButton btnAddTransaction = new JButton(Utilities.getString("ADD_TRANSACTION"));
		btnAddTransaction.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnAddTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// String accountName = askUser("Account Name",
				// "What is the name of the account?", null, db
				// .getAccountNames().toArray());
				// if (accountName == null) {
				// return;
				// }
				// String transactionAmount = askUser("Transaction Amount",
				// "What is the amount of the transaction?", null, null);
				// if (transactionAmount == null) {
				// return;
				// }
				// String transactionDate = askUser("Transaction Date",
				// "What is the date of the transaction?", null, null);
				// if (transactionDate == null) {
				// return;
				// }
				// String transactionComment = askUser("Transaction Comment",
				// "What is the comment of the tansaction?", null, null);
				// if (transactionComment == null) {
				// return;
				// }
				// try {
				// db.addTransaction(accountName, Double
				// .parseDouble(transactionAmount),
				// new SimpleDateFormat("yyyy-mm-dd")
				// .parse(transactionDate), transactionComment);
				// } catch (NumberFormatException e1) {
				// e1.printStackTrace();
				// } catch (ParseException e1) {
				// e1.printStackTrace();
				// }

				String[] transactionDetails = askUserTransaction(db
						.getAccountNames().toArray(), null, null,
						new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
						null);

				if (transactionDetails != null) {
					try {
						db.addTransaction(transactionDetails[0], GUI
								.parseAmount(transactionDetails[1]),
								new SimpleDateFormat("yyyy-MM-dd")
										.parse(transactionDetails[2]),
								transactionDetails[3]);
					} catch (NumberFormatException | ParseException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		transactionsButtonPanel.setLayout(new BoxLayout(
				transactionsButtonPanel, BoxLayout.Y_AXIS));
		transactionsButtonPanel.add(btnAddTransaction);

		JButton btnEditTransaction = new JButton(Utilities.getString("EDIT_TRANSACTION"));
		btnEditTransaction.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnEditTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// String accountName = askUser(
				// "Account Name",
				// "What is the name of the account?",
				// (String) transactionsTable.getModel().getValueAt(
				// transactionsTable.getSelectedRow(), 1),
				// db.getAccountNames().toArray());
				// if (accountName == null) {
				// return;
				// }
				// String transactionAmount = askUser(
				// "Transaction Amount",
				// "What is the amount of the transaction?",
				// (String) transactionsTable.getModel().getValueAt(
				// transactionsTable.getSelectedRow(), 2), null);
				// if (transactionAmount == null) {
				// return;
				// }
				// String transactionDate = askUser(
				// "Transaction Date",
				// "What is the date of the transaction?",
				// (String) transactionsTable.getModel().getValueAt(
				// transactionsTable.getSelectedRow(), 3), null);
				// if (transactionDate == null) {
				// return;
				// }
				// String transactionComment = askUser(
				// "Transaction Comment",
				// "What is the comment of the tansaction?",
				// (String) transactionsTable.getModel().getValueAt(
				// transactionsTable.getSelectedRow(), 4), null);
				// if (transactionComment == null) {
				// return;
				// }
				// try {
				// db.editTransaction(Long
				// .parseLong((String) transactionsTable.getModel()
				// .getValueAt(transactionsTable.getSelectedRow(),
				// 0)), accountName, Double
				// .parseDouble(transactionAmount),
				// new SimpleDateFormat("yyyy-mm-dd")
				// .parse(transactionDate), transactionComment);
				// } catch (NumberFormatException e1) {
				// e1.printStackTrace();
				// } catch (ParseException e1) {
				// e1.printStackTrace();
				// }
				String[] transactionDetails = askUserTransaction(
						db.getAccountNames().toArray(),
						(String) transactionsTable.getModel().getValueAt(
								transactionsTable.getSelectedRow(), 1),
						(String) transactionsTable.getModel().getValueAt(
								transactionsTable.getSelectedRow(), 2),
						(String) transactionsTable.getModel().getValueAt(
								transactionsTable.getSelectedRow(), 3),
						(String) transactionsTable.getModel().getValueAt(
								transactionsTable.getSelectedRow(), 4));
				if (transactionDetails != null) {
					try {
						db.editTransaction(Long
								.parseLong((String) transactionsTable
										.getModel().getValueAt(
												transactionsTable
														.getSelectedRow(), 0)),
								transactionDetails[0], GUI
										.parseAmount(transactionDetails[1]),
								new SimpleDateFormat("yyyy-MM-dd")
										.parse(transactionDetails[2]),
								transactionDetails[3]);
					} catch (NumberFormatException | ParseException e1) {
						e1.printStackTrace();
					}
				}

			}
		});
		transactionsButtonPanel.add(btnEditTransaction);

		JButton btnRemoveTransaction = new JButton(Utilities.getString("REMOVE_TRANSACTION"));
		btnRemoveTransaction.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnRemoveTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(
						null,
						Utilities.getString("REMOVE_TRANSACTION_PROMPT") + " -- "
								+ (String) transactionsTable.getModel()
										.getValueAt(
												transactionsTable
														.getSelectedRow(), 0),
						Utilities.getString("REMOVE_TRANSACTION"), JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					db.removeTransaction(Long
							.parseLong((String) transactionsTable.getModel()
									.getValueAt(
											transactionsTable.getSelectedRow(),
											0)));
				}
			}
		});
		transactionsButtonPanel.add(btnRemoveTransaction);
		update(db, null);
	}

	public void update(Observable o, Object arg) {
		updateTransactionList();
		transactionsPane.getVerticalScrollBar().setValue(
				transactionsPane.getVerticalScrollBar().getMaximum());
		GUI.resizeTable(transactionsTable);
		repaint();
	}

	private String[] askUserTransaction(Object[] accountValues,
			String selectedAccount, String selectedAmount, String selectedDate,
			String selectedComment) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JComboBox accountField = new JComboBox(accountValues);
		accountField.setSelectedItem(selectedAccount);
		JPanel accountPanel = new JPanel();
		accountPanel.add(new JLabel(Utilities.getString("ADD_TRANSACTION_ACCOUNT") + ":"));
		accountPanel.add(accountField);
		panel.add(accountPanel);

		JTextField amountField = new JTextField("", 7);
		amountField.setText(selectedAmount);
		JPanel amountPanel = new JPanel();
		amountPanel.add(new JLabel(Utilities.getString("ADD_TRANSACTION_AMOUNT") + ":"));
		amountPanel.add(amountField);
		panel.add(amountPanel);

		// JTextField dateField = new JTextField("", 7);
		// dateField.setText(selectedDate);
		JPanel datePanel = new JPanel();
		datePanel.add(new JLabel(Utilities.getString("ADD_TRANSACTION_DATE") + ":"));
		// -----
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		JDateChooser dateSelector = null;
		try {
			dateSelector = new JDateChooser(df.parse(selectedDate),
					"yyyy-MM-dd");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		datePanel.add(dateSelector);
		// -----
		// datePanel.add(dateField);
		panel.add(datePanel);

		JTextField commentField = new JTextField("", 15);
		commentField.setText(selectedComment);
		JPanel commentPanel = new JPanel();
		commentPanel.add(new JLabel(Utilities.getString("ADD_TRANSACTION_COMMENT") + ":"));
		commentPanel.add(commentField);
		panel.add(commentPanel);

		int result = JOptionPane.showConfirmDialog(null, panel,
				Utilities.getString("TRANSACTION_DETAILS"), JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null);

		if (result == JOptionPane.OK_OPTION) {
			String[] details = new String[4];
			details[0] = (String) accountField.getSelectedItem();
			details[1] = amountField.getText();
			details[2] = df.format(dateSelector.getDate());// dateField.getText();
			details[3] = commentField.getText();
			return details;
		}
		return null;
	}

	private void updateTransactionList() {
		Model m = new Model(transactionHeader, 0);
		for (String[] row : db.getTransactions()) {
			row[2] = NumberFormat.getCurrencyInstance().format(
					Double.parseDouble(row[2]));
			m.addRow(row);
		}
		transactionsTable.setModel(m);
	}
}
