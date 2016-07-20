package se.freedrikp.econview.gui.dialogs;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;

import se.freedrikp.econview.common.Common;
import se.freedrikp.econview.common.Configuration;
import se.freedrikp.econview.common.Language;
import se.freedrikp.econview.database.SQLiteDatabase;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JSpinnerDateEditor;

public class NormalTransactionDialog extends TransactionDialog {

	private JDateChooser dateSelector;

	public NormalTransactionDialog(SQLiteDatabase db) {
		super(db);
	}

	protected void addDatePanel(JPanel dialogPanel) {
		JPanel datePanel = new JPanel();
		datePanel.add(new JLabel(Language.getString("ADD_TRANSACTION_DATE")
				+ ":"));
		dateSelector = new JDateChooser(new JSpinnerDateEditor());
		dateSelector.setDateFormatString(Configuration
				.getString("FULL_DATE_FORMAT"));
		datePanel.add(dateSelector);

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
		dialogPanel.add(datePanel);
	}

	protected void setEditSpecifics(JPanel dialogPanel, Object[] input) {
		super.setEditSpecifics(dialogPanel, input);
		dateSelector.setDate((Date) input[1]);
	}

	protected void setAddSpecifics(JPanel dialogPanel) {
		super.setAddSpecifics(dialogPanel);
		dateSelector.setDate(new Date());
	}

	protected List<Object[]> getMultiTransactions(Object[] input) {
		return db.getMultiTransactions((Date) input[1], (String) input[0]);
	}

	protected void addDatabaseHelper(String account, double amount,
			String comment) {
		db.addTransaction(account, amount, dateSelector.getDate(), comment);
	}

	protected void editDatabaseHelper(long id, String account, double amount,
			String comment) {
		db.editTransaction(id, account, amount, dateSelector.getDate(), comment);
	}

	protected void setAddSpecifics(JPanel dialogPanel, Object[] input) {
		super.setAddSpecifics(dialogPanel,
				Arrays.copyOf(input, input.length - 1));
		dateSelector.setDate((Date) input[input.length - 1]);
	}

	protected void removeDatabaseHelper(long id) {
		db.removeTransaction(id);
	}

	protected Calendar getCorrectCal() {
		return Common.getFlattenCalendar(dateSelector.getDate());
	}

}
