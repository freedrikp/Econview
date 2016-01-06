package se.freedrikp.econview.gui.dialogs;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JPanel;

import se.freedrikp.econview.database.Database;

public class StoredTransactionDialog extends TransactionDialog{

	public StoredTransactionDialog(Database db) {
		super(db);
	}

	protected void addDatePanel(JPanel dialogPanel) {
		return;
	}

	protected List<Object[]> getMultiTransactions(Object[] input) {
		return db.getStoredTransaction((String)input[0]);
	}
	protected void addDatabaseHelper(String account, double amount,
			String comment) {
		db.addStoredTransaction(account, amount, comment);
	}

	protected void editDatabaseHelper(long id, String account, double amount,
			String comment) {
		db.editStoredTransaction(id, account, amount, comment);
	}

	protected void setAddSpecifics(JPanel dialogPanel,Object[] input) {
		super.setAddSpecifics(dialogPanel, input);
	}

}
