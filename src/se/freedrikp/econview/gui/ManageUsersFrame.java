package se.freedrikp.econview.gui;

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

import se.freedrikp.econview.database.Security;
import se.freedrikp.econview.gui.GUI.Model;

public class ManageUsersFrame extends JFrame implements Observer {
	private static final String[] userHeader = {
			Utilities.getString("USER_HEADER_USERNAME"),
			Utilities.getString("USER_HEADER_ADMIN") };
	private Security sec;
	private JTable userTable;

	public ManageUsersFrame(final Security sec) {
		Utilities.getString("MENUBAR_MANAGE_USERS");
		JPanel userPanel = new JPanel();
		userPanel.setLayout(new GridLayout(1, 2, 0, 0));
		this.sec = sec;
		sec.addObserver(this);
		userTable = new JTable();
		userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		userTable.setAutoCreateRowSorter(true);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(userTable);
		userPanel.add(scrollPane);

		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(3, 1, 0, 0));
		userPanel.add(buttons);

		JButton setAdmin = new JButton(Utilities.getString("BUTTON_SET_ADMIN"));
		buttons.add(setAdmin);
		setAdmin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String user = (String) userTable.getModel().getValueAt(
						userTable.convertRowIndexToModel(userTable
								.getSelectedRow()), 0);
				if (user != null) {
					sec.setAdmin(user);
				}
			}
		});

		JButton changePassword = new JButton(
				Utilities.getString("MENUBAR_CHANGE_PASSWORD"));
		buttons.add(changePassword);
		changePassword.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String user = (String) userTable.getModel().getValueAt(
						userTable.convertRowIndexToModel(userTable
								.getSelectedRow()), 0);
				if (user != null) {
					JPanel promptPanel = new JPanel();
					promptPanel.setLayout(new GridLayout(2, 2, 0, 0));
					promptPanel.add(new JLabel(Utilities
							.getString("PROMPT_NEW_PASSWORD") + ":"));
					JPasswordField passField = new JPasswordField(15);
					promptPanel.add(passField);
					promptPanel.add(new JLabel(Utilities
							.getString("PROMPT_NEW_PASSWORD") + ":"));
					JPasswordField passField2 = new JPasswordField(15);
					promptPanel.add(passField2);
					int result = JOptionPane.showConfirmDialog(null,
							promptPanel,
							Utilities.getString("USER_DETAILS_PROMPT"),
							JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
						if (!Arrays.equals(passField.getPassword(),
								passField2.getPassword())) {
							JOptionPane.showMessageDialog(null,
									Utilities.getString("PASSWORDS_NOT_MATCH"),
									Utilities.getString("PASSWORD_ERROR"),
									JOptionPane.ERROR_MESSAGE);
							return;
						}
						sec.changePasswordAdmin(user,
								new String(passField.getPassword()));

					}
				}
			}

		});

		JButton removeUser = new JButton(Utilities.getString("REMOVE_USER"));
		buttons.add(removeUser);
		removeUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String user = (String) userTable.getModel().getValueAt(
						userTable.convertRowIndexToModel(userTable
								.getSelectedRow()), 0);
				if (user != null) {
					if (JOptionPane.showConfirmDialog(null,
							Utilities.getString("REMOVE_USER_PROMPT") + " -- "
									+ user, Utilities.getString("REMOVE_USER"),
							JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						sec.removeUser(user);
					}
				}
			}
		});

		int width = Integer.parseInt(Utilities.getConfig("USER_PANEL_WIDTH"));
		int height = Integer.parseInt(Utilities.getConfig("USER_PANEL_HEIGHT"));
		DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDisplayMode();
		setBounds((dm.getWidth() - width) / 2, (dm.getHeight() - height) / 2,
				width, height);
		setContentPane(userPanel);
		setVisible(true);
		update(sec, null);
	}

	public void update(Observable o, Object arg) {
		Model m = new Model(userHeader, 0);
		for (Object[] row : sec.listUsers()) {
			m.addRow(row);
		}
		userTable.setModel(m);
		GUI.resizeTable(userTable);
	}

}
