package se.freedrikp.econview.gui.menubar;

import javax.swing.JMenuBar;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.database.Security;
import se.freedrikp.econview.gui.frames.MainFrame;

public class MenuBar extends JMenuBar {

	private Database db;
	private Security sec;

	public MenuBar(final Database db, final Security security, MainFrame gui) {
		super();
		this.db = db;
		this.sec = security;

		add(new FileMenu(db, security, gui));
		add(new ImportExportMenu(db));
		add(new HiddenMenu(db));
		add(new SettingsMenu());
		if (security != null) {
			add(new UsersMenu(security));
		}
		add(new SearchMenu(db));
		add(new DeleteMenu(db));
		add(new StoredTransactionsMenu(db));
	}

}
