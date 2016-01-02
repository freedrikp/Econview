package se.freedrikp.econview.gui;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.database.Security;
import se.freedrikp.econview.gui.GUI.Model;

public class MenuBar extends JMenuBar {

	private Database db;
	private Security sec;
	private static final String[] userHeader = {
			Utilities.getString("USER_HEADER_USERNAME"),
			Utilities.getString("USER_HEADER_ADMIN") };

	public MenuBar(final Database db, final Security sec) {
		super();
		this.db = db;
		this.sec = sec;
		JMenu mnFile = new JMenu(Utilities.getString("MENUBAR_FILE"));
		add(mnFile);

		JMenuItem mntmOpenDatabase = new JMenuItem(
				Utilities.getString("MENUBAR_FILE_OPEN_DATABASE"));
		mntmOpenDatabase.addActionListener(new OpenDatabaseListener(this));
		mnFile.add(mntmOpenDatabase);

		JMenuItem mntmSaveDatabaseAs = new JMenuItem(
				Utilities.getString("MENUBAR_FILE_SAVE_DATABASE_AS"));
		mntmSaveDatabaseAs.addActionListener(new SaveDatabaseListener(this));
		mnFile.add(mntmSaveDatabaseAs);

		JMenu mnImportExport = new JMenu(
				Utilities.getString("MENUBAR_IMPORT_EXPORT"));
		add(mnImportExport);

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
		mnImportExport.add(mntmImport);

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
		mnImportExport.add(mntmExport);

		JMenu mnHidden = new JMenu(Utilities.getString("MENUBAR_HIDDEN"));
		add(mnHidden);

		final JCheckBoxMenuItem mntmShowHidden = new JCheckBoxMenuItem(
				Utilities.getString("MENUBAR_HIDDEN_SHOW_HIDDEN"),
				db.getShowHidden());
		mntmShowHidden.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				db.setShowHidden(mntmShowHidden.getState());
			}
		});
		mnHidden.add(mntmShowHidden);

		JMenu mnSettings = new JMenu(Utilities.getString("MENUBAR_SETTINGS"));
		add(mnSettings);

		JMenuItem mntmConfig = new JMenuItem(
				Utilities.getString("MENUBAR_SETTINGS_CONFIGURATION"));
		mntmConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JPanel panel = new JPanel();
				panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
				JScrollPane scrollPane = new JScrollPane();
				HashMap<String, JTextField> map = new HashMap<String, JTextField>();
				scrollPane.setViewportView(panel);
				for (Map.Entry<String, String> config : Utilities
						.listAllConfigs().entrySet()) {
					JPanel pan = new JPanel();
					pan.add(new JLabel(config.getKey() + ":"));
					JTextField field = new JTextField(config.getValue(), 10);
					pan.add(field);
					panel.add(pan);
					map.put(config.getKey(), field);
				}
				scrollPane.setPreferredSize(new Dimension(
						Integer.parseInt(Utilities
								.getConfig("SETTINGS_CONFIGURATION_PANEL_WIDTH")),
						Integer.parseInt(Utilities
								.getConfig("SETTINGS_CONFIGURATION_PANEL_HEIGHT"))));
				int result = JOptionPane.showConfirmDialog(null, scrollPane,
						Utilities.getString("SETTINGS_CONFIGURATION"),
						JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					for (Map.Entry<String, JTextField> entry : map.entrySet()) {
						Utilities.putConfig(entry.getKey(), entry.getValue()
								.getText());
					}
				}
			}
		});
		mnSettings.add(mntmConfig);

		JMenu users = new JMenu(Utilities.getString("MENUBAR_USERS"));
		add(users);
		JMenuItem addUser = new JMenuItem(
				Utilities.getString("MENUBAR_ADD_USER"));
		users.add(addUser);
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
				if (!Arrays.equals(passField.getPassword(),
						passField2.getPassword())) {
					JOptionPane.showMessageDialog(null,
							Utilities.getString("PASSWORDS_NOT_MATCH"),
							Utilities.getString("PASSWORD_ERROR"),
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				int result = JOptionPane.showConfirmDialog(null, promptPanel,
						Utilities.getString("USER_DETAILS_PROMPT"),
						JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					if (!sec.addUser(userField.getText(),
							new String(passField.getPassword()), false)) {
						JOptionPane.showMessageDialog(null,
								Utilities.getString("USER_EXISTS"),
								Utilities.getString("USER_ERROR"),
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		JMenuItem changePassword = new JMenuItem(
				Utilities.getString("MENUBAR_CHANGE_PASSWORD"));
		users.add(changePassword);
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
									passField.getPassword()),files)) {
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
		users.add(manageUsers);
		manageUsers.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (sec.isAdmin()) {
					JPanel userPanel = new JPanel();
					userPanel.setLayout(new GridLayout(1, 2, 0, 0));
					final JTable userTable = new JTable();
					userTable
							.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					userTable.setAutoCreateRowSorter(true);
					Model m = new Model(userHeader, 0);
					for (Object[] row : sec.listUsers()) {
						m.addRow(row);
					}
					userTable.setModel(m);
					JScrollPane scrollPane = new JScrollPane();
					scrollPane.setViewportView(userTable);
					GUI.resizeTable(userTable);
					userPanel.add(scrollPane);

					JPanel buttons = new JPanel();
					buttons.setLayout(new GridLayout(3, 1, 0, 0));
					userPanel.add(buttons);

					JButton setAdmin = new JButton(Utilities
							.getString("BUTTON_SET_ADMIN"));
					buttons.add(setAdmin);
					setAdmin.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String user = (String) userTable.getModel()
									.getValueAt(userTable.getSelectedRow(), 0);
							if (user != null) {
								sec.setAdmin(user);
								Model m = new Model(userHeader, 0);
								for (Object[] row : sec.listUsers()) {
									m.addRow(row);
								}
								userTable.setModel(m);
								GUI.resizeTable(userTable);
							}
						}
					});

					JButton changePassword = new JButton(Utilities
							.getString("MENUBAR_CHANGE_PASSWORD"));
					buttons.add(changePassword);
					changePassword.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String user = (String) userTable.getModel()
									.getValueAt(userTable.getSelectedRow(), 0);
							if (user != null) {
								JPanel promptPanel = new JPanel();
								promptPanel
										.setLayout(new GridLayout(2, 2, 0, 0));
								promptPanel.add(new JLabel(Utilities
										.getString("PROMPT_NEW_PASSWORD") + ":"));
								JPasswordField passField = new JPasswordField(
										15);
								promptPanel.add(passField);
								promptPanel.add(new JLabel(Utilities
										.getString("PROMPT_NEW_PASSWORD") + ":"));
								JPasswordField passField2 = new JPasswordField(
										15);
								promptPanel.add(passField2);
								int result = JOptionPane.showConfirmDialog(
										null,
										promptPanel,
										Utilities
												.getString("USER_DETAILS_PROMPT"),
										JOptionPane.OK_CANCEL_OPTION);
								if (result == JOptionPane.OK_OPTION) {
									if (!Arrays.equals(passField.getPassword(),
											passField2.getPassword())) {
										JOptionPane
												.showMessageDialog(
														null,
														Utilities
																.getString("PASSWORDS_NOT_MATCH"),
														Utilities
																.getString("PASSWORD_ERROR"),
														JOptionPane.ERROR_MESSAGE);
										return;
									}
									sec.changePasswordAdmin(user, new String(
											passField.getPassword()));

								}
							}
						}

					});

					JButton removeUser = new JButton(Utilities
							.getString("REMOVE_USER"));
					buttons.add(removeUser);
					removeUser.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							String user = (String) userTable.getModel()
									.getValueAt(userTable.getSelectedRow(), 0);
							if (user != null) {
								if (JOptionPane.showConfirmDialog(
										null,
										Utilities
												.getString("REMOVE_USER_PROMPT")
												+ " -- " + user, Utilities
												.getString("REMOVE_USER"),
										JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
									sec.removeUser(user);
									Model m = new Model(userHeader, 0);
									for (Object[] row : sec.listUsers()) {
										m.addRow(row);
									}
									userTable.setModel(m);
									GUI.resizeTable(userTable);
								}
							}
						}
					});

					JFrame frame = new JFrame(Utilities
							.getString("MENUBAR_MANAGE_USERS"));
					int width = Integer.parseInt(Utilities
							.getConfig("USER_PANEL_WIDTH"));
					int height = Integer.parseInt(Utilities
							.getConfig("USER_PANEL_HEIGHT"));
					DisplayMode dm = GraphicsEnvironment
							.getLocalGraphicsEnvironment()
							.getDefaultScreenDevice().getDisplayMode();
					frame.setBounds((dm.getWidth() - width) / 2,
							(dm.getHeight() - height) / 2, width, height);
					frame.setContentPane(userPanel);
					frame.setVisible(true);
				}
			}
		});

		JMenu delete = new JMenu(Utilities.getString("MENUBAR_DELETE"));
		add(delete);

		JMenuItem deleteAccounts = new JMenuItem(
				Utilities.getString("MENUBAR_DELETE_ACCOUNTS"));
		delete.add(deleteAccounts);
		deleteAccounts.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(null,
						Utilities.getString("PROMPT_DELETE_ACCOUNTS"),
						Utilities.getString("MENUBAR_DELETE_ACCOUNTS"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					db.deleteAccounts();
				}
			}
		});

		JMenuItem deleteTransactions = new JMenuItem(
				Utilities.getString("MENUBAR_DELETE_TRANSACTIONS"));
		delete.add(deleteTransactions);
		deleteTransactions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(null,
						Utilities.getString("PROMPT_DELETE_TRANSACTIONS"),
						Utilities.getString("MENUBAR_DELETE_TRANSACTIONS"),
						JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
					db.deleteTransactions();
				}
			}
		});
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
			int result = fc.showDialog(menuBar, text);
			if (result == JFileChooser.APPROVE_OPTION) {
				sec.openFile(fc.getSelectedFile(), db);
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
			int result = fc.showDialog(menuBar, text);
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
