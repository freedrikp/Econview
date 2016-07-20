package se.freedrikp.econview.gui.menubar;

import javax.swing.JMenuBar;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.database.SQLiteDatabase;
import se.freedrikp.econview.database.SQLiteSecurity;
import se.freedrikp.econview.database.Security;
import se.freedrikp.econview.gui.frames.MainFrame;

public class MenuBar extends JMenuBar {

	private Database db;
	private Security sec;

	public MenuBar(final SQLiteDatabase db, final SQLiteSecurity sec, MainFrame gui) {
		super();
		this.db = db;
		this.sec = sec;

		add(new FileMenu(db, sec, gui));
		add(new ImportExportMenu(db));
		add(new HiddenMenu(db));
		add(new SettingsMenu());
		if (sec != null) {
			add(new UsersMenu(sec));
		}
		add(new SearchMenu(db));
		add(new DeleteMenu(db));
		add(new StoredTransactionsMenu(db));
	}

}
