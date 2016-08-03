package se.freedrikp.econview.gui;

import java.awt.EventQueue;
import java.io.File;

import javax.swing.UIManager;

import se.freedrikp.econview.common.Configuration;
import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.database.MySQLDatabase;
import se.freedrikp.econview.database.MySQLSecurity;
import se.freedrikp.econview.database.SQLiteDatabase;
import se.freedrikp.econview.database.SQLiteSecurity;
import se.freedrikp.econview.database.Security;
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
					if (Configuration.getString(
							"DATABASE_SYSTEM_SQLITE_OR_MYSQL").equals("SQLITE")) {
						File data = new File(Configuration
								.getString("DATABASE_DIRECTORY"));
						if (!data.exists()) {
							data.mkdirs();
						}
					}
					if (secure) {
						String database = null;
						Security security = null;
						if (Configuration.getString(
								"DATABASE_SYSTEM_SQLITE_OR_MYSQL").equals(
								"SQLITE")) {
							security = new SQLiteSecurity(Configuration
									.getString("DATABASE_DIRECTORY")
									+ "/"
									+ Configuration
											.getString("USERS_DATABASE_FILE"));
							database = Configuration
									.getString("DATABASE_DIRECTORY")
									+ "/"
									+ Configuration.getString("DATABASE_FILE");
						} else if (Configuration.getString(
								"DATABASE_SYSTEM_SQLITE_OR_MYSQL").equals(
								"MYSQL")) {
							security = new MySQLSecurity(Configuration
									.getString("MYSQL_DATABASE"), Configuration
									.getString("MYSQL_USERNAME"), Configuration
									.getString("MYSQL_PASSWORD"));
							database = Configuration
									.getString("MYSQL_DATABASE");
						}
						if (!security.usersExist()) {
							new AddUserDialog(security, true).showDialog();
						}
						AuthenticationDialog ad = new AuthenticationDialog();
						if (!ad.showDialog()) {
							System.exit(0);
						}
						Database db = security.openNewDatabase(database,
								ad.getUsername(), ad.getPassword());
						if (db == null) {
							ad.showFailedDialog(true);
						}
						frame = new MainFrame(db, security);
					} else {
						Database db = null;
						if (Configuration.getString(
								"DATABASE_SYSTEM_SQLITE_OR_MYSQL").equals(
								"SQLITE")) {
							db = new SQLiteDatabase(Configuration
									.getString("DATABASE_DIRECTORY")
									+ "/"
									+ Configuration.getString("DATABASE_FILE"));
						} else if (Configuration.getString(
								"DATABASE_SYSTEM_SQLITE_OR_MYSQL").equals(
								"MYSQL")) {
							db = new MySQLDatabase(Configuration
									.getString("MYSQL_DATABASE"), Configuration
									.getString("MYSQL_USERNAME"), Configuration
									.getString("MYSQL_PASSWORD"), "NOUSER");

						}
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
