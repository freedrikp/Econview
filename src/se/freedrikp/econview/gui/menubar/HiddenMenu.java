package se.freedrikp.econview.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.Language;

public class HiddenMenu extends JMenu {

	public HiddenMenu(final Database db) {
		super(Language.getString("MENUBAR_HIDDEN"));

		final JCheckBoxMenuItem mntmShowHidden = new JCheckBoxMenuItem(
				Language.getString("MENUBAR_HIDDEN_SHOW_HIDDEN"),
				db.getShowHidden());
		mntmShowHidden.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				db.setShowHidden(mntmShowHidden.getState());
			}
		});
		add(mntmShowHidden);
	}

}
