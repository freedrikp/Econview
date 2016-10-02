package se.freedrikp.econview.database;

import java.io.File;
import java.io.UnsupportedEncodingException;
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
import java.util.List;

public abstract class SQLSecurity extends Security {
	protected Connection c;
	protected SecureRandom rand;
	protected MessageDigest digest;
	protected String user;
	protected String securityDatabase;

	public SQLSecurity(String securityDatabase, String sqlClass,
			String connectionString) {
		this.securityDatabase = securityDatabase;
		try {
			rand = new SecureRandom();
			digest = MessageDigest.getInstance("SHA-256");
			Class.forName(sqlClass).newInstance();
			c = DriverManager.getConnection(connectionString);
			initdb();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException
				| NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public SQLSecurity(String securityDatabase, String sqlClass,
			String connectionString, String username, String password) {
		this.securityDatabase = securityDatabase;
		try {
			rand = new SecureRandom();
			digest = MessageDigest.getInstance("SHA-256");
			Class.forName(sqlClass).newInstance();
			c = DriverManager.getConnection(connectionString, username,
					password);
			initdb();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException
				| NoSuchAlgorithmException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	protected abstract void initdb();

	protected boolean checkUser(String username, String password)
			throws SQLException, UnsupportedEncodingException {
		String sql = "SELECT password,salt FROM Users WHERE username= ?";
		PreparedStatement ps = c.prepareStatement(sql);
		ps.setString(1, username);
		ResultSet user = ps.executeQuery();
		if (user.next()) {
			byte[] pass = user.getBytes("password");
			byte[] salt = user.getBytes("salt");
			digest.update(password.getBytes("UTF-8"));
			digest.update(salt);
			if (Arrays.equals(pass, digest.digest())) {
				checkUserSpecifics(username, password, salt);
				this.user = username;
				setChanged();
				notifyObservers();
				return true;
			}
		}
		return false;
	}

	protected abstract void checkUserSpecifics(String username,
			String password, byte[] salt) throws UnsupportedEncodingException;

	public Database openNewDatabase(String database, String username,
			String password) throws Exception {
		if (checkUser(username, password)) {
			Database db = openNewDatabaseHelper(database);
			setChanged();
			notifyObservers();
			return db;
		}
		return null;
	}

	public abstract Database openNewDatabaseHelper(String database)
			throws Exception;

	public boolean openDatabase(String selectedDatabase, String dbUsername,
			String dbPassword, Database db, String username, String password) {
		try {
			if (checkUser(username, password)) {
				db.close();
				openDatabaseHelper(selectedDatabase, dbUsername, dbPassword,
						db, username);
				setChanged();
				notifyObservers();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public abstract void openDatabaseHelper(String selectedDatabase,
			String dbUsername, String dbPassword, Database db, String username)
			throws Exception;

	public abstract boolean saveDatabase(String destinationDatabase,
			String username, String password);

	public abstract String getDatabase();

	public boolean addUser(String username, String password, boolean admin) {
		try {
			byte[] salt = new byte[10];
			rand.nextBytes(salt);
			
			digest.update(password.getBytes("UTF-8"));
			digest.update(salt);

			String sql = "INSERT INTO Users VALUES (?,?,?,?)";
			PreparedStatement ps = c.prepareStatement(sql);
			ps.setString(1, username);
			ps.setBytes(2, digest.digest());
			ps.setBytes(3, salt);
			int ad = admin ? 1 : 0;
			ps.setInt(4, ad);
			ps.executeUpdate();
		} catch (SQLException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		}
		setChanged();
		notifyObservers();
		return true;
	}

	public boolean changePassword(String username, String oldPass,
			String newPass, List<File> files) {
		try {
			if (checkUser(username, oldPass)) {
				if (!changePasswordHelper(username, newPass, files)) {
					return false;
				}
				setChanged();
				notifyObservers();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public abstract boolean changePasswordHelper(String username,
			String newPass, List<File> files) throws Exception;

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
				entry[0] = users.getString("username");
				entry[1] = users.getBoolean("admin");
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
			setChanged();
			notifyObservers();
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
		setChanged();
		notifyObservers();
	}

	public void changePasswordAdmin(String username, String password) {
		try {
			byte[] salt = new byte[10];
			rand.nextBytes(salt);
			
			digest.update(password.getBytes("UTF-8"));
			digest.update(salt);
			
			String sql = "UPDATE Users SET password=?, salt=? WHERE username=?";
			PreparedStatement ps = c.prepareStatement(sql);
			ps.setBytes(1, digest.digest());
			ps.setBytes(2, salt);
			ps.setString(3, username);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public abstract void close() throws SQLException;

	public boolean usersExist() {
		try {
			String sql = "SELECT COUNT(username) as num FROM Users";
			ResultSet nbrUsers = c.prepareStatement(sql).executeQuery();
			if (nbrUsers.next()) {
				int num = nbrUsers.getInt("num");
				return num > 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

}