package se.freedrikp.econview.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.Utilities;

public class ImportExportMenu extends JMenu {

	public ImportExportMenu(final Database db) {
		super(Utilities.getString("MENUBAR_IMPORT_EXPORT"));

		JMenuItem mntmImport = new JMenuItem(
				Utilities.getString("MENUBAR_IMPORT_EXPORT_IMPORT"));
		mntmImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(System
						.getProperty("user.dir"));
				int result = fc.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					try {
						FileInputStream fis = new FileInputStream(fc
								.getSelectedFile());
						db.importDatabase(fis);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		add(mntmImport);

		JMenuItem mntmExport = new JMenuItem(
				Utilities.getString("MENUBAR_IMPORT_EXPORT_EXPORT"));
		mntmExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(System
						.getProperty("user.dir"));
				int result = fc.showSaveDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					try {
						FileOutputStream fos = new FileOutputStream(fc
								.getSelectedFile());
						db.exportDatabase(fos);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		add(mntmExport);
	}

}
