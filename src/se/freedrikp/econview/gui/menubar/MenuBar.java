package se.freedrikp.econview.gui.menubar;

import javax.swing.JMenuBar;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.database.Security;
import se.freedrikp.econview.gui.GUI;

public class MenuBar extends JMenuBar {

	private Database db;
	private Security sec;

	public MenuBar(final Database db, final Security sec, GUI gui) {
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
		add(new DeleteMenu(db));
		add(new StoredTransactionsMenu(db));
	}

}
