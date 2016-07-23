package se.freedrikp.econview.gui.frames;

import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import se.freedrikp.econview.common.Configuration;
import se.freedrikp.econview.common.Language;
import se.freedrikp.econview.database.SQLiteSecurity;
import se.freedrikp.econview.database.Security;
import se.freedrikp.econview.gui.frames.MainFrame.Model;

public class ManageUsersFrame extends JFrame implements Observer {
	private static final String[] userHeader = {
			Language.getString("USER_HEADER_USERNAME"),
			Language.getString("USER_HEADER_ADMIN") };
	private Security sec;
	private JTable userTable;

	public ManageUsersFrame(final Security security) {
		super(Language.getString("MENUBAR_MANAGE_USERS"));
		JPanel userPanel = new JPanel();
		userPanel.setLayout(new GridLayout(1, 2, 0, 0));
		this.sec = security;
		security.addObserver(this);
		userTable = new JTable();
		userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		userTable.setAutoCreateRowSorter(true);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(userTable);
		userPanel.add(scrollPane);

		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(3, 1, 0, 0));
		userPanel.add(buttons);

		JButton setAdmin = new JButton(Language.getString("BUTTON_SET_ADMIN"));
		buttons.add(setAdmin);
		setAdmin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String user = (String) userTable.getModel().getValueAt(
						userTable.convertRowIndexToModel(userTable
								.getSelectedRow()), 0);
				if (user != null) {
					security.setAdmin(user);
				}
			}
		});

		JButton changePassword = new JButton(
				Language.getString("MENUBAR_CHANGE_PASSWORD"));
		buttons.add(changePassword);
		changePassword.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String user = (String) userTable.getModel().getValueAt(
						userTable.convertRowIndexToModel(userTable
								.getSelectedRow()), 0);
				if (user != null) {
					JPanel promptPanel = new JPanel();
					promptPanel.setLayout(new GridLayout(2, 2, 0, 0));
					promptPanel.add(new JLabel(Language
							.getString("PROMPT_NEW_PASSWORD") + ":"));
					JPasswordField passField = new JPasswordField(15);
					promptPanel.add(passField);
					promptPanel.add(new JLabel(Language
							.getString("PROMPT_NEW_PASSWORD") + ":"));
					JPasswordField passField2 = new JPasswordField(15);
					promptPanel.add(passField2);
					int result = JOptionPane.showConfirmDialog(null,
							promptPanel,
							Language.getString("USER_DETAILS_PROMPT"),
							JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
						if (!Arrays.equals(passField.getPassword(),
								passField2.getPassword())) {
							JOptionPane.showMessageDialog(null,
									Language.getString("PASSWORDS_NOT_MATCH"),
									Language.getString("PASSWORD_ERROR"),
									JOptionPane.ERROR_MESSAGE);
							return;
						}
						security.changePasswordAdmin(user,
								new String(passField.getPassword()));

					}
				}
			}

		});

		JButton removeUser = new JButton(Language.getString("REMOVE_USER"));
		buttons.add(removeUser);
		removeUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String user = (String) userTable.getModel().getValueAt(
						userTable.convertRowIndexToModel(userTable
								.getSelectedRow()), 0);
				if (user != null) {
					if (JOptionPane.showConfirmDialog(null,
							Language.getString("REMOVE_USER_PROMPT") + " -- "
									+ user, Language.getString("REMOVE_USER"),
							JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						security.removeUser(user);
					}
				}
			}
		});

		int width = Configuration.getInt("USER_PANEL_WIDTH");
		int height = Configuration.getInt("USER_PANEL_HEIGHT");
		DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDisplayMode();
		setBounds((dm.getWidth() - width) / 2, (dm.getHeight() - height) / 2,
				width, height);
		setContentPane(userPanel);
		setVisible(true);
		update(security, null);
	}

	public void update(Observable o, Object arg) {
		Model m = new Model(userHeader, 0);
		for (Object[] row : sec.listUsers()) {
			m.addRow(row);
		}
		userTable.setModel(m);
		MainFrame.resizeTable(userTable);
	}

}
