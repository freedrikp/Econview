package se.freedrikp.econview.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.ProgressMonitor;

import se.freedrikp.econview.database.Database;

public class MenuBar extends JMenuBar {

	private Database db;

	public MenuBar(final Database db){
		super();
		this.db = db;
		JMenu mnFile = new JMenu(Utilities.getString("MENUBAR_FILE"));
		add(mnFile);

		JMenuItem mntmOpenDatabase = new JMenuItem(Utilities.getString("MENUBAR_FILE_OPEN_DATABASE"));
		mntmOpenDatabase.addActionListener(new OpenDatabaseListener(this));
		mnFile.add(mntmOpenDatabase);

		JMenuItem mntmSaveDatabaseAs = new JMenuItem(Utilities.getString("MENUBAR_FILE_SAVE_DATABASE_AS"));
		mntmSaveDatabaseAs.addActionListener(new SaveDatabaseListener(this));
		mnFile.add(mntmSaveDatabaseAs);

		JMenu mnImportExport = new JMenu(Utilities.getString("MENUBAR_IMPORT_EXPORT"));
		add(mnImportExport);

		JMenuItem mntmImport = new JMenuItem(Utilities.getString("MENUBAR_IMPORT_EXPORT_IMPORT"));
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
		mnImportExport.add(mntmImport);

		JMenuItem mntmExport = new JMenuItem(Utilities.getString("MENUBAR_IMPORT_EXPORT_EXPORT"));
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
		mnImportExport.add(mntmExport);
		
		JMenu mnIncluded = new JMenu(Utilities.getString("MENUBAR_INCLUDED"));
		add(mnIncluded);

		final JCheckBoxMenuItem mntmShowHideIncluded = new JCheckBoxMenuItem(Utilities.getString("MENUBAR_INCLUDED_SHOW_ONLY_INCLUDED"),db.getOnlyIncluded());
		mntmShowHideIncluded.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				db.setOnlyIncluded(mntmShowHideIncluded.getState());
			}
		});
		mnIncluded.add(mntmShowHideIncluded);
	}
	
	private class OpenDatabaseListener implements ActionListener {
		private MenuBar menuBar;

		public OpenDatabaseListener(MenuBar menuBar) {
			this.menuBar = menuBar;
		}

		public void actionPerformed(ActionEvent arg0) {
			JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
			JFileChooser f = new JFileChooser();
			f.setDialogType(JFileChooser.OPEN_DIALOG);
			String text = f.getUI().getApproveButtonText(f);
			fc.setDialogTitle(text);
			int result = fc.showDialog(menuBar,text);
			if (result == JFileChooser.APPROVE_OPTION) {
				db.openFile(fc.getSelectedFile());
			}
		}
	}

	private class SaveDatabaseListener implements ActionListener {
		private MenuBar menuBar;

		public SaveDatabaseListener(MenuBar menuBar) {
			this.menuBar = menuBar;
		}

		public void actionPerformed(ActionEvent arg0) {
			JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
			JFileChooser f = new JFileChooser();
			f.setDialogType(JFileChooser.SAVE_DIALOG);
			String text = f.getUI().getApproveButtonText(f);
			fc.setDialogTitle(text);
			int result = fc.showDialog(menuBar,text);
			if (result == JFileChooser.APPROVE_OPTION) {
				File toFile = fc.getSelectedFile();
				File fromFile = db.getFile();
				try {
					FileInputStream fis = new FileInputStream(fromFile);
					FileOutputStream fos = new FileOutputStream(toFile);
					ProgressMonitor pm = new ProgressMonitor(menuBar, null,
							Utilities.getString("COPYING_DATABASE"), 0, (int) fromFile.length());
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
