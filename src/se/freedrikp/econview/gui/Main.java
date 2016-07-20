package se.freedrikp.econview.gui;

import java.awt.EventQueue;
import java.io.File;

import javax.swing.UIManager;

import se.freedrikp.econview.common.Configuration;
import se.freedrikp.econview.database.SQLiteDatabase;
import se.freedrikp.econview.database.SQLiteSecurity;
import se.freedrikp.econview.gui.dialogs.AddUserDialog;
import se.freedrikp.econview.gui.dialogs.AuthenticationDialog;
import se.freedrikp.econview.gui.frames.MainFrame;

public class Main {
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					boolean secure = Configuration
							.getBoolean("SECURITY_TRUE_FALSE");
					MainFrame frame;
					File data = new File(Configuration
							.getString("DATABASE_DIRECTORY"));
					if (!data.exists()) {
						data.mkdirs();
					}
					if (secure) {
						SQLiteSecurity security = new SQLiteSecurity(Configuration
								.getString("DATABASE_DIRECTORY")
								+ "/"
								+ Configuration
										.getString("USERS_DATABASE_FILE"));
						if (!security.usersExist()) {
							new AddUserDialog(security, true).showDialog();
						}
						AuthenticationDialog ad = new AuthenticationDialog();
						if (!ad.showDialog()) {
							System.exit(0);
						}
						SQLiteDatabase db = security.openDatabase(
								Configuration.getString("DATABASE_DIRECTORY")
										+ "/"
										+ Configuration
												.getString("DATABASE_FILE"),
								ad.getUsername(), ad.getPassword());
						if (db == null) {
							ad.showFailedDialog(true);
						}
						frame = new MainFrame(db, security);
					} else {
						SQLiteDatabase db = new SQLiteDatabase(Configuration
								.getString("DATABASE_DIRECTORY")
								+ "/"
								+ Configuration.getString("DATABASE_FILE"));
						frame = new MainFrame(db, null);
					}
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
}
