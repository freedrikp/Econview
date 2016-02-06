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

	public SearchTransactionsControlPanel(Database db,
			TransactionsTable transactionsTable) {
		this.db = db;
		db.addObserver(this);
		this.transactionsTable = transactionsTable;

		Listener listener = new Listener();

		setLayout(new GridLayout(6, 3));

		add(new JLabel(Language.getString("SEARCH_TRANSACTION_ID") + ":"));
		idField = new JTextField();
		add(idField);
		idField.getDocument().addDocumentListener(listener);
		idBox = new JCheckBox(Language.getString("SEARCH_INCLUDE_IN_SEARCH"));
		add(idBox);
		idBox.addItemListener(listener);

		add(new JLabel(Language.getString("SEARCH_TRANSACTION_ACCOUNT") + ":"));
		accountField = new JTextField();
		add(accountField);
		accountField.getDocument().addDocumentListener(listener);
		accountBox = new JCheckBox(Language.getString("SEARCH_INCLUDE_IN_SEARCH"));
		add(accountBox);
		accountBox.addItemListener(listener);

		add(new JLabel(Language.getString("SEARCH_TRANSACTION_AMOUNT") + ":"));
		amountField = new JTextField();
		add(amountField);
		amountField.getDocument().addDocumentListener(listener);
		amountBox = new JCheckBox(Language.getString("SEARCH_INCLUDE_IN_SEARCH"));
		add(amountBox);
		amountBox.addItemListener(listener);

		add(new JLabel(Language.getString("SEARCH_TRANSACTION_FROM_DATE") + ":"));
		fromDateChooser = new JDateChooser(new JSpinnerDateEditor());
		fromDateChooser.setDate(new Date());
		fromDateChooser.setDateFormatString(Configuration
				.getString("FULL_DATE_FORMAT"));
		add(fromDateChooser);
		((JSpinner) (fromDateChooser.getDateEditor().getUiComponent()))
				.addChangeListener(listener);
		fromDateBox = new JCheckBox(Language.getString("SEARCH_INCLUDE_IN_SEARCH"));
		add(fromDateBox);
		fromDateBox.addItemListener(listener);

		add(new JLabel(Language.getString("SEARCH_TRANSACTION_TO_DATE") + ":"));
		toDateChooser = new JDateChooser(new JSpinnerDateEditor());
		toDateChooser.setDate(new Date());
		toDateChooser.setDateFormatString(Configuration
				.getString("FULL_DATE_FORMAT"));
		add(toDateChooser);
		((JSpinner) (toDateChooser.getDateEditor().getUiComponent()))
				.addChangeListener(listener);
		toDateBox = new JCheckBox(Language.getString("SEARCH_INCLUDE_IN_SEARCH"));
		add(toDateBox);
		toDateBox.addItemListener(listener);

		add(new JLabel(Language.getString("SEARCH_TRANSACTION_COMMENT") + ":"));
		commentField = new JTextField();
		add(commentField);
		commentField.getDocument().addDocumentListener(listener);
		commentBox = new JCheckBox(Language.getString("SEARCH_INCLUDE_IN_SEARCH"));
		add(commentBox);
		commentBox.addItemListener(listener);

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
		private void checkDocumentSource(JTextField field, DocumentEvent e,
				JCheckBox box) {
			if (e.getDocument() == field.getDocument()) {
				if (e.getDocument().getLength() > 0){
					box.setSelected(true);					
				}else{
					box.setSelected(false);					
				}
			}
		}

		private void documentUpdate(DocumentEvent e) {
			checkDocumentSource(idField, e, idBox);
			checkDocumentSource(accountField, e, accountBox);
			checkDocumentSource(amountField, e, amountBox);
			checkDocumentSource(commentField, e, commentBox);

			update(db, null);

		}

		public void itemStateChanged(ItemEvent e) {
			update(db, null);
		}

		public void changedUpdate(DocumentEvent e) {
			documentUpdate(e);
		}

		public void insertUpdate(DocumentEvent e) {
			documentUpdate(e);
		}

		public void removeUpdate(DocumentEvent e) {
			documentUpdate(e);
		}

		public void stateChanged(ChangeEvent e) {
			if (e.getSource() == fromDateChooser.getDateEditor()
					.getUiComponent()) {
				fromDateBox.setSelected(true);
			} else if (e.getSource() == toDateChooser.getDateEditor()
					.getUiComponent()) {
				toDateBox.setSelected(true);
			}
			update(db, null);
		}

	}

}
