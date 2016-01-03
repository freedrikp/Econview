package se.freedrikp.econview.gui.menubar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.database.Security;
import se.freedrikp.econview.gui.GUI;
import se.freedrikp.econview.gui.Utilities;

public class FileMenu extends JMenu {
	private Database db;
	private Security sec;

	public FileMenu(Database db, Security sec, GUI gui) {
		super(Utilities.getString("MENUBAR_FILE"));
		this.db = db;
		this.sec = sec;
		JMenuItem mntmOpenDatabase = new JMenuItem(
				Utilities.getString("MENUBAR_FILE_OPEN_DATABASE"));
		mntmOpenDatabase.addActionListener(new OpenDatabaseListener(gui));
		add(mntmOpenDatabase);

		JMenuItem mntmSaveDatabaseAs = new JMenuItem(
				Utilities.getString("MENUBAR_FILE_SAVE_DATABASE_AS"));
		mntmSaveDatabaseAs.addActionListener(new SaveDatabaseListener(gui));
		add(mntmSaveDatabaseAs);
	}

	private class OpenDatabaseListener implements ActionListener {
		private GUI gui;

		public OpenDatabaseListener(GUI gui) {
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
				sec.openFile(fc.getSelectedFile(), db);
			}
		}
	}

	private class SaveDatabaseListener implements ActionListener {
		private GUI gui;

		public SaveDatabaseListener(GUI gui) {
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
				// File fromFile = db.getFile();
				// try {
				// FileInputStream fis = new FileInputStream(fromFile);
				// FileOutputStream fos = new FileOutputStream(toFile);
				// ProgressMonitor pm = new ProgressMonitor(menuBar, null,
				// Utilities.getString("COPYING_DATABASE"), 0,
				// (int) fromFile.length());
				// pm.setMillisToPopup(0);
				// pm.setMillisToDecideToPopup(0);
				// byte[] buffer = new byte[1024];
				// int bytesRead = 0;
				// long totalRead = 0;
				// while ((bytesRead = fis.read(buffer)) > -1) {
				// totalRead += bytesRead;
				// fos.write(buffer, 0, bytesRead);
				// pm.setProgress((int) totalRead);
				// }
				// fos.flush();
				// fos.close();
				// fis.close();
				//
				// } catch (FileNotFoundException e) {
				// e.printStackTrace();
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				sec.saveFile(toFile);
			}
		}
	}

}
