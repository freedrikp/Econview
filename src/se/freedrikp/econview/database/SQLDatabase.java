package se.freedrikp.econview.database;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
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
	protected static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd");

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

	protected abstract String monthGrouper(String column);

	protected abstract String yearGrouper(String column);

	protected abstract void initdb();

	public void addAccount(String accountName, double accountBalance,
			boolean accountHidden) {
		try {
			String helperAddValue = helperAddValue();
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c, "INSERT INTO Accounts VALUES (?,?,?"
							+ helperAddValue + ")");
			ps.setString(accountName);
			ps.setDouble(accountBalance);
			int hidden = accountHidden ? 1 : 0;
			ps.setInt(hidden);
			if (helperAddValue.length() > 0) {
				ps.setString(helperValue());
			}
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
			String helperClause = helperClause();
			ps = selectBetweenDates(
					"UPDATE Accounts SET accountName=?, accountBalance=? + COALESCE((SELECT * FROM (SELECT SUM(transactionAmount) FROM",
					"AND accountName = ?" + helperClause,
					") as Future),0) , accountHidden = ? WHERE accountName=?"
							+ helperClause, until, null, false, showHidden,
					false, true);
			ps.setString(accountName);
			ps.setDouble(accountBalance);
			ps.setString(oldAccountName);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
			int hidden = accountHidden ? 1 : 0;
			ps.setInt(hidden);
			ps.setString(oldAccountName);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void removeAccount(String accountName) {
		try {
			String helperClause = helperClause();
			AutoPreparedStatement ps = AutoPreparedStatement.create(c,
					"DELETE FROM Accounts WHERE accountName=?" + helperClause);
			ps.setString(accountName);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void addTransaction(String accountName, double transactionAmount,
			Date transactionDate, String transactionComment) {
		try {
			String helperAdd = helperAdd();
			c.setAutoCommit(false);
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"INSERT INTO Transactions(accountName,transactionAmount,transactionDate,transactionComment"
									+ helperAdd
									+ ") VALUES (?,?,?,?"
									+ helperAddValue() + ")");
			ps.setString(accountName);
			ps.setDouble(transactionAmount);
			ps.setDate(transactionDate);
			ps.setString(transactionComment);
			if (helperAdd.length() > 0) {
				ps.setString(helperValue());
			}
			ps.executeUpdate();
			String helperClause = helperClause();
			ps = AutoPreparedStatement.create(c,
					"UPDATE Accounts SET accountBalance=accountBalance + ? WHERE accountName=?"
							+ helperClause);
			ps.setDouble(transactionAmount);
			ps.setString(accountName);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
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
			String helperClause = helperClause();
			c.setAutoCommit(false);
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"UPDATE Accounts SET accountBalance=accountBalance - (SELECT transactionAmount FROM Transactions WHERE transactionID = ?) WHERE accountName=(SELECT accountName FROM Transactions WHERE transactionID = ?)"
									+ helperClause);
			ps.setLong(transactionID);
			ps.setLong(transactionID);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
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
			ps = AutoPreparedStatement.create(c,
					"UPDATE Accounts SET accountBalance=accountBalance + ? WHERE accountName=?"
							+ helperClause);
			ps.setDouble(transactionAmount);
			ps.setString(accountName);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
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
			String helperClause = helperClause();
			c.setAutoCommit(false);
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"UPDATE Accounts SET accountBalance=accountBalance - (SELECT transactionAmount FROM Transactions WHERE transactionID = ?) WHERE accountName=(SELECT accountName FROM Transactions WHERE transactionID = ?)"
									+ helperClause);
			ps.setLong(transactionID);
			ps.setLong(transactionID);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
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
			String helperClause = helperClause();
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT accountName,accountBalance-COALESCE(future,0) as accountBalance,accountHidden FROM Accounts LEFT OUTER JOIN (SELECT SUM(transactionAmount) as future,accountName as accName FROM",
					"GROUP BY accName",
					") as FutureEvents ON accountName = accName WHERE accountHidden <= ? "
							+ helperClause + " ORDER BY accountName ASC",
					until, null, false, showHidden, false, false);
			ps.setInt(showHidden);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
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
			String helperClause = helperClause();
			AutoPreparedStatement ps = AutoPreparedStatement.create(c,
					"SELECT accountName FROM Accounts WHERE accountHidden <= ? "
							+ helperClause + " ORDER BY accountName ASC");
			ps.setInt(showHidden);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				list.add(results.getString("accountName"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

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
					"SELECT transactionID,accountName,transactionAmount,transactionDate,transactionComment FROM",
					"AND accountName IN " + selectedAccounts, "", fromDate,
					toDate, true, showHidden, true, true);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] row = new Object[5];
				row[0] = results.getLong("transactionID");
				row[1] = results.getString("accountName");
				row[2] = results.getDouble("transactionAmount");
				row[3] = dateFormat.parse(results.getString("transactionDate"));
				row[4] = results.getString("transactionComment");
				list.add(row);
			}
		} catch (SQLException | ParseException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<Object[]> getMonthlyRevenues(Date until) {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT "
							+ yearGrouper("transactionDate")
							+ "as transactionYear,"
							+ monthGrouper("transactionDate")
							+ "as transactionMonth,SUM(transactionAmount) as revenue FROM Accounts NATURAL JOIN (SELECT * FROM ",
					"",
					") as FutureTransactions GROUP BY transactionYear, transactionMonth ORDER BY transactionYear DESC, transactionMonth DESC",
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
					"SELECT "
							+ yearGrouper("transactionDate")
							+ "as transactionYear,SUM(transactionAmount) as revenue FROM Accounts NATURAL JOIN (SELECT * FROM ",
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
					"SELECT accountName,"
							+ yearGrouper("transactionDate")
							+ "as transactionYear,"
							+ monthGrouper("transactionDate")
							+ "as transactionMonth,SUM(transactionAmount) as revenue FROM Accounts NATURAL JOIN (SELECT * FROM ",
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
					"SELECT accountName,"
							+ yearGrouper("transactionDate")
							+ "as transactionYear,SUM(transactionAmount) as revenue FROM Accounts NATURAL JOIN (SELECT * FROM ",
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
					"AND accountName IN " + selectedAccounts, "", from, to,
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
					"AND accountName IN " + consideredAccounts, "", from, to,
					false, showHidden, true, true);
		} else {
			ps = selectBetweenDates(
					"SELECT SUM(transactionAmount) as Amount FROM",
					"AND accountName = ?", "", from, to, false, showHidden,
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
					"SELECT transactionAmount,transactionDate FROM",
					"AND accountName IN " + consideredAccounts, "", from, to,
					true, showHidden, true, true);
		} else {
			ps = selectBetweenDates(
					"Select transactionAmount,transactionDate FROM",
					"AND accountName = ?", "", from, to, true, showHidden,
					true, true);
			ps.setString(accountName);
		}
		transactions = ps.executeQuery();
		while (transactions.next()) {
			startBalance += transactions.getDouble("transactionAmount");
			try {
				datapoints.put(dateFormat.parse(transactions
						.getString("transactionDate")), startBalance);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		dataset.put(accountName, datapoints);
	}

	public double getTotalAccountBalanceSum(Date until) {
		try {
			String helperClause = helperClause();
			if (helperClause.length() > 0) {
				helperClause = helperClause.replaceAll("AND", "WHERE");
			}
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT SUM(accountBalance) - COALESCE((SELECT SUM(transactionAmount) FROM",
					"", "),0) as balanceSum FROM Accounts" + helperClause,
					until, null, false, 1, false, true);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
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
			String helperClause = helperClause();
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT SUM(accountBalance) -  COALESCE((SELECT SUM(transactionAmount) FROM ",
					"AND accountHidden = 0",
					"),0) as balanceSum FROM Accounts WHERE accountHidden = 0"
							+ helperClause, until, null, false, 1, false, true);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
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
			String helperClause = helperClause();
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT SUM(accountBalance) -  COALESCE((SELECT SUM(transactionAmount) FROM ",
					"AND accountHidden = 1",
					"),0) as balanceSum FROM Accounts WHERE accountHidden = 1"
							+ helperClause, until, null, false, 1, false, true);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				return result.getDouble("balanceSum");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0.;
	}

	public abstract void exportDatabase(OutputStream out, String exportMessage);

	protected abstract void importDatabaseHelper(String table, String sql);

	public void importDatabaseOld(InputStream in, String importMessage) {
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
				int yearIndex = -1;
				int monthIndex = -1;
				int dayIndex = -1;
				if (name.equals("Transactions")) {
					String[] headerParts = header.split(",");
					String newHeader = "";
					for (int i = 0; i < headerParts.length; i++) {
						if (headerParts[i].equals("transactionDay")) {
							dayIndex = i;
						} else if (headerParts[i].equals("transactionMonth")) {
							monthIndex = i;
						} else if (headerParts[i].equals("transactionYear")) {
							yearIndex = i;
						} else {
							newHeader += headerParts[i] + ",";
						}
					}
					if (yearIndex >= 0 && monthIndex >= 0 && dayIndex >= 0) {
						newHeader += "transactionDate";
						header = newHeader;
					}
				}
				String line;
				while (scan.hasNextLine()
						&& !(line = scan.nextLine()).isEmpty()) {
					String[] row = line.split(",");
					if (name.equals("Transactions") && yearIndex >= 0
							&& monthIndex >= 0 && dayIndex >= 0) {
						String newRow[] = new String[row.length - 2];
						for (int i = 0, j = 0; i < row.length; i++) {
							if (i != yearIndex && i != monthIndex
									&& i != dayIndex) {
								newRow[j] = row[i];
								j++;
							}
						}
						String date = row[yearIndex] + "-" + row[monthIndex]
								+ "-" + row[dayIndex];
						newRow[newRow.length - 1] = date;
						row = newRow;
					}
					String sql = "INSERT INTO " + name + " (" + header + ")"
							+ " VALUES (";
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

	public long getNumberOfTransactions() {
		try {
			String helperClause = helperClause();
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT COUNT(*) as number FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ?"
									+ helperClause);
			ps.setInt(showHidden);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
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
			String helperClause = helperClause();
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT COUNT(*) as number FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionAmount > 0"
									+ helperClause);
			ps.setInt(showHidden);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
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
			String helperClause = helperClause();
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT COUNT(*) as number FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionAmount < 0"
									+ helperClause);
			ps.setInt(showHidden);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
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
			String helperClause = helperClause();
			String query = "SELECT "
					+ sql
					+ "(transactionDate) as transactionDate FROM Accounts NATURAL JOIN Transactions WHERE accountHidden <= ?"
					+ helperClause;
			AutoPreparedStatement ps = AutoPreparedStatement.create(c, query);
			ps.setInt(showHidden);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
			ResultSet result = ps.executeQuery();
			Date date = null;
			while (result.next()) {
				String d = result.getString("transactionDate");
				if (d == null) {
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
			String helperClause = helperClause();
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT transactionID,accountName,transactionAmount FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionDate = ? AND transactionComment = ?"
									+ helperClause);
			ps.setInt(showHidden);
			ps.setDate(transactionDate);
			ps.setString(transactionComment);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
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

	public abstract void close() throws SQLException;

	protected void deleteEntries(String table) {
		try {
			String helperClause = helperClause();
			if (helperClause.length() > 0) {
				helperClause = helperClause.replaceAll("AND", "WHERE");
			}
			AutoPreparedStatement ps = AutoPreparedStatement.create(c,
					"DELETE FROM " + table + helperClause);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
			ps.executeUpdate();
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
			String helperAdd = helperAdd();
			String helperAddValue = helperAddValue();
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"INSERT INTO StoredTransactions(accountName,transactionAmount,transactionComment"
									+ helperAdd
									+ ") VALUES (?,?,?"
									+ helperAddValue + ")");
			ps.setString(accountName);
			ps.setDouble(transactionAmount);
			ps.setString(transactionComment);
			if (helperAdd.length() > 0) {
				ps.setString(helperValue());
			}
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
			String helperClause = helperClause();
			if (helperClause.length() > 0) {
				helperClause = helperClause.replaceAll("AND", "WHERE");
			}
			AutoPreparedStatement ps = AutoPreparedStatement.create(c,
					"SELECT transactionComment FROM StoredTransactions "
							+ helperClause + " GROUP BY transactionComment");
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
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
			String helperClause = helperClause();
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT transactionID,accountName,transactionAmount FROM StoredTransactions WHERE transactionComment=?"
									+ helperClause);
			ps.setString(transactionComment);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
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
			String helperClause = helperClause();
			if (helperClause.length() > 0) {
				helperClause = helperClause.replaceAll("AND", "WHERE");
			}
			AutoPreparedStatement ps = AutoPreparedStatement
					.create(c,
							"SELECT transactionID,accountName,transactionAmount,transactionComment FROM StoredTransactions"
									+ helperClause);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
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
			String helperClause = helperClause();
			AutoPreparedStatement ps = selectBetweenDates(
					"SELECT accountBalance-COALESCE((SELECT SUM(transactionAmount) FROM",
					"AND accountName = ?",
					"),0) as accountBalance FROM Accounts WHERE accountName = ? "
							+ helperClause, until, null, false, showHidden,
					false, true);
			ps.setString(accountName);
			ps.setString(accountName);
			if (helperClause.length() > 0) {
				ps.setString(helperValue());
			}
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
		String sqlWhere = "AND ";
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
		if (sqlWhere.equals("AND ")) {
			sqlWhere = "";
		}
		AutoPreparedStatement ps;
		try {
			ps = selectBetweenDates(
					"SELECT transactionID,accountName,transactionAmount,transactionDate,transactionComment FROM",
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
				row[3] = dateFormat.parse(results.getString("transactionDate"));
				row[4] = results.getString("transactionComment");
				list.add(row);
			}
		} catch (SQLException | ParseException e) {
			e.printStackTrace();
		}
		return list;
	}

	protected abstract String helperClause();

	protected abstract String helperValue();

	protected abstract String helperAdd();

	protected abstract String helperAddValue();

	protected AutoPreparedStatement selectBetweenDates(String sqlSelect,
			String sqlWhere, String sqlEnd, Date from, Date to,
			boolean ascending, int showHidden, boolean inclusive,
			boolean doOrder) throws SQLException {

		String order = ascending ? "ASC" : "DESC";
		String less = inclusive ? "<=" : "<";
		String great = inclusive ? ">=" : ">";

		String clause = "";
		if (from != null) {
			clause += " AND transactionDate " + great + " ?";
		}
		if (to != null) {
			clause += " AND transactionDate " + less + " ?";
		}

		String helperClause = helperClause();

		String sqlDate = " Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? "
				+ helperClause + clause;

		String sql = sqlSelect
				+ " "
				+ sqlDate
				+ " "
				+ sqlWhere
				+ (doOrder ? " ORDER BY transactionDate " + order
						+ ",transactionID " + order : "") + " " + sqlEnd;

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
			ps.setPlacedDate(index++, from);
		}
		if (to != null) {
			ps.setPlacedDate(index++, to);
		}
		return ps;
	}

}