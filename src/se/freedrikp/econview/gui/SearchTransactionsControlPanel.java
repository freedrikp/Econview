package se.freedrikp.econview.gui;

import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import se.freedrikp.econview.database.Database;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JSpinnerDateEditor;

public class SearchTransactionsControlPanel extends JPanel implements Observer {
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
	private Database db;
	private TransactionsTable transactionsTable;

	public SearchTransactionsControlPanel(Database db, TransactionsTable transactionsTable) {
		this.db = db;
		db.addObserver(this);
		this.transactionsTable = transactionsTable;
		Listener listener = new Listener();
		setLayout(new GridLayout(6, 3));
		add(new JLabel(Language.getString("SEARCH_TRANSACTION_ID") + ":"));
		add(new JLabel(Language.getString("SEARCH_TRANSACTION_ACCOUNT") + ":"));
		add(new JLabel(Language.getString("SEARCH_TRANSACTION_AMOUNT") + ":"));
		idField = new JTextField();
		add(idField);
		idField.getDocument().addDocumentListener(listener);
		accountField = new JTextField();
		add(accountField);
		accountField.getDocument().addDocumentListener(listener);
		amountField = new JTextField();
		add(amountField);
		amountField.getDocument().addDocumentListener(listener);
		idBox = new JCheckBox();
		add(idBox);
		idBox.addItemListener(listener);
		accountBox = new JCheckBox();
		add(accountBox);
		accountBox.addItemListener(listener);
		amountBox = new JCheckBox();
		add(amountBox);
		amountBox.addItemListener(listener);
		add(new JLabel(Language.getString("SEARCH_TRANSACTION_FROM_DATE") + ":"));
		add(new JLabel(Language.getString("SEARCH_TRANSACTION_TO_DATE") + ":"));
		add(new JLabel(Language.getString("SEARCH_TRANSACTION_COMMENT") + ":"));
		fromDateChooser = new JDateChooser(new JSpinnerDateEditor());
		fromDateChooser.setDate(new Date());
		fromDateChooser.setDateFormatString(Configuration
				.getString("FULL_DATE_FORMAT"));
		add(fromDateChooser);
		((JSpinner) (fromDateChooser.getDateEditor().getUiComponent()))
				.addChangeListener(listener);
		toDateChooser = new JDateChooser(new JSpinnerDateEditor());
		toDateChooser.setDate(new Date());
		toDateChooser.setDateFormatString(Configuration
				.getString("FULL_DATE_FORMAT"));
		add(toDateChooser);
		((JSpinner) (toDateChooser.getDateEditor().getUiComponent()))
				.addChangeListener(listener);
		commentField = new JTextField();
		add(commentField);
		commentField.getDocument().addDocumentListener(listener);

		fromDateBox = new JCheckBox();
		add(fromDateBox);
		fromDateBox.addItemListener(listener);
		toDateBox = new JCheckBox();
		add(toDateBox);
		toDateBox.addItemListener(listener);
		commentBox = new JCheckBox();
		add(commentBox);
		commentBox.addItemListener(listener);
		update(db,null);
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
