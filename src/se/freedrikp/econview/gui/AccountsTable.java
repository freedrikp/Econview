package se.freedrikp.econview.gui;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import se.freedrikp.econview.common.Common;
import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.GUI.Model;

public class AccountsTable extends JTable{
	private static final String[] accountHeader = {
		Language.getString("ACCOUNT_HEADER_ACCOUNT"),
		Language.getString("ACCOUNT_HEADER_BALANCE"),
		Language.getString("ACCOUNT_HEADER_HIDDEN") };
	private Database db;
	public AccountsTable(Database db){
		this.db = db;
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	public Object getSelectedColumn(int column){
		return getModel()
		.getValueAt(convertRowIndexToModel(getSelectedRow()),
				column);
	}
	
	public void updateAccountList() {
		Model m = new Model(accountHeader, 0);
		Calendar cal = Common.getFlattenCalendar(null);
		cal.add(Calendar.DAY_OF_MONTH, 1);
		for (Object[] row : db.getAccounts(cal.getTime())) {
			row[1] = NumberFormat.getCurrencyInstance().format(row[1]);
			m.addRow(row);
		}
		setModel(m);
		TableRowSorter<Model> sorter = new TableRowSorter<Model>(m);
		setRowSorter(sorter);
		sorter.setComparator(1, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return Double.compare(GUI.parseAmount(o1), GUI.parseAmount(o2));
			}
		});
		GUI.resizeTable(this);
	}
}
