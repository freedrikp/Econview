package se.freedrikp.econview.database;

import java.io.InputStream;
import java.io.OutputStream;
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
import java.util.Scanner;
import java.util.TreeMap;

import javax.swing.ProgressMonitor;

import se.freedrikp.econview.common.Common;

public abstract class SQLDatabase extends Database {
	protected Connection c;
	protected String database;
	protected int showHidden;

	public SQLDatabase(String database, String sqlClass, String connectionString) {
		this.database = database;
		showHidden = 0;
		try {
			Class.forName(sqlClass).newInstance();
			c = DriverManager.getConnection(connectionString);
			initdb();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public SQLDatabase(String database, String sqlClass,
			String connectionString, String user, String password) {
		this.database = database;
		showHidden = 0;
		try {
			Class.forName(sqlClass).newInstance();
			c = DriverManager.getConnection(connectionString, user, password);
			initdb();
		} catch (InstantiationException | IllegalAccessException
				| ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	protected abstract void initdb();

	public abstract void addAccount(String accountName, double accountBalance,
			boolean accountHidden);

	public abstract void editAccount(String oldAccountName, String accountName,
			double accountBalance, boolean accountHidden, Date until);

	public abstract void removeAccount(String accountName);

	public abstract void addTransaction(String accountName,
			double transactionAmount, Date transactionDate,
			String transactionComment);

	public abstract void editTransaction(long transactionID,
			String accountName, double transactionAmount, Date transactionDate,
			String transactionComment);

	public abstract void removeTransaction(long transactionID);

	public abstract List<Object[]> getAccounts(Date until);

	public List<Object[]> getTransactions(Date fromDate, Date toDate,
			Collection<String> accounts) {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		if (accounts.isEmpty()) {
			return list;
		}
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
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT transactionID,accountName,transactionAmount,transactionYear,transactionMonth,transactionDay,transactionComment FROM",
					"Where accountName IN " + selectedAccounts, "", fromDate,
					toDate, true, showHidden, true, true);
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

	public abstract List<String> getAccountNames();

	public List<Object[]> getMonthlyRevenues(Date until) {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT transactionYear,transactionMonth,SUM(transactionAmount) as revenue FROM Accounts NATURAL JOIN (SELECT * FROM ",
					"",
					") as FutureTransactions GROUP BY transactionYear,transactionMonth ORDER BY transactionYear DESC, transactionMonth DESC",
					null, until, false, showHidden, true, true);
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
					") as FutureTransactions GROUP BY transactionYear ORDER BY transactionYear DESC",
					null, until, false, showHidden, true, true);
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
					") as FutureTransactions GROUP BY accountName,transactionYear,transactionMonth ORDER BY transactionYear DESC, transactionMonth DESC, accountName ASC",
					null, until, false, showHidden, true, true);
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
					") as FutureTransactions GROUP BY accountName,transactionYear ORDER BY transactionYear DESC, accountName ASC",
					null, until, false, showHidden, true, true);
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
					null, until, false, showHidden, true, true);
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
					until, false, showHidden, true, true);
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

	public double getRevenue(Date from, Date to, Collection<String> accounts) {
		if (accounts.isEmpty()) {
			return 0.;
		}
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
			ps = selectBetweenDates(
					"Select SUM(transactionAmount) as revenue FROM",
					"WHERE accountName IN " + selectedAccounts, "", from, to,
					true, showHidden, true, true);
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
		Map<String, Map<Date, Double>> dataset = new TreeMap<String, Map<Date, Double>>();
		if (accounts.isEmpty()) {
			return dataset;
		}
		try {
			String helperClause = helperClause();
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT accountName,accountBalance-COALESCE(future,0) as accountBalance FROM Accounts LEFT OUTER JOIN (SELECT SUM(transactionAmount) as future,accountName as accName FROM",
					"GROUP BY accName",
					") as Future ON Accounts.accountName = Future.accName WHERE accountHidden <= ? "
							+ helperClause
							+ " AND accountName IN "
							+ selectedAccounts, to, null, true, showHidden,
					false, false);
			ps.setInt(showHidden);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
			ResultSet accs = ps.executeQuery();
			double totalStartBalance = 0;
			while (accs.next()) {
				double startBalance = accs.getDouble("accountBalance");
				totalStartBalance += startBalance;
				String accountName = accs.getString("accountName");
				buildDiagramDataset(from, to, dataset, startBalance,
						accountName, null, totalAccountName);
			}
			if (includeTotal) {
				buildDiagramDataset(from, to, dataset, totalStartBalance,
						totalAccountName, selectedAccounts, totalAccountName);
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
		datapoints.put(to, startBalance);
		AutoPreparedStatement ps;
		if (accountName.equals(totalAccountName)) {
			ps = selectBetweenDates(
					"SELECT sum(transactionAmount) as Amount FROM",
					"WHERE accountName IN " + consideredAccounts, "", from, to,
					false, showHidden, true, true);
		} else {
			ps = selectBetweenDates(
					"SELECT SUM(transactionAmount) as Amount FROM",
					"WHERE accountName = ?", "", from, to, false, showHidden,
					true, true);
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
					true, showHidden, true, true);
		} else {
			ps = selectBetweenDates(
					"Select transactionAmount,transactionYear,transactionMonth,transactionDay FROM",
					"WHERE accountName = ?", "", from, to, true, showHidden,
					true, true);
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

	public abstract double getTotalAccountBalanceSum(Date until);

	public abstract double getVisibleAccountBalanceSum(Date until);

	public abstract double getHiddenAccountBalanceSum(Date until);

	public abstract void exportDatabase(OutputStream out, String exportMessage);

	protected abstract void importDatabaseHelper(String table, String sql);

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
					importDatabaseHelper(name, sql);
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

	public abstract void openDatabase(String database, String dbUsername,
			String dbPassword, String username);

	public String getDatabase() {
		return database;
	}

	public void setShowHidden(boolean showHidden) {
		this.showHidden = showHidden ? 1 : 0;
		setChanged();
		notifyObservers();
	}

	public boolean getShowHidden() {
		return showHidden > 0 ? true : false;
	}

	public abstract long getNumberOfTransactions();

	public abstract long getNumberOfDeposits();

	public abstract long getNumberOfWithdrawals();

	public abstract Date getOldestTransactionDate();

	public abstract Date getNewestTransactionDate();

	public abstract List<Object[]> getMultiTransactions(Date transactionDate,
			String transactionComment);

	public abstract void close() throws SQLException;

	protected abstract void deleteEntries(String table);

	public void deleteAccounts() {
		deleteEntries("Accounts");
	}

	public void deleteTransactions() {
		deleteEntries("Transactions");
	}

	public void deleteStoredTransactions() {
		deleteEntries("StoredTransactions");
	}

	public abstract void addStoredTransaction(String accountName,
			double transactionAmount, String transactionComment);

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

	public abstract List<String> getStoredTransactionNames();

	public abstract List<Object[]> getStoredTransaction(
			String transactionComment);

	public abstract List<Object[]> getStoredTransactions();

	public abstract double getAccountBalance(String accountName, Date until);

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
					sqlWhere, "", fromDate, toDate, true, showHidden, true,
					true);
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

	protected abstract String helperClause();

	protected abstract String helperValue();

	protected AutoPreparedStatement selectBetweenDates(String sqlSelect,
			String sqlWhere, String sqlEnd, Date from, Date to,
			boolean ascending, int showHidden, boolean inclusive,
			boolean doOrder) throws SQLException {
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
			monthClause += "transactionYear = ?  AND transactionmonth < ?";
			dayClause += "transactionYear = ? AND transactionMonth = ? AND transactionDay "
					+ less + " ?";
		}
		if (to != null) {
			toy = year.format(to);
			tom = month.format(to);
			tod = day.format(to);
			yearClause += " AND transactionYear <= ?";
			monthClause += (monthClause.isEmpty() ? "" : "OR ")
					+ "transactionYear = ?  AND transactionmonth > ?";
			dayClause += (dayClause.isEmpty() ? "" : "OR ")
					+ "transactionYear = ? AND transactionMonth = ? AND transactionDay "
					+ great + " ?";
		}

		monthClause = monthClause.isEmpty() ? "" : (" WHERE NOT ("
				+ monthClause + ")");
		dayClause = dayClause.isEmpty() ? ""
				: (" WHERE NOT (" + dayClause + ")");

		String helperClause = helperClause();

		String sqlYears = "(SELECT * FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? "
				+ helperClause + yearClause + ")";
		String sqlMonths = "(SELECT * FROM " + sqlYears + " as Years"
				+ monthClause + ")";
		String sqlDays = "(SELECT * FROM " + sqlMonths + " as Months"
				+ dayClause + ")";

		String sql = sqlSelect
				+ " "
				+ sqlDays
				+ " as Days "
				+ sqlWhere
				+ (doOrder ? " ORDER BY transactionYear " + order
						+ ",transactionMonth " + order + ",transactionDay "
						+ order + ",transactionID " + order : "") + " "
				+ sqlEnd;

		AutoPreparedStatement ps = AutoPreparedStatement.create(c, sql);

		int index = 1;
		int temp = 0;
		while ((temp = 1 + sqlSelect.indexOf('?', temp)) > 0) {
			index++;
		}

		ps.setPlacedInt(index++, showHidden);
		if (helperClause.length() > 0) {
			ps.setPlacedString(index++, helperValue());
		}
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

}