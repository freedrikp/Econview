package se.freedrikp.econview.database;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ProgressMonitor;

import se.freedrikp.econview.common.Common;

public class MySQLDatabase extends SQLDatabase {
	private String username;
	private boolean foreign;

	public MySQLDatabase(String database, String dbUsername, String dbPassword,
			String username, boolean foreign) {
		super(database, "com.mysql.jdbc.Driver", "jdbc:mysql://" + database,
				dbUsername, dbPassword);
		this.username = username;
		this.foreign = foreign;
	}

	private boolean tableExists(String tableName) {
		PreparedStatement ps;
		try {
			ps = c.prepareStatement("SELECT count(*) as count FROM information_schema.TABLES WHERE (TABLE_SCHEMA = ?) AND (TABLE_NAME = ?)");
			ps.setString(1, database.substring(database.lastIndexOf('/') + 1));
			ps.setString(2, tableName);
			ResultSet results = ps.executeQuery();
			if (results.next()) {
				int count = results.getInt("count");
				return count > 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void initdb() {
		try {
			c.setAutoCommit(false);
			if (!tableExists("Accounts")) {
				String sql = "CREATE TABLE Accounts("
						+ "accountName varchar(100),"
						+ "accountBalance REAL DEFAULT 0.0,"
						+ "accountHidden INTEGER DEFAULT '1',"
						+ "username varchar(30),";
				if (foreign) {
					sql += "FOREIGN KEY (username) REFERENCES Users(username),";
				}
				sql += "PRIMARY KEY (accountName,username)" + ")";
				AutoPreparedStatement.create(c, sql).executeUpdate();
			}
			if (!tableExists("Transactions")) {
				String sql = "CREATE TABLE Transactions("
						+ "transactionID INTEGER PRIMARY KEY AUTO_INCREMENT,"
						+ "accountName varchar(100),"
						+ "transactionAmount REAL," + "transactionDate DATE,"
						+ "transactionComment TEXT," + "username varchar(30)";
				if (foreign) {
					sql += ",FOREIGN KEY (username) REFERENCES Users(username)";
				}
				sql += ")";
				AutoPreparedStatement.create(c, sql).executeUpdate();
			}
			if (!tableExists("StoredTransactions")) {
				String sql = "CREATE TABLE StoredTransactions("
						+ "transactionID INTEGER PRIMARY KEY AUTO_INCREMENT,"
						+ "accountName varchar(100),"
						+ "transactionAmount REAL,"
						+ "transactionComment varchar(500),"
						+ "username varchar(30)";
				if (foreign) {
					sql += ",FOREIGN KEY (username) REFERENCES Users(username)";
				}
				sql += ")";
				AutoPreparedStatement.create(c, sql).executeUpdate();
			}
			c.commit();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addAccount(String accountName, double accountBalance,
			boolean accountHidden) {
		try {
			AutoPreparedStatement ps = AutoPreparedStatement.create(c,
					"INSERT INTO Accounts VALUES (?,?,?,?)");
			ps.setString(accountName);
			ps.setDouble(accountBalance);
			int hidden = accountHidden ? 1 : 0;
			ps.setInt(hidden);
			ps.setString(username);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void editAccount(String oldAccountName, String accountName,
			double accountBalance, boolean accountHidden, Date until) {
		AutoPreparedStatement ps;
		try {
			c.setAutoCommit(false);
			ps = selectBetweenDates(
					"UPDATE Accounts SET accountName=?, accountBalance=? + COALESCE((SELECT SUM(transactionAmount) FROM",
					"WHERE accountName = ? AND username = ?",
					"),0) , accountHidden = ? WHERE accountName=? AND username = ?",
					until, null, false, showHidden, false, true);
			ps.setString(accountName);
			ps.setDouble(accountBalance);
			ps.setString(oldAccountName);
			ps.setString(username);
			int hidden = accountHidden ? 1 : 0;
			ps.setInt(hidden);
			ps.setString(oldAccountName);
			ps.setString(username);
			ps.executeUpdate();
			ps = AutoPreparedStatement
					.create(c,
							"UPDATE Transactions SET accountName=? WHERE accountName=? AND username = ?");
			ps.setString(accountName);
			ps.setString(oldAccountName);
			ps.setString(username);
			ps.executeUpdate();
			c.commit();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void removeAccount(String accountName) {
		try {
			c.setAutoCommit(false);
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"DELETE FROM Transactions WHERE accountName=? AND username = ?");
			ps.setString(accountName);
			ps.setString(username);
			ps.executeUpdate();
			ps = AutoPreparedStatement
					.create(c,
							"DELETE FROM Accounts WHERE accountName=? AND username = ?");
			ps.setString(accountName);
			ps.setString(username);
			ps.executeUpdate();
			c.commit();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void addTransaction(String accountName, double transactionAmount,
			Date transactionDate, String transactionComment) {
		try {
			c.setAutoCommit(false);
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"INSERT INTO Transactions(accountName,transactionAmount,transactionDate,transactionComment,username) VALUES (?,?,?,?,?)");
			ps.setString(accountName);
			ps.setDouble(transactionAmount);
			ps.setDate(transactionDate);
			ps.setString(transactionComment);
			ps.setString(username);
			ps.executeUpdate();
			ps = AutoPreparedStatement
					.create(c,
							"UPDATE Accounts SET accountBalance=accountBalance + ? WHERE accountName=? AND username = ?");
			ps.setDouble(transactionAmount);
			ps.setString(accountName);
			ps.setString(username);
			ps.executeUpdate();
			c.commit();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void editTransaction(long transactionID, String accountName,
			double transactionAmount, Date transactionDate,
			String transactionComment) {
		try {
			c.setAutoCommit(false);
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"UPDATE Accounts SET accountBalance=accountBalance - (SELECT transactionAmount FROM Transactions WHERE transactionID = ?) WHERE accountName=(SELECT accountName FROM Transactions WHERE transactionID = ?) AND username = ?");
			ps.setLong(transactionID);
			ps.setLong(transactionID);
			ps.setString(username);
			ps.executeUpdate();
			ps = AutoPreparedStatement
					.create(c,
							"UPDATE Transactions SET accountName = ?,transactionAmount = ?,transactionDate = ?,transactionComment = ? WHERE transactionID = ?");
			ps.setString(accountName);
			ps.setDouble(transactionAmount);
			ps.setDate(transactionDate);
			ps.setString(transactionComment);
			ps.setLong(transactionID);
			ps.executeUpdate();
			ps = AutoPreparedStatement
					.create(c,
							"UPDATE Accounts SET accountBalance=accountBalance + ? WHERE accountName=? AND username = ?");
			ps.setDouble(transactionAmount);
			ps.setString(accountName);
			ps.setString(username);
			ps.executeUpdate();
			c.commit();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void removeTransaction(long transactionID) {
		try {
			c.setAutoCommit(false);
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"UPDATE Accounts SET accountBalance=accountBalance - (SELECT transactionAmount FROM Transactions WHERE transactionID = ?) WHERE accountName=(SELECT accountName FROM Transactions WHERE transactionID = ?) AND username = ?");
			ps.setLong(transactionID);
			ps.setLong(transactionID);
			ps.setString(username);
			ps.executeUpdate();
			ps = AutoPreparedStatement.create(c,
					"DELETE FROM Transactions WHERE transactionID = ?");
			ps.setLong(transactionID);
			ps.executeUpdate();
			c.commit();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public List<Object[]> getAccounts(Date until) {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT accountName,accountBalance-COALESCE(future,0) as accountBalance,accountHidden FROM Accounts LEFT OUTER JOIN (SELECT SUM(transactionAmount) as future,accountName as accName FROM",
					"GROUP BY accName",
					") as FutureEvents ON accountName = accName WHERE accountHidden <= ? AND username = ? ORDER BY accountName ASC",
					until, null, false, showHidden, false, false);
			ps.setInt(showHidden);
			ps.setString(username);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] row = new Object[3];
				row[0] = results.getString("accountName");
				row[1] = results.getDouble("accountBalance");
				row[2] = results.getInt("accountHidden") == 1;
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<String> getAccountNames() {
		ArrayList<String> list = new ArrayList<String>();
		try {
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT accountName FROM Accounts WHERE accountHidden <= ? AND username = ? ORDER BY accountName ASC");
			ps.setInt(showHidden);
			ps.setString(username);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				list.add(results.getString("accountName"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	protected String helperClause() {
		return " AND username = ? ";
	}

	protected String helperValue() {
		return username;
	}

	public double getTotalAccountBalanceSum(Date until) {
		try {
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT SUM(accountBalance) - COALESCE((SELECT SUM(transactionAmount) FROM",
					"", "),0) as balanceSum FROM Accounts WHERE username = ?",
					until, null, false, 1, false, true);
			ps.setString(username);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				return result.getDouble("balanceSum");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0.;
	}

	public double getVisibleAccountBalanceSum(Date until) {
		try {
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT SUM(accountBalance) -  COALESCE((SELECT SUM(transactionAmount) FROM Accounts Natural JOIN (SELECT * FROM",
					"WHERE accountHidden = 0",
					") as VisibleTransactions),0) as balanceSum FROM Accounts WHERE accountHidden = 0 AND username = ?",
					until, null, false, 1, false, true);
			ps.setString(username);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				return result.getDouble("balanceSum");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0.;
	}

	public double getHiddenAccountBalanceSum(Date until) {
		try {
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT SUM(accountBalance) -  COALESCE((SELECT SUM(transactionAmount) FROM Accounts Natural JOIN (SELECT * FROM",
					"WHERE accountHidden = 1",
					") as VisibleTransactions),0) as balanceSum FROM Accounts WHERE accountHidden = 1 AND username = ?",
					until, null, false, 1, false, true);
			ps.setString(username);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				return result.getDouble("balanceSum");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0.;
	}

	public void exportDatabase(OutputStream out, String exportMessage) {
		PrintWriter pw = new PrintWriter(out);
		try {
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT TABLE_NAME as name FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME <> 'Users'");
			ps.setString(database.substring(database.lastIndexOf('/') + 1));
			ResultSet rs = ps.executeQuery();
			LinkedList<String> tableNames = new LinkedList<String>();
			long totalCount = 0;
			while (rs.next()) {
				tableNames.add(rs.getString("name"));
				ps = AutoPreparedStatement
						.create(c,
								"SELECT COUNT(*) as Count FROM "
										+ rs.getString("name"));
				ResultSet row = ps.executeQuery();
				while (row.next()) {
					totalCount += row.getLong("Count");
				}
			}
			pw.println(totalCount);
			ProgressMonitor pm = new ProgressMonitor(null, exportMessage, "",
					0, 100);
			final float percent = 100.0f / totalCount;
			float progress = 0;
			pm.setMillisToPopup(0);
			pm.setMillisToDecideToPopup(0);
			for (String table : tableNames) {
				pm.setNote(table);
				pw.println(table);
				ps = AutoPreparedStatement.create(c, "SELECT * FROM " + table);
				ResultSet row = ps.executeQuery();
				for (int i = 1; i <= row.getMetaData().getColumnCount(); i++) {
					if (row.getMetaData().getColumnLabel(i)
							.equals(("username"))) {
						continue;
					}
					pw.print(row.getMetaData().getColumnLabel(i));
					if (i < row.getMetaData().getColumnCount()) {
						pw.print(",");
					}
				}
				pw.println();
				while (row.next()) {
					for (int i = 1; i <= row.getMetaData().getColumnCount(); i++) {
						if (row.getMetaData().getColumnLabel(i)
								.equals(("username"))) {
							continue;
						}
						if (row.getMetaData().isAutoIncrement(i)) {
							pw.print("NULL");
						} else {
							pw.print(row.getString(i));
						}
						if (i < row.getMetaData().getColumnCount()) {
							pw.print(",");
						}
					}
					pw.println();
					progress += percent;
					pm.setProgress(Math.round(progress));
				}
				pw.println();
			}
			pw.flush();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void openDatabase(String database, String dbUsername,
			String dbPassword, String username) {
		this.database = database;
		this.username = username;
		try {
			close();
			c = DriverManager.getConnection("jdbc:mysql://" + database,
					dbUsername, dbPassword);
			initdb();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		setChanged();
		notifyObservers();
	}

	public long getNumberOfTransactions() {
		try {
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT COUNT(*) as number FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND username = ?");
			ps.setInt(showHidden);
			ps.setString(username);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				return result.getLong("number");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0L;
	}

	public long getNumberOfDeposits() {
		try {
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT COUNT(*) as number FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionAmount > 0 AND username = ?");
			ps.setInt(showHidden);
			ps.setString(username);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				return result.getLong("number");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0L;
	}

	public long getNumberOfWithdrawals() {
		try {
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT COUNT(*) as number FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionAmount < 0 AND username = ?");
			ps.setInt(showHidden);
			ps.setString(username);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				return result.getLong("number");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0L;
	}

	public Date getOldestTransactionDate() {
		return getTransactionDate("MIN");
	}

	public Date getNewestTransactionDate() {
		return getTransactionDate("MAX");
	}

	private Date getTransactionDate(String sql) {
		try {
			String query = "SELECT "
					+ sql
					+ "(transactionDate) as transactionDate FROM Accounts NATURAL JOIN Transactions WHERE accountHidden <= ? AND username = ?";
			AutoPreparedStatement ps = AutoPreparedStatement.create(c, query);
			ps.setInt(showHidden);
			ps.setString(username);
			ResultSet result = ps.executeQuery();
			Date date = null;
			while (result.next()) {
				String d = result.getString("transactionDate");
				if (d == null){
					return null;
				}
				date = dateFormat.parse(d);
			}
			return date;
		} catch (SQLException | ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<Object[]> getMultiTransactions(Date transactionDate,
			String transactionComment) {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT transactionID,accountName,transactionAmount FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionDate = ? AND transactionComment = ? AND username = ?");
			ps.setInt(showHidden);
			ps.setDate(transactionDate);
			ps.setString(transactionComment);
			ps.setString(username);

			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] row = new Object[3];
				row[0] = results.getLong("transactionID");
				row[1] = results.getString("accountName");
				row[2] = results.getDouble("transactionAmount");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public void close() throws SQLException {
		try {
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		c.close();
	}

	protected void deleteEntries(String table) {
		try {
			AutoPreparedStatement ps = AutoPreparedStatement.create(c,
					"DELETE FROM " + table + "WHERE username = ?");
			ps.setString(username);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void addStoredTransaction(String accountName,
			double transactionAmount, String transactionComment) {
		try {
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"INSERT INTO StoredTransactions(accountName,transactionAmount,transactionComment,username) VALUES (?,?,?,?)");
			ps.setString(accountName);
			ps.setDouble(transactionAmount);
			ps.setString(transactionComment);
			ps.setString(username);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public List<String> getStoredTransactionNames() {
		List<String> res = new ArrayList<String>();
		try {
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT transactionComment FROM StoredTransactions WHERE username = ? GROUP BY transactionComment");
			ps.setString(username);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				res.add(results.getString("transactionComment"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	public List<Object[]> getStoredTransaction(String transactionComment) {
		List<Object[]> res = new ArrayList<Object[]>();
		try {
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT transactionID,accountName,transactionAmount FROM StoredTransactions WHERE transactionComment=? AND username = ?");
			ps.setString(transactionComment);
			ps.setString(username);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] entry = new Object[3];
				entry[0] = results.getLong("transactionID");
				entry[1] = results.getString("accountName");
				entry[2] = results.getDouble("transactionAmount");
				res.add(entry);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	public List<Object[]> getStoredTransactions() {
		List<Object[]> res = new ArrayList<Object[]>();
		try {
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT transactionID,accountName,transactionAmount,transactionComment FROM StoredTransactions WHERE username = ?");
			ps.setString(username);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] entry = new Object[4];
				entry[0] = results.getLong("transactionID");
				entry[1] = results.getString("accountName");
				entry[2] = results.getDouble("transactionAmount");
				entry[3] = results.getString("transactionComment");
				res.add(entry);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	public double getAccountBalance(String accountName, Date until) {
		try {
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT accountBalance-COALESCE((SELECT SUM(transactionAmount) FROM",
					"WHERE accountName = ?",
					"),0) as accountBalance FROM Accounts WHERE accountName = ? AND username = ?",
					until, null, false, showHidden, false, true);
			ps.setString(accountName);
			ps.setString(accountName);
			ps.setString(username);
			ResultSet results = ps.executeQuery();
			if (results.next()) {
				return results.getDouble("accountBalance");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;

	}

	protected void importDatabaseHelper(String name, String sql) {
		try {
			AutoPreparedStatement ps = AutoPreparedStatement.create(c,
					"ALTER TABLE " + name
							+ " ALTER COLUMN username SET DEFAULT ?");
			ps.setString(username);
			ps.executeUpdate();
			AutoPreparedStatement.create(c, sql).executeUpdate();
			AutoPreparedStatement.create(
					c,
					"ALTER TABLE " + name
							+ " ALTER COLUMN username DROP DEFAULT")
					.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected String monthGrouper(String column) {
		return "MONTH(" + column + ")";
	}

	protected String yearGrouper(String column) {
		return "YEAR(" + column + ")";
	}
}
