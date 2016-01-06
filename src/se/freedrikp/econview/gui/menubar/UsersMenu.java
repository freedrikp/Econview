package se.freedrikp.econview.gui.menubar;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import se.freedrikp.econview.database.Security;
import se.freedrikp.econview.gui.ManageUsersFrame;
import se.freedrikp.econview.gui.Utilities;

public class UsersMenu extends JMenu {

	public UsersMenu(final Security sec) {
		super(Utilities.getString("MENUBAR_USERS"));

		JMenuItem addUser = new JMenuItem(
				Utilities.getString("MENUBAR_ADD_USER"));
		add(addUser);
		addUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPanel promptPanel = new JPanel();
				promptPanel.setLayout(new GridLayout(3, 2, 0, 0));
				promptPanel.add(new JLabel(Utilities
						.getString("PROMPT_USERNAME") + ":"));
				JTextField userField = new JTextField(15);
				promptPanel.add(userField);
				promptPanel.add(new JLabel(Utilities
						.getString("PROMPT_PASSWORD") + ":"));
				JPasswordField passField = new JPasswordField(15);
				promptPanel.add(passField);
				promptPanel.add(new JLabel(Utilities
						.getString("PROMPT_PASSWORD") + ":"));
				JPasswordField passField2 = new JPasswordField(15);
				promptPanel.add(passField2);
				boolean matched = false;
				while (!matched) {
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
						} else {
							if (!sec.addUser(userField.getText(), new String(
									passField.getPassword()), false)) {
								JOptionPane.showMessageDialog(null,
										Utilities.getString("USER_EXISTS"),
										Utilities.getString("USER_ERROR"),
										JOptionPane.ERROR_MESSAGE);
							}
							matched = true;
						}
					} else {
						matched = true;
					}
				}
			}
		});
		JMenuItem changePassword = new JMenuItem(
				Utilities.getString("MENUBAR_CHANGE_PASSWORD"));
		add(changePassword);
		changePassword.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPanel promptPanel = new JPanel();
				promptPanel.setLayout(new GridLayout(4, 2, 0, 0));
				promptPanel.add(new JLabel(Utilities
						.getString("PROMPT_USERNAME") + ":"));
				promptPanel.add(new JLabel(sec.getUser()));
				promptPanel.add(new JLabel(Utilities
						.getString("PROMPT_OLD_PASSWORD") + ":"));
				JPasswordField oldPass = new JPasswordField(15);
				promptPanel.add(oldPass);
				promptPanel.add(new JLabel(Utilities
						.getString("PROMPT_NEW_PASSWORD") + ":"));
				JPasswordField passField = new JPasswordField(15);
				promptPanel.add(passField);
				promptPanel.add(new JLabel(Utilities
						.getString("PROMPT_NEW_PASSWORD") + ":"));
				JPasswordField passField2 = new JPasswordField(15);
				promptPanel.add(passField2);
				int result = JOptionPane.showConfirmDialog(null, promptPanel,
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
					final LinkedList<File> files = new LinkedList<File>();
					files.add(sec.getFile().getAbsoluteFile());
					JPanel filePanel = new JPanel();
					filePanel.setLayout(new GridLayout(3, 1, 0, 0));
					final JList fileList = new JList();
					fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					fileList.setListData(files.toArray());
					filePanel.add(new JLabel(Utilities
							.getString("CHANGE_PASSWORD_FILES")));
					JScrollPane scrollPane = new JScrollPane();
					scrollPane.setViewportView(fileList);
					filePanel.add(scrollPane);
					JPanel buttonPanel = new JPanel();
					JButton addFile = new JButton("+");
					addFile.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							JFileChooser fc = new JFileChooser(System
									.getProperty("user.dir"));
							fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
							fc.setMultiSelectionEnabled(true);
							int result = fc.showOpenDialog(null);
							if (result == JFileChooser.APPROVE_OPTION) {
								for (File f : fc.getSelectedFiles()) {
									files.add(f);
								}
								fileList.setListData(files.toArray());
								System.out.println("hej");
							}
						}
					});
					JButton removeFile = new JButton("-");
					removeFile.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							files.remove(fileList.getSelectedValue());
							fileList.setListData(files.toArray());
						}
					});

					buttonPanel.add(addFile);
					buttonPanel.add(removeFile);
					filePanel.add(buttonPanel);

					if (JOptionPane.showConfirmDialog(null, filePanel,
							Utilities.getString("MENUBAR_CHANGE_PASSWORD"),
							JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) {
						return;
					}

					if (!sec.changePassword(sec.getUser(),
							new String(oldPass.getPassword()), new String(
									passField.getPassword()), files)) {
						JOptionPane.showMessageDialog(null,
								Utilities.getString("PROMPT_ACCESS_DENIED"),
								Utilities.getString("USER_DETAILS_PROMPT"),
								JOptionPane.WARNING_MESSAGE);
					}
				}
			}
		});
		JMenuItem manageUsers = new JMenuItem(
				Utilities.getString("MENUBAR_MANAGE_USERS"));
		add(manageUsers);
		manageUsers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (sec.isAdmin()) {
					new ManageUsersFrame(sec);
				}
			}
		});
	}
}
