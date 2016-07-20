package se.freedrikp.econview.gui.tables;

import java.text.NumberFormat;
import java.util.Comparator;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import se.freedrikp.econview.common.Language;
import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.frames.MainFrame;
import se.freedrikp.econview.gui.frames.MainFrame.Model;

public class StoredTransactionsTable extends JTable {
	private static final String[] transactionHeader = {
			Language.getString("TRANSACTION_HEADER_ID"),
			Language.getString("TRANSACTION_HEADER_ACCOUNT"),
			Language.getString("TRANSACTION_HEADER_AMOUNT"),
			Language.getString("TRANSACTION_HEADER_COMMENT") };
	private Database db;

	public StoredTransactionsTable(Database db) {
		this.db = db;
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	public Object getSelectedColumn(int column) {
		return getModel().getValueAt(convertRowIndexToModel(getSelectedRow()),
				column);
	}

	public void updateStoredTransactionsList() {
		Model m = new Model(transactionHeader, 0);
		for (Object[] row : db.getStoredTransactions()) {
			row[2] = NumberFormat.getCurrencyInstance().format(row[2]);
			m.addRow(row);
		}
		setModel(m);
		TableRowSorter<Model> sorter = new TableRowSorter<Model>(m);
		setRowSorter(sorter);
		sorter.setComparator(0, new Comparator<Long>() {
			public int compare(Long o1, Long o2) {
				return o1.compareTo(o2);
			}
		});
		sorter.setComparator(2, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return Double.compare(MainFrame.parseAmount(o1),
						MainFrame.parseAmount(o2));
			}
		});
		MainFrame.resizeTable(this);
	}

}
