package se.freedrikp.econview.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.Language;

public class DeleteMenu extends JMenu {

	public DeleteMenu(final Database db) {
		super(Language.getString("MENUBAR_DELETE"));

		JMenuItem deleteAccounts = new JMenuItem(
				Language.getString("MENUBAR_DELETE_ACCOUNTS"));
		add(deleteAccounts);
		deleteAccounts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(null,
						Language.getString("PROMPT_DELETE_ACCOUNTS"),
						Language.getString("MENUBAR_DELETE_ACCOUNTS"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					db.deleteAccounts();
				}
			}
		});

		JMenuItem deleteTransactions = new JMenuItem(
				Language.getString("MENUBAR_DELETE_TRANSACTIONS"));
		add(deleteTransactions);
		deleteTransactions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(null,
						Language.getString("PROMPT_DELETE_TRANSACTIONS"),
						Language.getString("MENUBAR_DELETE_TRANSACTIONS"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					db.deleteTransactions();
				}
			}
		});

		JMenuItem deleteStoredTransactions = new JMenuItem(
				Language.getString("MENUBAR_DELETE_STORED_TRANSACTIONS"));
		add(deleteStoredTransactions);
		deleteStoredTransactions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(
						null,
						Language.getString("PROMPT_DELETE_STORED_TRANSACTIONS"),
						Language.getString("MENUBAR_DELETE_STORED_TRANSACTIONS"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					db.deleteStoredTransactions();
				}
			}
		});
	}

}
