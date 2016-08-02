package se.freedrikp.econview.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SQLiteSecurity extends SQLSecurity implements Observer {
	private Cipher cipher;
	private SecretKeySpec key;
	private IvParameterSpec iv;
	private File encDB;
	private String tempDBFile;

	public SQLiteSecurity(String securityDatabase) {
		super(securityDatabase,"org.sqlite.JDBC","jdbc:sqlite:"+securityDatabase);
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	protected void initdb() {
		File db = new File(securityDatabase);
		if (!db.exists() || db.length() == 0) {
			try {
				c.setAutoCommit(false);
				String sql = "CREATE TABLE Users("
						+ "username TEXT PRIMARY KEY," + "password BLOB,"
						+ "salt BLOB," + "admin INTEGER DEFAULT 0)";
				c.prepareStatement(sql).executeUpdate();
				c.commit();
				c.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Database openNewDatabaseHelper(String database) throws Exception {
		tempDBFile = database;
		String userDB;
		int indexOfPoint = database.lastIndexOf('.');
		if (indexOfPoint >= 0) {
			userDB = database.substring(0, indexOfPoint) + "_" + user
					+ database.substring(indexOfPoint, database.length());
		} else {
			userDB = database + "_" + user;
		}
		encDB = new File(userDB);
		boolean found = false;
		if (encDB.exists() && encDB.length() > 0) {
			decrypt(encDB, database);
			found = true;
		}
		SQLiteDatabase db = new SQLiteDatabase(database);
		db.addObserver(this);
		if (!found) {
			encrypt(encDB, database);
		}
		return db;
	}

	protected void checkUserSpecifics(String username, String password, String salt) throws UnsupportedEncodingException {
		byte[] temp = digest.digest((username + password + salt).getBytes("UTF-8"));
		key = new SecretKeySpec(Arrays.copyOfRange(temp, 16, 32), "AES");
		iv = new IvParameterSpec(Arrays.copyOfRange(temp, 0, 16));
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
			encrypt(encDB, tempDBFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void openDatabaseHelper(String selectedDatabase, Database db) throws Exception{
		if (new File(tempDBFile).delete()) {
			encDB = new File(selectedDatabase);
			boolean found = false;
			if (encDB.exists() && encDB.length() > 0) {
				decrypt(encDB, tempDBFile);
				found = true;
			}
			db.openDatabase(tempDBFile,"NULL","NULL",user);
			if (!found) {
				encrypt(encDB, tempDBFile);
			}
		}
	}

	public boolean saveDatabase(String destinationDatabase, String username, String password) {
		try {
			if (checkUser(username, password)) {
				encrypt(new File(destinationDatabase), tempDBFile);
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public String getDatabase() {
		return encDB.getAbsolutePath();
	}
	
	public boolean changePasswordHelper(String username, String newPass, List<File> files) throws Exception {
		HashMap<String, File> dec = new HashMap<String, File>();
		for (File f : files) {
			String dest = "econview_temp_" + f.getName();
			decrypt(f, dest);
			dec.put(dest, f);
		}
		changePasswordAdmin(username, newPass);
		if (!checkUser(username, newPass)) {
			return false;
		}
		for (Map.Entry<String, File> enc : dec.entrySet()) {
			encrypt(enc.getValue(), enc.getKey());
			Files.delete(new File(enc.getKey()).toPath());
		}
		return true;
	}

	public void close() throws SQLException {
		try {
			c.close();
			c = DriverManager.getConnection("jdbc:sqlite:" + securityDatabase);
			c.prepareStatement("VACUUM").executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		c.close();
	}
}
