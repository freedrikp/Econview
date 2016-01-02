package se.freedrikp.econview.database;

import java.awt.GridLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import se.freedrikp.econview.gui.Utilities;

public class Security extends Observable implements Observer {
	private Connection c;
	private SecureRandom rand;
	private MessageDigest digest;
	private Cipher cipher;
	private SecretKeySpec key;
	private IvParameterSpec iv;
	private String user;
	private File encDB;

	public Security(String dbfile) {
		try {
			File db = new File(dbfile);
			rand = new SecureRandom();
			digest = MessageDigest.getInstance("SHA-256");
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			if (!db.exists() || db.length() == 0) {
				initdb();
			}
		} catch (Exception e) {
			// System.err.println(e.getClass().getName() + ": " +
			// e.getMessage());
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void initdb() throws UnsupportedEncodingException {
		try {
			c.setAutoCommit(false);
			String sql = "CREATE TABLE Users(" + "username TEXT PRIMARY KEY,"
					+ "password TEXT," + "salt TEXT,"
					+ "admin INTEGER DEFAULT 0)";
			c.prepareStatement(sql).executeUpdate();
			String user = "admin";
			String password = "1234";
			addUser(user, password, true);
			c.commit();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Database openDatabase(String database) throws Exception {
		if (authenticate()) {
			String userDB = user + "_" + database;
			encDB = new File(userDB);
			boolean found = false;
			if (encDB.exists() && encDB.length() > 0) {
				decrypt(encDB, database);
				found = true;
			}
			Database db = new Database(database);
			db.addObserver(this);
			if (!found) {
				encrypt(encDB, database);
			}
			setChanged();
			notifyObservers();
			return db;
		}
		System.exit(0);
		return null;
	}

	private boolean authenticate() throws Exception {
		JPanel promptPanel = new JPanel();
		promptPanel.setLayout(new GridLayout(2, 2, 0, 0));
		promptPanel
				.add(new JLabel(Utilities.getString("PROMPT_USERNAME") + ":"));
		JTextField userField = new JTextField(15);
		promptPanel.add(userField);
		promptPanel
				.add(new JLabel(Utilities.getString("PROMPT_PASSWORD") + ":"));
		JPasswordField passField = new JPasswordField(15);
		promptPanel.add(passField);
		int result = JOptionPane.showConfirmDialog(null, promptPanel,
				Utilities.getString("USER_DETAILS_PROMPT"),
				JOptionPane.OK_CANCEL_OPTION);
		if (result == JOptionPane.OK_OPTION) {
			if (checkUser(userField.getText(),
					new String(passField.getPassword()))) {
				return true;
			} else {
				JOptionPane.showMessageDialog(null,
						Utilities.getString("PROMPT_ACCESS_DENIED"),
						Utilities.getString("USER_DETAILS_PROMPT"),
						JOptionPane.WARNING_MESSAGE);
			}
		}
		return false;
	}

	private boolean checkUser(String username, String password)
			throws Exception {
		String sql = "SELECT password,salt FROM Users WHERE username= ?";
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setString(1, username);
		ResultSet user = ps.executeQuery();
		if (user.next()) {
			String pass = user.getString("password");
			String salt = user.getString("salt");
			if (pass.equals(new String(digest.digest((password + salt)
					.getBytes("UTF-8")), "UTF-8"))) {
				byte[] temp = digest.digest((username + password + salt)
						.getBytes("UTF-8"));
				key = new SecretKeySpec(Arrays.copyOfRange(temp, 16, 32), "AES");
				iv = new IvParameterSpec(Arrays.copyOfRange(temp, 0, 16));
				this.user = username;
				return true;
			}
		}
		return false;
	}

	private void decrypt(File encrypted, String decrypted) throws Exception {
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		CipherInputStream cis = new CipherInputStream(new FileInputStream(
				encrypted), cipher);
		FileOutputStream fos = new FileOutputStream(decrypted);
		byte[] buffer = new byte[1024];
		int read = 0;
		while ((read = cis.read(buffer)) > -1) {
			fos.write(buffer, 0, read);
		}
		cis.close();
		fos.flush();
		fos.close();
	}

	private void encrypt(File encrypted, String decrypted) throws Exception {
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		FileInputStream fis = new FileInputStream(decrypted);
		CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(
				encrypted), cipher);
		byte[] buffer = new byte[1024];
		int read = 0;
		while ((read = fis.read(buffer)) > -1) {
			cos.write(buffer, 0, read);
		}
		fis.close();
		cos.flush();
		cos.close();
	}

	public void update(Observable o, Object arg) {
		try {
			encrypt(encDB, Utilities.getConfig("DATABASE_FILE"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void openFile(File selectedFile, Database db) {
		try {
			if (authenticate()) {
				db.close();
				if (new File(Utilities.getConfig("DATABASE_FILE")).delete()) {
					encDB = selectedFile;
					boolean found = false;
					if (encDB.exists() && encDB.length() > 0) {
						decrypt(encDB, Utilities.getConfig("DATABASE_FILE"));
						found = true;
					}
					db.openFile(new File(Utilities.getConfig("DATABASE_FILE")));
					if (!found) {
						encrypt(encDB, Utilities.getConfig("DATABASE_FILE"));
					}
					setChanged();
					notifyObservers();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveFile(File toFile) {
		try {
			if (authenticate()) {
				encrypt(toFile, Utilities.getConfig("DATABASE_FILE"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public File getFile() {
		return encDB;
	}

	public boolean addUser(String username, String password, boolean admin) {
		try {
			byte[] temp = new byte[10];
			rand.nextBytes(temp);
			String salt;
			salt = new String(temp, "UTF-8");

			password = new String(digest.digest((password + salt)
					.getBytes("UTF-8")), "UTF-8");

			String sql = "INSERT INTO Users VALUES (?,?,?,?)";
			PreparedStatement ps = c.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, password);
			ps.setString(3, salt);
			int ad = admin ? 1 : 0;
			ps.setInt(4, ad);
			ps.executeUpdate();
		} catch (SQLException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean changePassword(String username, String oldPass,
			String newPass,List<File> files) {
		try {
			if (checkUser(username, oldPass)) {
				HashMap<String,File> dec = new HashMap<String,File>();
				for (File f : files){
					String dest = "econview_temp_"+f.getName();
					decrypt(f,dest);
					dec.put(dest,f);
				}
				changePasswordAdmin(username, newPass);
				if (!checkUser(username,newPass)){
					return false;
				}
				for (Map.Entry<String, File> enc : dec.entrySet()){
					encrypt(enc.getValue(), enc.getKey());
					Files.delete(new File(enc.getKey()).toPath());
				}
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getUser() {
		return user;
	}

	public List<Object[]> listUsers() {
		try {
			String sql = "SELECT username,admin FROM Users";
			ResultSet users = c.prepareStatement(sql).executeQuery();
			ArrayList<Object[]> res = new ArrayList<Object[]>();
			while (users.next()) {
				Object[] entry = new Object[2];
				entry[0]=users.getString("username");
				entry[1]= users.getBoolean("admin");
				res.add(entry);
			}

			return res;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean removeUser(String username) {
		try {
			String sql = "DELETE FROM Users WHERE username=?";
			PreparedStatement ps = c.prepareStatement(sql);
			ps.setString(1, username);
			ps.executeUpdate();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isAdmin() {
		try {
			String sql = "SELECT username FROM Users WHERE admin=1";
			ResultSet results = c.prepareStatement(sql).executeQuery();
			if (results.next()) {
				return user.equals(results.getString("username"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setAdmin(String username) {
		try {
			c.setAutoCommit(false);
			String sql = "UPDATE Users SET admin=0 WHERE admin=1";
			c.prepareStatement(sql).executeUpdate();
			sql = "UPDATE Users SET admin=? WHERE username=?";
			PreparedStatement ps = c.prepareStatement(sql);
			ps.setInt(1, 1);
			ps.setString(2, username);
			ps.executeUpdate();
			c.commit();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void changePasswordAdmin(String username, String password) {
		try {
			byte[] temp = new byte[10];
			rand.nextBytes(temp);
			String salt;

			salt = new String(temp, "UTF-8");

			password = new String(digest.digest((password + salt)
					.getBytes("UTF-8")), "UTF-8");
			String sql = "UPDATE Users SET password=?, salt=? WHERE username=?";
			PreparedStatement ps = c.prepareStatement(sql);
			ps.setString(1, password);
			ps.setString(2, salt);
			ps.setString(3, username);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void close() throws SQLException {
		c.close();
	}
}
