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

public class MySQLSecurity extends SQLSecurity {
	private String username;
	private String password;

	public MySQLSecurity(String securityDatabase, String username, String password) {
		super(securityDatabase, "com.mysql.jdbc.Driver",
				"jdbc:mysql://" + securityDatabase,username,password);
		this.username = username;
		this.password = password;
	}

	protected void initdb() {
		try {
		PreparedStatement ps = c
				.prepareStatement("SELECT count(*) as count FROM information_schema.TABLES WHERE (TABLE_SCHEMA = 'econview') AND (TABLE_NAME = 'Users')");
		ResultSet results;
			results = ps.executeQuery();
			if (results.next()) {
				int count = results.getInt("count");
				if (count == 0) {
					c.setAutoCommit(false);
					String sql = "CREATE TABLE Users("
							+ "username varchar(30) PRIMARY KEY," + "password TEXT,"
							+ "salt TEXT," + "admin INTEGER DEFAULT 0)";
					c.prepareStatement(sql).executeUpdate();
					c.commit();
					c.setAutoCommit(true);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Database openNewDatabaseHelper(String database) throws Exception {
		Database db = new MySQLDatabase(database,username,password);
		return db;
	}

	protected void checkUserSpecifics(String username, String password,
			String salt) throws UnsupportedEncodingException {
		return;
	}

	public void openDatabaseHelper(String selectedDatabase, Database db)
			throws Exception {
			db.openDatabase(selectedDatabase);
	}

	public boolean saveDatabase(String destinationDatabase, String username,
			String password) {
		return false;
	}

	public String getDatabase() {
		return securityDatabase;
	}

	public boolean changePasswordHelper(String username, String newPass,
			List<File> files) throws Exception {
		changePasswordAdmin(username, newPass);
		if (!checkUser(username, newPass)) {
			return false;
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
