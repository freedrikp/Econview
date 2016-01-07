package se.freedrikp.econview.gui.dialogs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.GUI;
import se.freedrikp.econview.gui.Utilities;

public abstract class TransactionDialog extends DatabaseDialog {

	private JPanel multiAccountPanel;
	private List<JComponent> multiAccounts;
	private Object[] accountValues;
	private JTextField commentField;
	private Map<JComponent,Long> IDs;
	private Set<JComponent> toRemove;

	public TransactionDialog(Database db) {
		super(db, Utilities.getString("TRANSACTION_DETAILS"), Utilities
				.getString("ADD_TRANSACTION_CHAIN"));
	}

	protected JComponent createDialog(JPanel dialogPanel) {
		multiAccountPanel = new JPanel();
		multiAccountPanel.setLayout(new BoxLayout(multiAccountPanel,
				BoxLayout.Y_AXIS));

		// multiAccounts.clear();
		multiAccounts = new LinkedList<JComponent>();
		accountValues = db.getAccountNames().toArray();

		dialogPanel.add(multiAccountPanel);

		JPanel multiAccountControlPanel = new JPanel();
		JButton addMultiAccountButton = new JButton("+");

		addMultiAccountButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addMultiAccount(null, null);
			}
		});

		multiAccountControlPanel.add(addMultiAccountButton);
		dialogPanel.add(multiAccountControlPanel);

		addDatePanel(dialogPanel);

		commentField = new JTextField("", 15);
		JPanel commentPanel = new JPanel();
		commentPanel.add(new JLabel(Utilities
				.getString("ADD_TRANSACTION_COMMENT") + ":"));
		commentPanel.add(commentField);
		dialogPanel.add(commentPanel);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(dialogPanel);
		scrollPane
				.setPreferredSize(new Dimension(Integer.parseInt(Utilities
						.getConfig("ADD_TRANSACTION_PANEL_WIDTH")), Integer
						.parseInt(Utilities
								.getConfig("ADD_TRANSACTION_PANEL_HEIGHT"))));
		return scrollPane;
	}

	protected abstract void addDatePanel(JPanel dialogPanel);

	protected void doAddDatabaseAction() {
		for (int i = 0; i < multiAccounts.size(); i += 2) {
			addDatabaseHelper(
					(String) ((JComboBox) multiAccounts.get(i))
							.getSelectedItem(),
					GUI.parseAmount(((JTextField) multiAccounts.get(i + 1))
							.getText()), commentField.getText());
		}
	}

	protected void doEditDatabaseAction() {
		for (int i = 0, j = 0; i < multiAccounts.size(); i += 2, j++) {
			JComboBox accountField = (JComboBox) multiAccounts.get(i);
			if (IDs.containsKey(accountField)){
				editDatabaseHelper(IDs.get(accountField),
						(String) accountField
						.getSelectedItem(),
						GUI.parseAmount(((JTextField) multiAccounts.get(i + 1))
								.getText()), commentField.getText());				
			}else{
				addDatabaseHelper((String) accountField
						.getSelectedItem(),
						GUI.parseAmount(((JTextField) multiAccounts.get(i + 1))
								.getText()), commentField.getText());
			}
		}
		for (JComponent c: toRemove){
			if (IDs.containsKey(c)){
				removeDatabaseHelper(IDs.get(c));
			}
		}
		IDs = null;
		toRemove = null;
	}

	protected abstract void addDatabaseHelper(String account, double amount,
			String comment);

	protected abstract void editDatabaseHelper(long id, String account,
			double amount, String comment);
	
	protected abstract void removeDatabaseHelper(long id);

	protected void setEditSpecifics(JPanel dialogPanel, Object[] input) {
		List<Object[]> multiTransactions = getMultiTransactions(input);
		IDs = new HashMap<JComponent,Long>();
		toRemove = new HashSet<JComponent>();
		int i = 0;
		for (Object[] transaction : multiTransactions) {
			IDs.put(addMultiAccount((String) transaction[1], NumberFormat
					.getCurrencyInstance().format((Double) transaction[2])),(long) transaction[0]);
		}
		commentField.setText((String) input[0]);
	}

	protected abstract List<Object[]> getMultiTransactions(Object[] input);

	protected void setAddSpecifics(JPanel dialogPanel) {
		addMultiAccount(null, null);
	}

	protected void setAddSpecifics(JPanel dialogPanel, Object[] input) {
		for (int i = 0; i < input.length - 1; i += 2) {
			addMultiAccount((String) input[i], NumberFormat
					.getCurrencyInstance().format((double) input[i + 1]));
		}
		commentField.setText((String) input[input.length - 1]);
	}
	
	protected JComponent addMultiAccount(String selectedAccount, String selectedAmount){
		return addMultiAccount(selectedAccount, selectedAmount,multiAccountPanel.getComponentCount());
	}

	private JComponent addMultiAccount(String selectedAccount, String selectedAmount,int index) {
		final JComboBox accountField = new JComboBox(accountValues);
		accountField.setSelectedItem(selectedAccount);
		final JPanel accountPanel = new JPanel();
		accountPanel.add(new JLabel(Utilities
				.getString("ADD_TRANSACTION_ACCOUNT") + ":"));
		accountPanel.add(accountField);
		
		JButton addButton = new JButton("+");
		accountPanel.add(addButton);
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component[] cs = multiAccountPanel.getComponents();
				int j = -1;
				for (int i = 0; i < cs.length; i++){
					if (multiAccountPanel.getComponent(i).equals(accountPanel)){
						j = i;
						break;
					}
				}
				addMultiAccount(null, null,j);
			}
		});
		
		multiAccountPanel.add(accountPanel,index);
		multiAccounts.add(accountField);

		final JTextField amountField = new JTextField("", 7);
		amountField.setText(selectedAmount);
		final JPanel amountPanel = new JPanel();
		amountPanel.add(new JLabel(Utilities
				.getString("ADD_TRANSACTION_AMOUNT") + ":"));
		amountPanel.add(amountField);

		JButton removeButton = new JButton("-");
		amountPanel.add(removeButton);
		removeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				multiAccountPanel.remove(accountPanel);
				multiAccountPanel.remove(amountPanel);
				multiAccounts.remove(accountField);
				multiAccounts.remove(amountField);
				if(toRemove != null){
					toRemove.add(accountField);
				}
				multiAccountPanel.revalidate();
			}
		});
		multiAccountPanel.add(amountPanel,index+1);
		multiAccounts.add(amountField);

		multiAccountPanel.revalidate();
		
		return accountField;
	}

	// private JDateChooser dateSelector;
	// private JTextField commentField;
	// private JScrollPane scrollPane;
	// private final LinkedList<JComponent> multiAccounts;
	// private Database db;
	// private JPanel multiAccountPanel;
	// private Object[] accountValues;
	//
	// public TransactionDialog(Database db) {
	// multiAccounts = new LinkedList<JComponent>();
	// this.db = db;
	// }
	//
	// private void createDialog(boolean storedTransaction) {
	// JPanel dialogPanel = new JPanel();
	// dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
	// multiAccountPanel = new JPanel();
	// multiAccountPanel.setLayout(new BoxLayout(multiAccountPanel,
	// BoxLayout.Y_AXIS));
	//
	// multiAccounts.clear();
	// accountValues = db.getAccountNames().toArray();
	//
	// dialogPanel.add(multiAccountPanel);
	//
	// JPanel multiAccountControlPanel = new JPanel();
	// // JButton removeMultiAccountButton = new JButton("-");
	// JButton addMultiAccountButton = new JButton("+");
	//
	// // removeMultiAccountButton.addActionListener(new ActionListener() {
	// // public void actionPerformed(ActionEvent e) {
	// // if (multiAccounts.size() >= 4) {
	// // multiAccountPanel.remove(multiAccounts.removeLast()
	// // .getParent());
	// // multiAccountPanel.remove(multiAccounts.removeLast()
	// // .getParent());
	// // }
	// // multiAccountPanel.revalidate();
	// // }
	// // });
	//
	// addMultiAccountButton.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// addMultiAccount(null, null);
	// }
	// });
	//
	// // multiAccountControlPanel.add(removeMultiAccountButton);
	// multiAccountControlPanel.add(addMultiAccountButton);
	// dialogPanel.add(multiAccountControlPanel);
	//
	// if (!storedTransaction) {
	// JPanel datePanel = new JPanel();
	// datePanel.add(new JLabel(Utilities
	// .getString("ADD_TRANSACTION_DATE") + ":"));
	// dateSelector = new JDateChooser();
	// dateSelector.setDateFormatString(Utilities
	// .getConfig("FULL_DATE_FORMAT"));
	// datePanel.add(dateSelector);
	//
	// JButton increaseDate = new JButton("+");
	// increaseDate.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// Calendar cal = Calendar.getInstance();
	// cal.setTime(dateSelector.getDate());
	// cal.add(Calendar.DAY_OF_MONTH, 1);
	// dateSelector.setDate(cal.getTime());
	// }
	// });
	// JButton decreaseDate = new JButton("-");
	// decreaseDate.addActionListener(new ActionListener() {
	// public void actionPerformed(ActionEvent e) {
	// Calendar cal = Calendar.getInstance();
	// cal.setTime(dateSelector.getDate());
	// cal.add(Calendar.DAY_OF_MONTH, -1);
	// dateSelector.setDate(cal.getTime());
	// }
	// });
	// datePanel.add(increaseDate);
	// datePanel.add(decreaseDate);
	// dialogPanel.add(datePanel);
	// }
	//
	// commentField = new JTextField("", 15);
	// JPanel commentPanel = new JPanel();
	// commentPanel.add(new JLabel(Utilities
	// .getString("ADD_TRANSACTION_COMMENT") + ":"));
	// commentPanel.add(commentField);
	// dialogPanel.add(commentPanel);
	//
	// dialogPanel
	// .add(new JLabel(Utilities.getString("ADD_TRANSACTION_CHAIN")));
	//
	// scrollPane = new JScrollPane();
	// scrollPane.setViewportView(dialogPanel);
	// scrollPane
	// .setPreferredSize(new Dimension(Integer.parseInt(Utilities
	// .getConfig("ADD_TRANSACTION_PANEL_WIDTH")), Integer
	// .parseInt(Utilities
	// .getConfig("ADD_TRANSACTION_PANEL_HEIGHT"))));
	// }
	//
	// private boolean showDialog(long[] IDs) {
	// int result = JOptionPane.showConfirmDialog(null, scrollPane,
	// Utilities.getString("TRANSACTION_DETAILS"),
	// JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
	// null);
	//
	// if (result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION)
	// {
	// int j = 0;
	// for (int i = 0; i < multiAccounts.size(); i += 2) {
	// if (j >= IDs.length) {
	// db.addTransaction((String) ((JComboBox) multiAccounts
	// .get(i)).getSelectedItem(),
	// GUI.parseAmount(((JTextField) multiAccounts
	// .get(i + 1)).getText()), dateSelector
	// .getDate(), commentField.getText());
	// } else {
	// db.editTransaction(IDs[j],
	// (String) ((JComboBox) multiAccounts.get(i))
	// .getSelectedItem(), GUI
	// .parseAmount(((JTextField) multiAccounts
	// .get(i + 1)).getText()),
	// dateSelector.getDate(), commentField.getText());
	// }
	// j++;
	// }
	// }
	// return result == JOptionPane.YES_OPTION;
	// }
	//
	// public void showEditDialog(Date selectedDate, String selectedComment) {
	// createDialog(false);
	// List<Object[]> multiTransactions = db.getMultiTransactions(
	// selectedDate, selectedComment);
	// long[] IDs = new long[multiTransactions.size()];
	// int i = 0;
	// for (Object[] transaction : multiTransactions) {
	// addMultiAccount((String) transaction[1], NumberFormat
	// .getCurrencyInstance().format((Double) transaction[2]));
	// IDs[i++] = (long) transaction[0];
	// }
	// dateSelector.setDate(selectedDate);
	// commentField.setText(selectedComment);
	// if (showDialog(IDs)) {
	// showAddDialog();
	// }
	// }
	//
	// public void showAddDialog() {
	// boolean chain;
	// do {
	// createDialog(false);
	// addMultiAccount(null, null);
	// dateSelector.setDate(new Date());
	// chain = showDialog(new long[0]);
	// } while (chain);
	// }
	//
	// private void addMultiAccount(String selectedAccount, String
	// selectedAmount) {
	// final JComboBox accountField = new JComboBox(accountValues);
	// accountField.setSelectedItem(selectedAccount);
	// final JPanel accountPanel = new JPanel();
	// accountPanel.add(new JLabel(Utilities
	// .getString("ADD_TRANSACTION_ACCOUNT") + ":"));
	// accountPanel.add(accountField);
	// multiAccountPanel.add(accountPanel);
	// multiAccounts.add(accountField);
	//
	// final JTextField amountField = new JTextField("", 7);
	// amountField.setText(selectedAmount);
	// final JPanel amountPanel = new JPanel();
	// amountPanel.add(new JLabel(Utilities
	// .getString("ADD_TRANSACTION_AMOUNT") + ":"));
	// amountPanel.add(amountField);
	//
	// JButton removeButton = new JButton("-");
	// amountPanel.add(removeButton);
	// removeButton.addActionListener(new ActionListener() {
	//
	// public void actionPerformed(ActionEvent e) {
	// multiAccountPanel.remove(accountPanel);
	// multiAccountPanel.remove(amountPanel);
	// multiAccounts.remove(accountField);
	// multiAccounts.remove(amountField);
	// multiAccountPanel.revalidate();
	// }
	// });
	//
	// multiAccountPanel.add(amountPanel);
	// multiAccounts.add(amountField);
	//
	// multiAccountPanel.revalidate();
	// }
	//
	// public void showEditStoredDialog(String selectedComment) {
	// createDialog(true);
	// List<Object[]> multiTransactions = db
	// .getStoredTransaction(selectedComment);
	// long[] IDs = new long[multiTransactions.size()];
	// int i = 0;
	// for (Object[] transaction : multiTransactions) {
	// addMultiAccount((String) transaction[1], NumberFormat
	// .getCurrencyInstance().format((Double) transaction[2]));
	// IDs[i++] = (long) transaction[0];
	// }
	// commentField.setText(selectedComment);
	// if (showStoredDialog(IDs)) {
	// showAddStoredDialog();
	// }
	// }
	//
	// public void showAddStoredDialog() {
	// boolean chain;
	// do {
	// createDialog(true);
	// addMultiAccount(null, null);
	// chain = showStoredDialog(new long[0]);
	// } while (chain);
	// }
	//
	// public void showAddStoredDialog(String account, double amount, String
	// comment) {
	// createDialog(true);
	// addMultiAccount(account,NumberFormat
	// .getCurrencyInstance().format(amount));
	// commentField.setText(comment);
	// if (showStoredDialog(new long[0])) {
	// showAddStoredDialog();
	// }
	// }
	//
	// private boolean showStoredDialog(long[] IDs) {
	// int result = JOptionPane.showConfirmDialog(null, scrollPane,
	// Utilities.getString("TRANSACTION_DETAILS"),
	// JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
	// null);
	//
	// if (result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION)
	// {
	// int j = 0;
	// for (int i = 0; i < multiAccounts.size(); i += 2) {
	// if (j >= IDs.length) {
	// db.addStoredTransaction((String) ((JComboBox) multiAccounts
	// .get(i)).getSelectedItem(),
	// GUI.parseAmount(((JTextField) multiAccounts
	// .get(i + 1)).getText()), commentField
	// .getText());
	// } else {
	// db.editStoredTransaction(IDs[j],
	// (String) ((JComboBox) multiAccounts.get(i))
	// .getSelectedItem(), GUI
	// .parseAmount(((JTextField) multiAccounts
	// .get(i + 1)).getText()),
	// commentField.getText());
	// }
	// j++;
	// }
	// }
	// return result == JOptionPane.YES_OPTION;
	// }
	//
	// public void showMakeStoredDialog(String selectedComment) {
	// createDialog(false);
	// List<Object[]> multiTransactions = db
	// .getStoredTransaction(selectedComment);
	// for (Object[] transaction : multiTransactions) {
	// addMultiAccount((String) transaction[1], NumberFormat
	// .getCurrencyInstance().format((Double) transaction[2]));
	// }
	// dateSelector.setDate(new Date());
	// commentField.setText(selectedComment);
	// if (showDialog(new long[0])) {
	// showAddDialog();
	// }
	// }
}
