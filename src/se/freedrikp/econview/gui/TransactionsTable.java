package se.freedrikp.econview.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableRowSorter;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.GUI.Model;
import se.freedrikp.econview.gui.dialogs.StoredTransactionDialog;

public class TransactionsTable extends JTable {
	private static final String[] transactionHeader = {
		Language.getString("TRANSACTION_HEADER_ID"),
		Language.getString("TRANSACTION_HEADER_ACCOUNT"),
		Language.getString("TRANSACTION_HEADER_AMOUNT"),
		Language.getString("TRANSACTION_HEADER_DATE"),
		Language.getString("TRANSACTION_HEADER_COMMENT") };
	private Database db;
	private SimpleDateFormat dateFormat;
	
	public TransactionsTable(final Database db){
		super();
		this.db = db;
		dateFormat = new SimpleDateFormat(
				Configuration.getString("FULL_DATE_FORMAT"));
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.getClickCount() == 2
						&& e.getButton() == MouseEvent.BUTTON1) {
					int selection = getSelectedRow();
					if (selection >= 0) {
						String account = (String) getSelectedColumn(1);
						double amount = GUI.parseAmount((String) getSelectedColumn(2));
						String comment = (String) getSelectedColumn(4);
						new StoredTransactionDialog(db)
								.showAddDialog(new Object[] { account, amount,
										comment });
					}
				} else if (e.getClickCount() == 2
						&& e.getButton() == MouseEvent.BUTTON3) {
					int selection = getSelectedRow();
					if (selection >= 0) {
						String comment = (String) getSelectedColumn(4);
						try {
							List<Object[]> transactions = db.getMultiTransactions(
									dateFormat.parse((String) getSelectedColumn(3)), comment);
							Object[] transaction = new Object[transactions
									.size() * 2 + 1];
							int i = 0;
							for (Object[] t : transactions) {
								transaction[i++] = t[1];
								transaction[i++] = t[2];
							}
							transaction[transactions.size() * 2] = comment;
							new StoredTransactionDialog(db)
									.showAddDialog(transaction);
						} catch (ParseException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
	}
	
	public Object getSelectedColumn(int column){
		return getModel()
		.getValueAt(convertRowIndexToModel(getSelectedRow()),
				column);
	}
	
	public void updateTransactionList(List<Object[]> data) {
		Model m = new Model(transactionHeader, 0);
		for (Object[] row : data) {
			row[2] = NumberFormat.getCurrencyInstance().format(row[2]);
			row[3] = dateFormat.format(row[3]);
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
				return Double.compare(GUI.parseAmount(o1), GUI.parseAmount(o2));
			}
		});
		GUI.resizeTable(this);
	}

}
