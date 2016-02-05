package se.freedrikp.econview.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.dialogs.NormalTransactionDialog;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JSpinnerDateEditor;

public class SearchTransactionsFrame extends JFrame implements Observer {
	private Database db;
	private TransactionsTable transactionsTable;
	private JTextField idField;
	private JTextField accountField;
	private JTextField amountField;
	private JDateChooser fromDateChooser;
	private JDateChooser toDateChooser;
	private JTextField commentField;
	private JCheckBox idBox;
	private JCheckBox accountBox;
	private JCheckBox amountBox;
	private JCheckBox fromDateBox;
	private JCheckBox toDateBox;
	private JCheckBox commentBox;

	public SearchTransactionsFrame(final Database db) {
		super(Language.getString("MENUBAR_SEARCH_TRANSACTIONS"));
		this.db = db;
		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				Configuration.getString("FULL_DATE_FORMAT"));
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		Listener listener = new Listener();
		JPanel controlPanel = new JPanel();
		contentPane.add(controlPanel, BorderLayout.NORTH);

		controlPanel.setLayout(new GridLayout(3, 6));
		controlPanel.add(new JLabel(Language.getString("SEARCH_TRANSACTION_ID")
				+ ":"));
		controlPanel.add(new JLabel(Language
				.getString("SEARCH_TRANSACTION_ACCOUNT") + ":"));
		controlPanel.add(new JLabel(Language
				.getString("SEARCH_TRANSACTION_AMOUNT") + ":"));
		controlPanel.add(new JLabel(Language
				.getString("SEARCH_TRANSACTION_FROM_DATE") + ":"));
		controlPanel.add(new JLabel(Language
				.getString("SEARCH_TRANSACTION_TO_DATE") + ":"));
		controlPanel.add(new JLabel(Language
				.getString("SEARCH_TRANSACTION_COMMENT") + ":"));
		idField = new JTextField();
		controlPanel.add(idField);
		idField.getDocument().addDocumentListener(listener);
		accountField = new JTextField();
		controlPanel.add(accountField);
		accountField.getDocument().addDocumentListener(listener);
		amountField = new JTextField();
		controlPanel.add(amountField);
		amountField.getDocument().addDocumentListener(listener);
		fromDateChooser = new JDateChooser(new JSpinnerDateEditor());
		fromDateChooser.setDate(new Date());
		fromDateChooser.setDateFormatString(Configuration
				.getString("FULL_DATE_FORMAT"));
		controlPanel.add(fromDateChooser);
		((JSpinner) (fromDateChooser.getDateEditor().getUiComponent()))
				.addChangeListener(listener);
		toDateChooser = new JDateChooser(new JSpinnerDateEditor());
		toDateChooser.setDate(new Date());
		toDateChooser.setDateFormatString(Configuration
				.getString("FULL_DATE_FORMAT"));
		controlPanel.add(toDateChooser);
		((JSpinner) (toDateChooser.getDateEditor().getUiComponent()))
				.addChangeListener(listener);
		commentField = new JTextField();
		controlPanel.add(commentField);
		commentField.getDocument().addDocumentListener(listener);

		idBox = new JCheckBox();
		controlPanel.add(idBox);
		idBox.addItemListener(listener);
		accountBox = new JCheckBox();
		controlPanel.add(accountBox);
		accountBox.addItemListener(listener);
		amountBox = new JCheckBox();
		controlPanel.add(amountBox);
		amountBox.addItemListener(listener);
		fromDateBox = new JCheckBox();
		controlPanel.add(fromDateBox);
		fromDateBox.addItemListener(listener);
		toDateBox = new JCheckBox();
		controlPanel.add(toDateBox);
		toDateBox.addItemListener(listener);
		commentBox = new JCheckBox();
		controlPanel.add(commentBox);
		commentBox.addItemListener(listener);

		transactionsTable = new TransactionsTable(db);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(transactionsTable);
		contentPane.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		contentPane.add(buttonPanel, BorderLayout.SOUTH);

		JButton btnEditTransaction = new JButton(
				Language.getString("EDIT_TRANSACTION"));
		btnEditTransaction.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnEditTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
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
		buttonPanel.add(btnEditTransaction);

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
		buttonPanel.add(btnRemoveTransaction);

		setContentPane(contentPane);
		int width = Configuration.getInt("SEARCH_PANEL_WIDTH");
		int height = Configuration.getInt("SEARCH_PANEL_HEIGHT");
		DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDisplayMode();
		setBounds((dm.getWidth() - width) / 2, (dm.getHeight() - height) / 2,
				width, height);
		setVisible(true);
		update(db, null);
	}

	public void update(Observable o, Object arg) {
		long id = 0;
		boolean doID = false;
		String accountName = null;
		double amount = 0;
		boolean doAmount = false;
		String comment = null;
		Date fromDate = null;
		Date toDate = null;
		if (idBox.isSelected()) {
			doID = true;
			try {
				id = Long.parseLong(idField.getText());
			} catch (NumberFormatException e) {
				doID = false;
			}
		}
		if (accountBox.isSelected()) {
			accountName = accountField.getText();
		}
		if (amountBox.isSelected()) {
			doAmount = true;
			try {
				amount = GUI.parseAmount(amountField.getText());
			} catch (NumberFormatException e) {
				doAmount = false;
			}
		}
		if (fromDateBox.isSelected()) {
			fromDate = fromDateChooser.getDate();
		}
		if (toDateBox.isSelected()) {
			toDate = toDateChooser.getDate();
		}
		if (commentBox.isSelected()) {
			comment = commentField.getText();
		}
		transactionsTable.updateTransactionList(db.searchTransactions(id, doID,
				accountName, amount, doAmount, comment, fromDate, toDate));
	}

	private class Listener implements ItemListener, DocumentListener,
			ChangeListener {

		public void itemStateChanged(ItemEvent e) {
			update(db, null);
		}

		public void changedUpdate(DocumentEvent e) {
			update(db, null);
		}

		public void insertUpdate(DocumentEvent e) {
			update(db, null);
		}

		public void removeUpdate(DocumentEvent e) {
			update(db, null);
		}

		public void stateChanged(ChangeEvent arg0) {
			update(db, null);
		}

	}
}
