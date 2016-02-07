package se.freedrikp.econview.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.ProgressMonitor;

import se.freedrikp.econview.common.Language;
import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.database.Security;
import se.freedrikp.econview.gui.frames.MainFrame;

public class FileMenu extends JMenu {
	private Database db;
	private Security sec;

	public FileMenu(Database db, Security sec, MainFrame gui) {
		super(Language.getString("MENUBAR_FILE"));
		this.db = db;
		this.sec = sec;
		JMenuItem mntmOpenDatabase = new JMenuItem(
				Language.getString("MENUBAR_FILE_OPEN_DATABASE"));
		mntmOpenDatabase.addActionListener(new OpenDatabaseListener(gui));
		add(mntmOpenDatabase);

		JMenuItem mntmSaveDatabaseAs = new JMenuItem(
				Language.getString("MENUBAR_FILE_SAVE_DATABASE_AS"));
		mntmSaveDatabaseAs.addActionListener(new SaveDatabaseListener(gui));
		add(mntmSaveDatabaseAs);
	}

	private class OpenDatabaseListener implements ActionListener {
		private MainFrame gui;

		public OpenDatabaseListener(MainFrame gui) {
			this.gui = gui;
		}

		public void actionPerformed(ActionEvent arg0) {
			JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
			JFileChooser f = new JFileChooser();
			f.setDialogType(JFileChooser.OPEN_DIALOG);
			String text = f.getUI().getApproveButtonText(f);
			fc.setDialogTitle(text);
			int result = fc.showDialog(gui, text);
			if (result == JFileChooser.APPROVE_OPTION) {
				if (sec != null) {
					sec.openFile(fc.getSelectedFile(), db);
				} else {
					db.openFile(fc.getSelectedFile());
				}
			}
		}
	}

	private class SaveDatabaseListener implements ActionListener {
		private MainFrame gui;

		public SaveDatabaseListener(MainFrame gui) {
			this.gui = gui;
		}

		public void actionPerformed(ActionEvent arg0) {
			JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
			JFileChooser f = new JFileChooser();
			f.setDialogType(JFileChooser.SAVE_DIALOG);
			String text = f.getUI().getApproveButtonText(f);
			fc.setDialogTitle(text);
			int result = fc.showDialog(gui, text);
			if (result == JFileChooser.APPROVE_OPTION) {
				File toFile = fc.getSelectedFile();
				if (sec != null) {
					sec.saveFile(toFile);
				} else {
					File fromFile = db.getFile();
					try {
						FileInputStream fis = new FileInputStream(fromFile);
						FileOutputStream fos = new FileOutputStream(toFile);
						ProgressMonitor pm = new ProgressMonitor(gui, null,
								Language.getString("COPYING_DATABASE"), 0,
								(int) fromFile.length());
						pm.setMillisToPopup(0);
						pm.setMillisToDecideToPopup(0);
						byte[] buffer = new byte[1024];
						int bytesRead = 0;
						long totalRead = 0;
						while ((bytesRead = fis.read(buffer)) > -1) {
							totalRead += bytesRead;
							fos.write(buffer, 0, bytesRead);
							pm.setProgress((int) totalRead);
						}
						fos.flush();
						fos.close();
						fis.close();

					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}
	}

}
