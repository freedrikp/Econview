package se.freedrikp.econview.database;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.DriverManager;
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

public class SQLiteDatabase extends SQLDatabase {

	// private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
	// "yyyy-MM-dd");

	public SQLiteDatabase(String database) {
		super(database, "org.sqlite.JDBC", "jdbc:sqlite:" + database);
	}

	public void initdb() {
		try {
			File dbFile = new File(database);
			if (!dbFile.exists() || dbFile.length() == 0) {
				c.setAutoCommit(false);
				String sql = "CREATE TABLE Accounts("
						+ "accountName TEXT PRIMARY KEY,"
						+ "accountBalance REAL DEFAULT 0.0,"
						+ "accountHidden INTEGER DEFAULT '1'" + ")";
				AutoPreparedStatement.create(c, sql).executeUpdate();
				sql = "CREATE TABLE Transactions("
						+ "transactionID INTEGER PRIMARY KEY,"
						+ "accountName TEXT," + "transactionAmount REAL,"
						+ "transactionDate DATE," + "transactionComment TEXT"
						+ ")";
				AutoPreparedStatement.create(c, sql).executeUpdate();
				sql = "CREATE TABLE StoredTransactions("
						+ "transactionID INTEGER PRIMARY KEY,"
						+ "accountName TEXT," + "transactionAmount REAL,"
						+ "transactionComment TEXT" + ")";
				AutoPreparedStatement.create(c, sql).executeUpdate();
				c.commit();
				c.setAutoCommit(true);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addAccount(String accountName, double accountBalance,
			boolean accountHidden) {
		try {
			AutoPreparedStatement ps = AutoPreparedStatement.create(c,
					"INSERT INTO Accounts VALUES (?,?,?)");
			ps.setString(accountName);
			ps.setDouble(accountBalance);
			int hidden = accountHidden ? 1 : 0;
			ps.setInt(hidden);
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
			// ps =
			// AutoPreparedStatement.create(c,"UPDATE Accounts SET accountName=?, accountBalance=?, accountHidden = ? WHERE accountName=?");
			ps = selectBetweenDates(
					"UPDATE Accounts SET accountName=?, accountBalance=? + COALESCE((SELECT SUM(transactionAmount) FROM",
					"WHERE accountName = ?",
					"),0) , accountHidden = ? WHERE accountName=?", until,
					null, false, showHidden, false, true);
			ps.setString(accountName);
			ps.setDouble(accountBalance);
			ps.setString(accountName);
			int hidden = accountHidden ? 1 : 0;
			ps.setInt(hidden);
			ps.setString(oldAccountName);
			ps.executeUpdate();
			ps = AutoPreparedStatement
					.create(c,
							"UPDATE Transactions SET accountName=? WHERE accountName=?");
			ps.setString(accountName);
			ps.setString(oldAccountName);
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
			AutoPreparedStatement ps = AutoPreparedStatement.create(c,
					"DELETE FROM Transactions WHERE accountName=?");
			ps.setString(accountName);
			ps.executeUpdate();
			ps = AutoPreparedStatement.create(c,
					"DELETE FROM Accounts WHERE accountName=?");
			ps.setString(accountName);
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
							"INSERT INTO Transactions(accountName,transactionAmount,transactionDate,transactionComment) VALUES (?,?,?,?)");
			ps.setString(accountName);
			ps.setDouble(transactionAmount);
			ps.setDate(transactionDate);
			ps.setString(transactionComment);
			ps.executeUpdate();
			ps = AutoPreparedStatement
					.create(c,
							"UPDATE Accounts SET accountBalance=accountBalance + ? WHERE accountName=?");
			ps.setDouble(transactionAmount);
			ps.setString(accountName);
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
							"UPDATE Accounts SET accountBalance=accountBalance - (SELECT transactionAmount FROM Transactions WHERE transactionID = ?) WHERE accountName=(SELECT accountName FROM Transactions WHERE transactionID = ?)");
			ps.setLong(transactionID);
			ps.setLong(transactionID);
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
							"UPDATE Accounts SET accountBalance=accountBalance + ? WHERE accountName=?");
			ps.setDouble(transactionAmount);
			ps.setString(accountName);
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
							"UPDATE Accounts SET accountBalance=accountBalance - (SELECT transactionAmount FROM Transactions WHERE transactionID = ?) WHERE accountName=(SELECT accountName FROM Transactions WHERE transactionID = ?)");
			ps.setLong(transactionID);
			ps.setLong(transactionID);
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
					") ON accountName = accName WHERE accountHidden <= ? ORDER BY accountName ASC",
					until, null, false, showHidden, false, true);
			ps.setInt(showHidden);
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
							"SELECT accountName FROM Accounts WHERE accountHidden <= ? ORDER BY accountName ASC");
			ps.setInt(showHidden);
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
		return "";
	}

	protected String helperValue() {
		return "";
	}

	// private AutoPreparedStatement selectBetweenDates(String sqlSelect,
	// String sqlWhere,String sqlEnd, Date from, Date to,boolean ascending,int
	// showHidden,boolean inclusive) throws SQLException {
	// SimpleDateFormat year = new SimpleDateFormat("yyyy");
	// SimpleDateFormat month = new SimpleDateFormat("MM");
	// SimpleDateFormat day = new SimpleDateFormat("dd");
	// String toy = year.format(to);
	// String tom = month.format(to);
	// String tod = day.format(to);
	// String foy = year.format(from);
	// String fom = month.format(from);
	// String fod = day.format(from);
	// String order = ascending ? "ASC" : "DESC";
	// String less = inclusive ? "<" :"<=";
	// String great = inclusive ? ">" : ">=";
	// String sqlYears,sqlMonths,sqlDays;
	// if ( from == null){
	// sqlYears =
	// "(SELECT * FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionYear <= ?)";
	// sqlMonths = "(SELECT * FROM "
	// + sqlYears
	// + " WHERE NOT (transactionYear == ? AND transactionMonth > ?))";
	// sqlDays = "(SELECT * FROM "
	// + sqlMonths
	// +
	// " WHERE NOT (transactionYear == ? AND transactionMonth == ? AND transactionDay "
	// + great + " ?))";
	// }else if (to == null){
	// sqlYears =
	// "(SELECT * FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionYear >= ?)";
	// sqlMonths = "(SELECT * FROM "
	// + sqlYears
	// + " WHERE NOT (transactionYear == ?  AND transactionmonth < ?))";
	// sqlDays = "(SELECT * FROM "
	// + sqlMonths
	// +
	// " WHERE NOT (transactionYear == ? AND transactionMonth == ? AND transactionDay "
	// + less + " ?))";
	// }else{
	// sqlYears =
	// "(SELECT * FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionYear <= ? AND transactionYear >= ?)";
	// sqlMonths = "(SELECT * FROM "
	// + sqlYears
	// +
	// " WHERE NOT (transactionYear == ? AND transactionMonth > ? OR transactionYear == ?  AND transactionmonth < ?))";
	// sqlDays = "(SELECT * FROM "
	// + sqlMonths
	// +
	// " WHERE NOT (transactionYear == ? AND transactionMonth == ? AND transactionDay "
	// + great +
	// " ? OR transactionYear == ? AND transactionMonth == ? AND transactionDay "
	// + less + " ?))";
	// }
	//
	// AutoPreparedStatement ps = AutoPreparedStatement.create(c,sqlSelect
	// + " "
	// + sqlDays
	// + " "
	// + sqlWhere
	// + " ORDER BY transactionYear " + order + ",transactionMonth " + order +
	// ",transactionDay " + order + ",transactionID " + order + " " + sqlEnd);
	// int index = 1;
	// int temp = 0;
	// while((temp = 1 + sqlSelect.indexOf('?',temp)) > 0){
	// index++;
	// }
	//
	// if ( from == null){
	// ps.setPlacedInt(index++, showHidden);
	// ps.setPlacedString(index++, toy);
	//
	// ps.setPlacedString(index++, toy);
	// ps.setPlacedString(index++, tom);
	//
	// ps.setPlacedString(index++, toy);
	// ps.setPlacedString(index++, tom);
	// ps.setPlacedString(index++, tod);
	// }else if ( to == null){
	// ps.setPlacedInt(index++, showHidden);
	// ps.setPlacedString(index++, foy);
	//
	// ps.setPlacedString(index++, foy);
	// ps.setPlacedString(index++, fom);
	//
	// ps.setPlacedString(index++, foy);
	// ps.setPlacedString(index++, fom);
	// ps.setPlacedString(index++, fod);
	// }else{
	// ps.setPlacedInt(index++, showHidden);
	// ps.setPlacedString(index++, toy);
	// ps.setPlacedString(index++, foy);
	//
	// ps.setPlacedString(index++, toy);
	// ps.setPlacedString(index++, tom);
	// ps.setPlacedString(index++, foy);
	// ps.setPlacedString(index++, fom);
	//
	// ps.setPlacedString(index++, toy);
	// ps.setPlacedString(index++, tom);
	// ps.setPlacedString(index++, tod);
	// ps.setPlacedString(index++, foy);
	// ps.setPlacedString(index++, fom);
	// ps.setPlacedString(index++, fod);
	// }
	// return ps;
	// }

	public double getTotalAccountBalanceSum(Date until) {
		try {
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT SUM(accountBalance) - COALESCE((SELECT SUM(transactionAmount) FROM",
					"", "),0) as balanceSum FROM Accounts", until, null, false,
					1, false, true);
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
					"SELECT SUM(accountBalance) -  COALESCE((SELECT SUM(transactionAmount) FROM ",
					"AND accountHidden = 0",
					"),0) as balanceSum FROM Accounts WHERE accountHidden = 0",
					until, null, false, 1, false, true);
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
					"SELECT SUM(accountBalance) -  COALESCE((SELECT SUM(transactionAmount) FROM ",
					"AND accountHidden = 1",
					"),0) as balanceSum FROM Accounts WHERE accountHidden = 1",
					until, null, false, 1, false, true);
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
							"SELECT name,type FROM sqlite_master WHERE type IN ('table','view') AND name NOT LIKE 'sqlite_%'");
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
					pw.print(row.getMetaData().getColumnLabel(i));
					if (i < row.getMetaData().getColumnCount()) {
						pw.print(",");
					}
				}
				pw.println();
				while (row.next()) {
					for (int i = 1; i <= row.getMetaData().getColumnCount(); i++) {
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
		try {
			close();
			c = DriverManager.getConnection("jdbc:sqlite:" + database);
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
							"SELECT COUNT(*) as number FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ?");
			ps.setInt(showHidden);
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
							"SELECT COUNT(*) as number FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionAmount > 0");
			ps.setInt(showHidden);
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
							"SELECT COUNT(*) as number FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionAmount < 0");
			ps.setInt(showHidden);
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
		// return
		// getTransactionDate("MIN(transactionYear) as year,MIN(transactionMonth) as month,MIN(transactionDay) as day");
		return getTransactionDate("MIN");
	}

	public Date getNewestTransactionDate() {
		// return
		// getTransactionDate("MAX(transactionYear) as year,MAX(transactionMonth) as month,MAX(transactionDay) as day");
		return getTransactionDate("MAX");
	}

	private Date getTransactionDate(String sql) {
		try {
			String query = "SELECT "
					+ sql
					+ "(transactionDate) as transactionDate FROM Accounts NATURAL JOIN Transactions WHERE accountHidden <= ?";
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,query);
			ps.setInt(showHidden);
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
							"SELECT transactionID,accountName,transactionAmount FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionDate = ? AND transactionComment = ?");
			ps.setInt(showHidden);
			ps.setDate(transactionDate);
			ps.setString(transactionComment);

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
			c = DriverManager.getConnection("jdbc:sqlite:" + database);
			AutoPreparedStatement.create(c, "VACUUM").executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		c.close();
	}

	protected void deleteEntries(String table) {
		try {
			AutoPreparedStatement.create(c, "DELETE FROM " + table)
					.executeUpdate();
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
							"INSERT INTO StoredTransactions(accountName,transactionAmount,transactionComment) VALUES (?,?,?)");
			ps.setString(accountName);
			ps.setDouble(transactionAmount);
			ps.setString(transactionComment);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	protected void importDatabaseHelper(String name, String sql) {
		try {
			AutoPreparedStatement.create(c, sql).executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	protected String monthGrouper(String column) {
		return "strftime('%m'," + column + ")";
	}

	protected String yearGrouper(String column) {
		return "strftime('%Y'," + column + ")";
	}

}
