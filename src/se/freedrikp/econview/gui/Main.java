package se.freedrikp.econview.gui;

import java.awt.EventQueue;

import javax.swing.UIManager;

import se.freedrikp.econview.common.Configuration;
import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.database.Security;
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
					if (secure) {
						Security security = new Security(Configuration
								.getString("USERS_DATABASE_FILE"));
						Database db = security.openDatabase(Configuration
								.getString("DATABASE_FILE"));
						frame = new MainFrame(db, security);
					} else {
						Database db = new Database(Configuration
								.getString("DATABASE_FILE"));
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
