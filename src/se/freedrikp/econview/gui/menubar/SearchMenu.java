package se.freedrikp.econview.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import se.freedrikp.econview.common.Language;
import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.frames.SearchTransactionsFrame;

public class SearchMenu extends JMenu {
	private Database db;

	public SearchMenu(final Database db) {
		super(Language.getString("MENUBAR_SEARCH"));
		this.db = db;

		JMenuItem searchTransactionsItem = new JMenuItem(
				Language.getString("MENUBAR_SEARCH_TRANSACTIONS"));
		add(searchTransactionsItem);

		searchTransactionsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new SearchTransactionsFrame(db);
			}
		});
	}
}
