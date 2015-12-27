package se.freedrikp.econview.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import se.freedrikp.econview.database.Database;

import com.toedter.calendar.JDateChooser;

public class TransactionDialog {
	private JComboBox accountField;
	private JTextField amountField;
	private JDateChooser dateSelector;
	private JTextField commentField;
	private JScrollPane scrollPane;
	private final LinkedList<JComponent> multiAccounts;
	private Database db;

	public TransactionDialog(Database db) {
		multiAccounts = new LinkedList<JComponent>();
		this.db = db;
	}

	private void createDialog() {
		JPanel dialogPanel = new JPanel();
		dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
		final JPanel multiAccountPanel = new JPanel();
		multiAccountPanel.setLayout(new BoxLayout(multiAccountPanel,
				BoxLayout.Y_AXIS));

		multiAccounts.clear();
		final Object[] accountValues = db.getAccountNames().toArray();

		accountField = new JComboBox(accountValues);

		JPanel accountPanel = new JPanel();
		accountPanel.add(new JLabel(Utilities
				.getString("ADD_TRANSACTION_ACCOUNT") + ":"));
		accountPanel.add(accountField);
		multiAccountPanel.add(accountPanel);
		multiAccounts.add(accountField);

		amountField = new JTextField("", 7);

		JPanel amountPanel = new JPanel();
		amountPanel.add(new JLabel(Utilities
				.getString("ADD_TRANSACTION_AMOUNT") + ":"));
		amountPanel.add(amountField);
		multiAccountPanel.add(amountPanel);
		multiAccounts.add(amountField);
		dialogPanel.add(multiAccountPanel);

		JPanel multiAccountControlPanel = new JPanel();
		JButton removeMultiAccountButton = new JButton("-");
		JButton addMultiAccountButton = new JButton("+");

		removeMultiAccountButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (multiAccounts.size() >= 4) {
					multiAccountPanel.remove(multiAccounts.removeLast()
							.getParent());
					multiAccountPanel.remove(multiAccounts.removeLast()
							.getParent());
				}
				multiAccountPanel.revalidate();
			}
		});

		addMultiAccountButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox accountField = new JComboBox(accountValues);
				accountField.setSelectedIndex(-1);
				JPanel accountPanel = new JPanel();
				accountPanel.add(new JLabel(Utilities
						.getString("ADD_TRANSACTION_ACCOUNT") + ":"));
				accountPanel.add(accountField);
				multiAccountPanel.add(accountPanel);
				multiAccounts.add(accountField);

				JTextField amountField = new JTextField("", 7);
				JPanel amountPanel = new JPanel();
				amountPanel.add(new JLabel(Utilities
						.getString("ADD_TRANSACTION_AMOUNT") + ":"));
				amountPanel.add(amountField);
				multiAccountPanel.add(amountPanel);
				multiAccounts.add(amountField);

				multiAccountPanel.revalidate();
			}
		});

		multiAccountControlPanel.add(removeMultiAccountButton);
		multiAccountControlPanel.add(addMultiAccountButton);
		dialogPanel.add(multiAccountControlPanel);

		JPanel datePanel = new JPanel();
		datePanel.add(new JLabel(Utilities.getString("ADD_TRANSACTION_DATE")
				+ ":"));
		dateSelector = new JDateChooser();
		dateSelector.setDateFormatString(Utilities
				.getConfig("FULL_DATE_FORMAT"));
		datePanel.add(dateSelector);
		dialogPanel.add(datePanel);

		commentField = new JTextField("", 15);
		JPanel commentPanel = new JPanel();
		commentPanel.add(new JLabel(Utilities
				.getString("ADD_TRANSACTION_COMMENT") + ":"));
		commentPanel.add(commentField);
		dialogPanel.add(commentPanel);
		
		dialogPanel.add(new JLabel(Utilities.getString("ADD_TRANSACTION_CHAIN")));

		scrollPane = new JScrollPane();
		scrollPane.setViewportView(dialogPanel);
		scrollPane.setPreferredSize(new Dimension(Integer.parseInt(Utilities
				.getConfig("ADD_TRANSACTION_PANEL_WIDTH")), Integer
				.parseInt(Utilities.getConfig("ADD_TRANSACTION_PANEL_HEIGHT"))));
	}

	private boolean showDialog(long transactionID) {
		int result = JOptionPane.showConfirmDialog(null, scrollPane,
				Utilities.getString("TRANSACTION_DETAILS"),
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null);

		if (result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION) {
			for (int i = 0; i < multiAccounts.size(); i += 2) {
				if (transactionID < 0) {
					db.addTransaction((String) ((JComboBox) multiAccounts
							.get(i)).getSelectedItem(),
							GUI.parseAmount(((JTextField) multiAccounts
									.get(i + 1)).getText()), dateSelector
									.getDate(), commentField.getText());
				} else {
					db.editTransaction(transactionID,
							(String) ((JComboBox) multiAccounts.get(i))
									.getSelectedItem(), GUI
									.parseAmount(((JTextField) multiAccounts
											.get(i + 1)).getText()),
							dateSelector.getDate(), commentField.getText());
				}
			}
		}
		return result == JOptionPane.YES_OPTION;
	}

	public void showEditDialog(long transactionID,
			String selectedAccount, String selectedAmount, Date selectedDate,
			String selectedComment) {
		createDialog();
		accountField.setSelectedItem(selectedAccount);
		amountField.setText(selectedAmount);
		dateSelector.setDate(selectedDate);
		commentField.setText(selectedComment);
		if (showDialog(transactionID)) {
			showAddDialog();
		}
	}

	public void showAddDialog() {
		boolean chain;
		do {
			createDialog();
			chain = showDialog(-1);
		} while (chain);
	}

}
