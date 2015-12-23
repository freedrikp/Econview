package se.freedrikp.econview.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
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

import com.toedter.calendar.JDateChooser;

public class TransactionDialog {

	public static Object[] showEditDialog(final Object[] accountValues,
			String selectedAccount, String selectedAmount, Date selectedDate,
			String selectedComment) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		final JPanel multiAccountPanel = new JPanel();
		multiAccountPanel.setLayout(new BoxLayout(multiAccountPanel,
				BoxLayout.Y_AXIS));

		final LinkedList<JComponent> multiAccounts = new LinkedList<JComponent>();

		JComboBox accountField = new JComboBox(accountValues);
		accountField.setSelectedItem(selectedAccount);
		JPanel accountPanel = new JPanel();
		accountPanel.add(new JLabel(Utilities
				.getString("ADD_TRANSACTION_ACCOUNT") + ":"));
		accountPanel.add(accountField);
		multiAccountPanel.add(accountPanel);
		multiAccounts.add(accountField);

		JTextField amountField = new JTextField("", 7);
		amountField.setText(selectedAmount);
		JPanel amountPanel = new JPanel();
		amountPanel.add(new JLabel(Utilities
				.getString("ADD_TRANSACTION_AMOUNT") + ":"));
		amountPanel.add(amountField);
		multiAccountPanel.add(amountPanel);
		multiAccounts.add(amountField);

		// JScrollPane multiAccountScrollPane = new JScrollPane();
		// multiAccountScrollPane.setViewportView(multiAccountPanel);
		// panel.add(multiAccountScrollPane);
		panel.add(multiAccountPanel);

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
		panel.add(multiAccountControlPanel);

		// JTextField dateField = new JTextField("", 7);
		// dateField.setText(selectedDate);
		JPanel datePanel = new JPanel();
		datePanel.add(new JLabel(Utilities.getString("ADD_TRANSACTION_DATE")
				+ ":"));
		// -----
		JDateChooser dateSelector = new JDateChooser(selectedDate,
				new SimpleDateFormat(Utilities.getConfig("FULL_DATE_FORMAT"))
						.toPattern());
		datePanel.add(dateSelector);
		// -----
		// datePanel.add(dateField);
		panel.add(datePanel);

		JTextField commentField = new JTextField("", 15);
		commentField.setText(selectedComment);
		JPanel commentPanel = new JPanel();
		commentPanel.add(new JLabel(Utilities
				.getString("ADD_TRANSACTION_COMMENT") + ":"));
		commentPanel.add(commentField);
		panel.add(commentPanel);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(panel);
		scrollPane.setPreferredSize(new Dimension(Integer.parseInt(Utilities
				.getConfig("ADD_TRANSACTION_PANEL_HEIGHT")), Integer
				.parseInt(Utilities.getConfig("ADD_TRANSACTION_PANEL_WIDTH"))));

		int result = JOptionPane.showConfirmDialog(null, scrollPane,
				Utilities.getString("TRANSACTION_DETAILS"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null);

		if (result == JOptionPane.OK_OPTION) {
			Object[] details = new Object[multiAccounts.size() + 2];
			int i = 0;
			for (JComponent c : multiAccounts) {
				if (i % 2 == 0) {
					details[i] = ((JComboBox) c).getSelectedItem();
				} else {
					details[i] = ((JTextField) c).getText();
				}
				i++;
			}
			// details[0] = accountField.getSelectedItem();
			// details[1] = amountField.getText();
			details[i] = dateSelector.getDate();// dateField.getText();
			details[i + 1] = commentField.getText();
			return details;
		}
		return null;

	}

	public static Object[] showAddDialog(final Object[] accountValues) {
		return showEditDialog(accountValues, null, null, new Date(), null);
	}

}
