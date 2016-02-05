package se.freedrikp.econview.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.dialogs.NormalTransactionDialog;

public class SearchTransactionsFrame extends JFrame implements Observer {
	private Database db;
	private TransactionsTable transactionsTable;

	public SearchTransactionsFrame(final Database db) {
		super(Language.getString("MENUBAR_SEARCH_TRANSACTIONS"));
		this.db = db;
		final SimpleDateFormat dateFormat = new SimpleDateFormat(
				Configuration.getString("FULL_DATE_FORMAT"));
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());

		JPanel controlPanel = new JPanel();
		contentPane.add(controlPanel, BorderLayout.NORTH);

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
		transactionsTable.updateTransactionList(db.searchTransactions(0, false,
				null, 0, false, null, null, null));
	}
}
