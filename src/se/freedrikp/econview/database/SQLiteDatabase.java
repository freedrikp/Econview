package se.freedrikp.econview.database;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Scanner;
import java.util.TreeMap;

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
						+ "transactionYear TEXT," + "transactionMonth TEXT,"
						+ "transactionDay TEXT," + "transactionComment TEXT"
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
					null, false, showHidden, false);
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
							"INSERT INTO Transactions(accountName,transactionAmount,transactionYear,transactionMonth,transactionDay,transactionComment) VALUES (?,?,?,?,?,?)");
			ps.setString(accountName);
			ps.setDouble(transactionAmount);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
			ps.setString(sdf.format(transactionDate));
			sdf = new SimpleDateFormat("MM");
			ps.setString(sdf.format(transactionDate));
			sdf = new SimpleDateFormat("dd");
			ps.setString(sdf.format(transactionDate));
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
							"UPDATE Transactions SET accountName = ?,transactionAmount = ?,transactionYear = ?,transactionMonth = ?,transactionDay = ?,transactionComment = ? WHERE transactionID = ?");
			ps.setString(accountName);
			ps.setDouble(transactionAmount);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
			ps.setString(sdf.format(transactionDate));
			sdf = new SimpleDateFormat("MM");
			ps.setString(sdf.format(transactionDate));
			sdf = new SimpleDateFormat("dd");
			ps.setString(sdf.format(transactionDate));
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
					until, null, false, showHidden, false);
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

	public List<Object[]> getTransactions(Date fromDate, Date toDate,
			Collection<String> accounts) {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			String selectedAccounts = "(";
			int i = 1;
			for (String a : accounts) {
				selectedAccounts += "'" + a + "'";
				if (i < accounts.size()) {
					selectedAccounts += ",";
				}
				i++;
			}
			selectedAccounts += ")";
			// PreparedStatement ps = c
			// .prepareStatement("SELECT transactionID,accountName,transactionAmount,transactionYear,transactionMonth,transactionDay,transactionComment FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ?");
			// ps.setInt(1, showHidden);
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT transactionID,accountName,transactionAmount,transactionYear,transactionMonth,transactionDay,transactionComment FROM",
					"Where accountName IN " + selectedAccounts, "", fromDate,
					toDate, true, showHidden, true);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] row = new Object[5];
				row[0] = results.getLong("transactionID");
				row[1] = results.getString("accountName");
				row[2] = results.getDouble("transactionAmount");
				Calendar cal = Common.getFlattenCalendar(null);
				cal.set(results.getInt("transactionYear"),
						results.getInt("transactionMonth") - 1,
						results.getInt("transactionDay"));
				row[3] = cal.getTime();
				row[4] = results.getString("transactionComment");
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

	public List<Object[]> getMonthlyRevenues(Date until) {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT transactionYear,transactionMonth,SUM(transactionAmount) as revenue FROM Accounts NATURAL JOIN (SELECT * FROM ",
					"",
					") GROUP BY transactionYear,transactionMonth ORDER BY transactionYear DESC, transactionMonth DESC",
					null, until, false, showHidden, true);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] row = new Object[2];
				Calendar cal = Common.getFlattenCalendar(null);
				cal.set(results.getInt("transactionYear"),
						results.getInt("transactionMonth") - 1, 1);
				row[0] = cal.getTime();
				row[1] = results.getDouble("revenue");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<Object[]> getYearlyRevenues(Date until) {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT transactionYear,SUM(transactionAmount) as revenue FROM Accounts NATURAL JOIN (SELECT * FROM ",
					"",
					") GROUP BY transactionYear ORDER BY transactionYear DESC",
					null, until, false, showHidden, true);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] row = new Object[2];
				Calendar cal = Common.getFlattenCalendar(null);
				cal.set(results.getInt("transactionYear"), 0, 1);
				row[0] = cal.getTime();
				row[1] = results.getDouble("revenue");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<Object[]> getMonthlyAccountRevenues(Date until) {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT accountName,transactionYear,transactionMonth,SUM(transactionAmount) as revenue FROM Accounts NATURAL JOIN (SELECT * FROM ",
					"",
					") GROUP BY accountName,transactionYear,transactionMonth ORDER BY transactionYear DESC, transactionMonth DESC, accountName ASC",
					null, until, false, showHidden, true);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] row = new Object[3];
				Calendar cal = Common.getFlattenCalendar(null);
				cal.set(results.getInt("transactionYear"),
						results.getInt("transactionMonth") - 1, 1);
				row[0] = cal.getTime();
				row[1] = results.getString("accountName");
				row[2] = results.getDouble("revenue");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<Object[]> getYearlyAccountRevenues(Date until) {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT accountName,transactionYear,SUM(transactionAmount) as revenue FROM Accounts NATURAL JOIN (SELECT * FROM ",
					"",
					") GROUP BY accountName,transactionYear ORDER BY transactionYear DESC, accountName ASC",
					null, until, false, showHidden, true);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] row = new Object[3];
				Calendar cal = Common.getFlattenCalendar(null);
				cal.set(results.getInt("transactionYear"), 0, 1);
				row[0] = cal.getTime();
				row[1] = results.getString("accountName");
				row[2] = results.getDouble("revenue");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public double getTotalRevenue(Date until) {
		try {
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT SUM(transactionAmount) as revenue FROM ", "", "",
					null, until, false, showHidden, true);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				return results.getDouble("revenue");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0.;
	}

	public List<Object[]> getTotalAccountRevenues(Date until) {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT accountName,SUM(transactionAmount) as revenue FROM Accounts NATURAL JOIN (SELECT * FROM ",
					"", ") GROUP BY accountName ORDER accountName ASC", null,
					until, false, showHidden, true);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] row = new Object[2];
				row[0] = results.getString("accountName");
				row[1] = results.getDouble("revenue");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public Double getRevenue(Date from, Date to, Collection<String> accounts) {
		try {
			String selectedAccounts = "(";
			int i = 1;
			for (String a : accounts) {
				selectedAccounts += "'" + a + "'";
				if (i < accounts.size()) {
					selectedAccounts += ",";
				}
				i++;
			}
			selectedAccounts += ")";
			AutoPreparedStatement ps;
			// if (account.isEmpty()) {
			// ps = selectBetweenDates(
			// "Select SUM(transactionAmount) as revenue FROM", "",
			// from, to);
			// } else {
			ps = selectBetweenDates(
					"Select SUM(transactionAmount) as revenue FROM",
					"WHERE accountName IN " + selectedAccounts, "", from, to,
					true, showHidden, true);
			// ps.setString(13, account);
			// }
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				return results.getDouble("revenue");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0.;
	}

	public Map<String, Map<Date, Double>> getCustomDiagramData(Date from,
			Date to, Collection<String> accounts, boolean includeTotal,
			String totalAccountName) {
		String selectedAccounts = "(";
		int i = 1;
		for (String a : accounts) {
			selectedAccounts += "'" + a + "'";
			if (i < accounts.size()) {
				selectedAccounts += ",";
			}
			i++;
		}
		selectedAccounts += ")";
		SimpleDateFormat year = new SimpleDateFormat("yyyy");
		SimpleDateFormat month = new SimpleDateFormat("MM");
		SimpleDateFormat day = new SimpleDateFormat("dd");
		String foy = year.format(from);
		String fom = month.format(from);
		String fod = day.format(from);
		// System.out.println(foy + "-" + fom + "-" + fod + "<->" + toy + "-" +
		// tom + "-" + tod);
		Map<String, Map<Date, Double>> dataset = new TreeMap<String, Map<Date, Double>>();
		try {
			// PreparedStatement ps = c
			// .prepareStatement("Select accountName,accountBalance FROM Accounts WHERE accountHidden <= ? AND accountName IN "
			// + selectedAccounts);
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT accountName,accountBalance-COALESCE(future,0) as accountBalance FROM Accounts LEFT OUTER JOIN (SELECT SUM(transactionAmount) as future,accountName as accName FROM",
					"GROUP BY accName",
					") as Future ON Accounts.accountName = Future.accName WHERE accountHidden <= ? AND accountName IN "
							+ selectedAccounts, to, null, true, showHidden,
					false);
			ps.setInt(showHidden);
			ResultSet accs = ps.executeQuery();
			double totalStartBalance = 0;
			while (accs.next()) {
				double startBalance = accs.getDouble("accountBalance");
				totalStartBalance += startBalance;
				String accountName = accs.getString("accountName");
				buildDiagramDataset(from, to, dataset, startBalance,
						accountName, null, totalAccountName);
				// System.out.println("AccountName: " + accountName +
				// " AccountBalance: " + startBalance);
			}
			if (includeTotal) {
				buildDiagramDataset(from, to, dataset, totalStartBalance,
						totalAccountName, selectedAccounts, totalAccountName);
				// HashMap<String,Double> total = new HashMap<String,Double>();
				// for (Map<String,Double> map : dataset.values()){
				// for (Map.Entry<String, Double> entry : map.entrySet()){
				// if (total.containsKey(entry.getKey())){
				// double newValue = entry.getValue() +
				// total.get(entry.getKey());
				// total.put(entry.getKey(), newValue);
				// }else{
				// total.put(entry.getKey(), entry.getValue());
				// }
				// }
				// }
				// System.out.println(total);
				// dataset.put(Utilities.getString("TOTAL_ACCOUNT_NAME"),
				// total);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dataset;
	}

	private void buildDiagramDataset(Date from, Date to,
			Map<String, Map<Date, Double>> dataset, double startBalance,
			String accountName, String consideredAccounts,
			String totalAccountName) throws SQLException {
		Map<Date, Double> datapoints = new TreeMap<Date, Double>();
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		datapoints.put(to, startBalance);
		AutoPreparedStatement ps;
		// Date latest = getNewestTransactionDate();
		// latest = latest != null ? latest : new Date();
		if (accountName.equals(totalAccountName)) {
			ps = selectBetweenDates(
					"SELECT sum(transactionAmount) as Amount FROM",
					"WHERE accountName IN " + consideredAccounts, "", from, to,
					false, showHidden, true);
		} else {
			ps = selectBetweenDates(
					"SELECT SUM(transactionAmount) as Amount FROM",
					"WHERE accountName = ?", "", from, to, false, showHidden,
					true);
			ps.setString(accountName);
		}
		ResultSet transactions = ps.executeQuery();
		while (transactions.next()) {
			startBalance -= transactions.getDouble("Amount");
		}
		datapoints.put(from, startBalance);
		if (accountName.equals(totalAccountName)) {
			ps = selectBetweenDates(
					"SELECT transactionAmount,transactionYear,transactionMonth,transactionDay FROM",
					"WHERE accountName IN " + consideredAccounts, "", from, to,
					true, showHidden, true);
		} else {
			ps = selectBetweenDates(
					"Select transactionAmount,transactionYear,transactionMonth,transactionDay FROM",
					"WHERE accountName = ?", "", from, to, true, showHidden,
					true);
			ps.setString(accountName);
		}
		transactions = ps.executeQuery();
		while (transactions.next()) {
			// Fix this GUI dependency
			Calendar cal = Common.getFlattenCalendar(null);
			cal.set(transactions.getInt("transactionYear"),
					transactions.getInt("transactionMonth") - 1,
					transactions.getInt("transactionDay"));
			startBalance += transactions.getDouble("transactionAmount");
			datapoints.put(cal.getTime(), startBalance);
		}
		dataset.put(accountName, datapoints);
	}

	private AutoPreparedStatement selectBetweenDates(String sqlSelect,
			String sqlWhere, String sqlEnd, Date from, Date to,
			boolean ascending, int showHidden, boolean inclusive)
			throws SQLException {
		SimpleDateFormat year = new SimpleDateFormat("yyyy");
		SimpleDateFormat month = new SimpleDateFormat("MM");
		SimpleDateFormat day = new SimpleDateFormat("dd");
		String toy = "";
		String tom = "";
		String tod = "";
		String foy = "";
		String fom = "";
		String fod = "";

		String order = ascending ? "ASC" : "DESC";
		String less = inclusive ? "<" : "<=";
		String great = inclusive ? ">" : ">=";

		String yearClause = "";
		String monthClause = "";
		String dayClause = "";
		if (from != null) {
			foy = year.format(from);
			fom = month.format(from);
			fod = day.format(from);
			yearClause += " AND transactionYear >= ?";
			monthClause += "transactionYear == ?  AND transactionmonth < ?";
			dayClause += "transactionYear == ? AND transactionMonth == ? AND transactionDay "
					+ less + " ?";
		}
		if (to != null) {
			toy = year.format(to);
			tom = month.format(to);
			tod = day.format(to);
			yearClause += " AND transactionYear <= ?";
			monthClause += (monthClause.isEmpty() ? "" : "OR ")
					+ "transactionYear == ?  AND transactionmonth > ?";
			dayClause += (dayClause.isEmpty() ? "" : "OR ")
					+ "transactionYear == ? AND transactionMonth == ? AND transactionDay "
					+ great + " ?";
		}

		monthClause = monthClause.isEmpty() ? "" : (" WHERE NOT ("
				+ monthClause + ")");
		dayClause = dayClause.isEmpty() ? ""
				: (" WHERE NOT (" + dayClause + ")");

		String sqlYears = "(SELECT * FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? "
				+ yearClause + ")";
		String sqlMonths = "(SELECT * FROM " + sqlYears + monthClause + ")";
		String sqlDays = "(SELECT * FROM " + sqlMonths + dayClause + ")";

		String sql = sqlSelect + " " + sqlDays + " " + sqlWhere
				+ " ORDER BY transactionYear " + order + ",transactionMonth "
				+ order + ",transactionDay " + order + ",transactionID "
				+ order + " " + sqlEnd;

		AutoPreparedStatement ps = AutoPreparedStatement.create(c, sql);

		int index = 1;
		int temp = 0;
		while ((temp = 1 + sqlSelect.indexOf('?', temp)) > 0) {
			index++;
		}

		ps.setPlacedInt(index++, showHidden);
		if (from != null) {
			ps.setPlacedString(index++, foy);
		}
		if (to != null) {
			ps.setPlacedString(index++, toy);
		}
		if (from != null) {
			ps.setPlacedString(index++, foy);
			ps.setPlacedString(index++, fom);
		}
		if (to != null) {
			ps.setPlacedString(index++, toy);
			ps.setPlacedString(index++, tom);
		}
		if (from != null) {
			ps.setPlacedString(index++, foy);
			ps.setPlacedString(index++, fom);
			ps.setPlacedString(index++, fod);
		}
		if (to != null) {
			ps.setPlacedString(index++, toy);
			ps.setPlacedString(index++, tom);
			ps.setPlacedString(index++, tod);
		}
		return ps;
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
					1, false);
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
					")),0) as balanceSum FROM Accounts WHERE accountHidden = 0",
					until, null, false, 1, false);
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
					")),0) as balanceSum FROM Accounts WHERE accountHidden = 1",
					until, null, false, 1, false);
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

	public void importDatabase(InputStream in, String importMessage) {
		try {
			c.setAutoCommit(false);
			Scanner scan = new Scanner(in);
			long totalCount = 0;
			if (scan.hasNextLine()) {
				totalCount = Long.parseLong(scan.nextLine());
			}
			ProgressMonitor pm = new ProgressMonitor(null, importMessage, "",
					0, 100);
			pm.setMillisToPopup(0);
			pm.setMillisToDecideToPopup(0);
			final float percent = 100.0f / totalCount;
			float progress = 0;
			while (scan.hasNextLine()) {
				String name = scan.nextLine();
				pm.setNote(name);
				String header = scan.nextLine();
				String line;
				while (scan.hasNextLine()
						&& !(line = scan.nextLine()).isEmpty()) {
					String sql = "INSERT INTO " + name + " (" + header + ")"
							+ " VALUES (";
					String[] row = line.split(",");
					for (int i = 0; i < row.length; i++) {
						if (row[i].equals("NULL")) {
							sql += row[i];
						} else {
							sql += "'" + row[i] + "'";
						}
						if (i < row.length - 1) {
							sql += ",";
						}
					}
					sql += ")";
					AutoPreparedStatement ps = AutoPreparedStatement.create(c,
							sql);
					ps.executeUpdate();
					progress += percent;
					pm.setProgress(Math.round(progress));
				}
			}
			c.commit();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void openDatabase(String database) {
		this.database = database;
		try {
			close();
			c = DriverManager.getConnection("jdbc:sqlite:" + database);
			initdb();
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		setChanged();
		notifyObservers();
	}

	public void setShowHidden(boolean showHidden) {
		this.showHidden = showHidden ? 1 : 0;
		setChanged();
		notifyObservers();
	}

	public boolean getShowHidden() {
		return showHidden > 0 ? true : false;
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
			String year = "SELECT "
					+ sql
					+ "(transactionYear) FROM Accounts NATURAL JOIN Transactions WHERE accountHidden <= ?";
			String month = "SELECT "
					+ sql
					+ "(transactionMonth) FROM Accounts NATURAL JOIN Transactions WHERE transactionYear IN ("
					+ year + ") AND accountHidden <= ?";
			String day = "SELECT "
					+ sql
					+ "(transactionDay) FROM Accounts NATURAL JOIN Transactions WHERE transactionMonth IN ("
					+ month + ") AND transactionYear IN (" + year
					+ ") AND accountHidden <= ?";
			// PreparedStatement ps = c
			// .prepareStatement("SELECT "
			// + sql
			// +
			// " FROM Transactions NATURAL JOIN Accounts WHERE accountIncluded >= ?");
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT transactionYear as year,transactionMonth as month,transactionDay as day FROM Transactions NATURAL JOIN Accounts WHERE transactionYear IN ("
									+ year
									+ ") AND transactionMonth IN ("
									+ month
									+ ") AND transactionDay IN ("
									+ day
									+ ")");
			ps.setInt(showHidden);
			ps.setInt(showHidden);
			ps.setInt(showHidden);
			ps.setInt(showHidden);
			ps.setInt(showHidden);
			ps.setInt(showHidden);
			ps.setInt(showHidden);
			ResultSet result = ps.executeQuery();
			Date date = null;
			while (result.next()) {
				Calendar cal = Common.getFlattenCalendar(null);
				cal.set(result.getInt("year"), result.getInt("month") - 1,
						result.getInt("day"));
				date = cal.getTime();
			}
			return date;
		} catch (SQLException e) {
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
							"SELECT transactionID,accountName,transactionAmount FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionYear = ? AND transactionMonth = ? AND transactionDay = ? AND transactionComment = ?");
			ps.setInt(showHidden);
			ps.setString(new SimpleDateFormat("yyyy").format(transactionDate));
			ps.setString(new SimpleDateFormat("MM").format(transactionDate));
			ps.setString(new SimpleDateFormat("dd").format(transactionDate));
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

	private void deleteEntries(String table) {
		try {
			AutoPreparedStatement.create(c, "DELETE FROM " + table)
					.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void deleteAccounts() {
		deleteEntries("Accounts");
	}

	public void deleteTransactions() {
		deleteEntries("Transactions");
	}

	public void deleteStoredTransactions() {
		deleteEntries("StoredTransactions");
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

	public void editStoredTransaction(long transactionID, String accountName,
			double transactionAmount, String transactionComment) {
		try {
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"UPDATE StoredTransactions SET accountName = ?,transactionAmount = ?,transactionComment = ? WHERE transactionID = ?");
			ps.setString(accountName);
			ps.setDouble(transactionAmount);
			ps.setString(transactionComment);
			ps.setLong(transactionID);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void removeStoredTransaction(long transactionID) {
		try {
			AutoPreparedStatement ps = AutoPreparedStatement.create(c,
					"DELETE FROM StoredTransactions WHERE transactionID = ?");
			ps.setLong(transactionID);
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
							"SELECT transactionComment FROM StoredTransactions GROUP BY transactionComment");
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
							"SELECT transactionID,accountName,transactionAmount FROM StoredTransactions WHERE transactionComment=?");
			ps.setString(transactionComment);
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
							"SELECT transactionID,accountName,transactionAmount,transactionComment FROM StoredTransactions");
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
					"),0) as accountBalance FROM Accounts WHERE accountName = ?",
					until, null, false, showHidden, false);
			ps.setString(accountName);
			ps.setString(accountName);
			ResultSet results = ps.executeQuery();
			if (results.next()) {
				return results.getDouble("accountBalance");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public List<Object[]> searchTransactions(long transactionId, boolean doID,
			String accountName, double transactionAmount, boolean doAmount,
			String transactionComment, Date fromDate, Date toDate) {
		List<Object[]> list = new LinkedList<Object[]>();
		String sqlWhere = "WHERE ";
		if (doID) {
			sqlWhere += "transactionID = ? AND ";
		}
		if (accountName != null) {
			sqlWhere += "LOWER(accountName) LIKE ? AND ";
			accountName = "%" + accountName + "%";
		}
		if (doAmount) {
			sqlWhere += "transactionAmount = ? AND ";
		}
		if (transactionComment != null) {
			sqlWhere += "LOWER(transactionComment) LIKE ?";
			transactionComment = "%" + transactionComment + "%";
		}
		if (sqlWhere.endsWith("AND ")) {
			sqlWhere = sqlWhere.substring(0, sqlWhere.length() - 4);
		}
		if (sqlWhere.equals("WHERE ")) {
			sqlWhere = "";
		}
		AutoPreparedStatement ps;
		try {
			ps = selectBetweenDates(
					"SELECT transactionID,accountName,transactionAmount,transactionYear,transactionMonth,transactionDay,transactionComment FROM",
					sqlWhere, "", fromDate, toDate, true, showHidden, true);
			if (doID) {
				ps.setLong(transactionId);
			}
			if (accountName != null) {
				ps.setString(accountName.toLowerCase());
			}
			if (doAmount) {
				ps.setDouble(transactionAmount);
			}
			if (transactionComment != null) {
				ps.setString(transactionComment.toLowerCase());
			}
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] row = new Object[5];
				row[0] = results.getLong("transactionID");
				row[1] = results.getString("accountName");
				row[2] = results.getDouble("transactionAmount");
				Calendar cal = Common.getFlattenCalendar(null);
				cal.set(results.getInt("transactionYear"),
						results.getInt("transactionMonth") - 1,
						results.getInt("transactionDay"));
				row[3] = cal.getTime();
				row[4] = results.getString("transactionComment");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}
}
