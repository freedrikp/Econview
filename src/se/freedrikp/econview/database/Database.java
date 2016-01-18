package se.freedrikp.econview.database;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
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

import se.freedrikp.econview.gui.Language;

public class Database extends Observable {
	private Connection c;
	private File dbfile;
	private int showHidden;

	// private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
	// "yyyy-MM-dd");

	public Database(String dbfile) {
		this.dbfile = new File(dbfile);
		showHidden = 0;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			if (!this.dbfile.exists() || this.dbfile.length() == 0) {
				initdb();
			}
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
	}

	public void initdb() {
		try {
			c.setAutoCommit(false);
			String sql = "CREATE TABLE Accounts("
					+ "accountName TEXT PRIMARY KEY,"
					+ "accountBalance REAL DEFAULT 0.0,"
					+ "accountHidden INTEGER DEFAULT '1'" + ")";
			c.prepareStatement(sql).executeUpdate();
			sql = "CREATE TABLE Transactions("
					+ "transactionID INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "accountName TEXT," + "transactionAmount REAL,"
					+ "transactionYear TEXT," + "transactionMonth TEXT,"
					+ "transactionDay TEXT," + "transactionComment TEXT" + ")";
			c.prepareStatement(sql).executeUpdate();
			sql = "CREATE TABLE StoredTransactions("
					+ "transactionID INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "accountName TEXT," + "transactionAmount REAL,"
					+ "transactionComment TEXT" + ")";
			c.prepareStatement(sql).executeUpdate();
			c.commit();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addAccount(String accountName, double accountBalance,
			boolean accountHidden) {
		try {
			PreparedStatement ps = c
					.prepareStatement("INSERT INTO Accounts VALUES (?,?,?)");
			ps.setString(1, accountName);
			ps.setDouble(2, accountBalance);
			int hidden = accountHidden ? 1 : 0;
			ps.setInt(3, hidden);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void editAccount(String oldAccountName, String accountName,
			double accountBalance, boolean accountHidden) {
		PreparedStatement ps;
		try {
			c.setAutoCommit(false);
			ps = c.prepareStatement("UPDATE Transactions SET accountName=? WHERE accountName=?");
			ps.setString(1, accountName);
			ps.setString(2, oldAccountName);
			ps.executeUpdate();
			ps = c.prepareStatement("UPDATE Accounts SET accountName=?, accountBalance=?, accountHidden = ? WHERE accountName=?");
			ps.setString(1, accountName);
			ps.setDouble(2, accountBalance);
			int hidden = accountHidden ? 1 : 0;
			ps.setInt(3, hidden);
			ps.setString(4, oldAccountName);
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
			PreparedStatement ps = c
					.prepareStatement("DELETE FROM Transactions WHERE accountName=?");
			ps.setString(1, accountName);
			ps.executeUpdate();
			ps = c.prepareStatement("DELETE FROM Accounts WHERE accountName=?");
			ps.setString(1, accountName);
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
			PreparedStatement ps = c
					.prepareStatement("INSERT INTO Transactions(accountName,transactionAmount,transactionYear,transactionMonth,transactionDay,transactionComment) VALUES (?,?,?,?,?,?)");
			ps.setString(1, accountName);
			ps.setDouble(2, transactionAmount);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
			ps.setString(3, sdf.format(transactionDate));
			sdf = new SimpleDateFormat("MM");
			ps.setString(4, sdf.format(transactionDate));
			sdf = new SimpleDateFormat("dd");
			ps.setString(5, sdf.format(transactionDate));
			ps.setString(6, transactionComment);
			ps.executeUpdate();
			ps = c.prepareStatement("UPDATE Accounts SET accountBalance=accountBalance + ? WHERE accountName=?");
			ps.setDouble(1, transactionAmount);
			ps.setString(2, accountName);
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
			PreparedStatement ps = c
					.prepareStatement("UPDATE Accounts SET accountBalance=accountBalance - (SELECT transactionAmount FROM Transactions WHERE transactionID = ?) WHERE accountName=(SELECT accountName FROM Transactions WHERE transactionID = ?)");
			ps.setLong(1, transactionID);
			ps.setLong(2, transactionID);
			ps.executeUpdate();
			ps = c.prepareStatement("UPDATE Transactions SET accountName = ?,transactionAmount = ?,transactionYear = ?,transactionMonth = ?,transactionDay = ?,transactionComment = ? WHERE transactionID = ?");
			ps.setString(1, accountName);
			ps.setDouble(2, transactionAmount);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
			ps.setString(3, sdf.format(transactionDate));
			sdf = new SimpleDateFormat("MM");
			ps.setString(4, sdf.format(transactionDate));
			sdf = new SimpleDateFormat("dd");
			ps.setString(5, sdf.format(transactionDate));
			ps.setString(6, transactionComment);
			ps.setLong(7, transactionID);
			ps.executeUpdate();
			ps = c.prepareStatement("UPDATE Accounts SET accountBalance=accountBalance + ? WHERE accountName=?");
			ps.setDouble(1, transactionAmount);
			ps.setString(2, accountName);
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
			PreparedStatement ps = c
					.prepareStatement("UPDATE Accounts SET accountBalance=accountBalance - (SELECT transactionAmount FROM Transactions WHERE transactionID = ?) WHERE accountName=(SELECT accountName FROM Transactions WHERE transactionID = ?)");
			ps.setLong(1, transactionID);
			ps.setLong(2, transactionID);
			ps.executeUpdate();
			ps = c.prepareStatement("DELETE FROM Transactions WHERE transactionID = ?");
			ps.setLong(1, transactionID);
			ps.executeUpdate();
			c.commit();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public List<Object[]> getAccounts() {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT accountName,accountBalance,accountHidden FROM Accounts WHERE accountHidden <= ? ORDER BY accountName ASC");
			ps.setInt(1, showHidden);
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
			PreparedStatement ps = selectBetweenDates(
					"SELECT transactionID,accountName,transactionAmount,transactionYear,transactionMonth,transactionDay,transactionComment FROM",
					"Where accountName IN " + selectedAccounts, fromDate,
					toDate);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] row = new Object[5];
				row[0] = results.getLong("transactionID");
				row[1] = results.getString("accountName");
				row[2] = results.getDouble("transactionAmount");
				Calendar cal = Calendar.getInstance();
				cal.set(results.getInt("transactionYear"),
						results.getInt("transactionMonth") - 1,
						results.getInt("transactionDay"), 0, 0, 0);
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
			PreparedStatement ps = c
					.prepareStatement("SELECT accountName FROM Accounts WHERE accountHidden <= ? ORDER BY accountName ASC");
			ps.setInt(1, showHidden);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				list.add(results.getString("accountName"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<Object[]> getMonthlyRevenues() {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT transactionYear,transactionMonth,SUM(transactionAmount) as revenue FROM Transactions NATURAL JOIN Accounts  WHERE accountHidden <= ? GROUP BY transactionYear,transactionMonth ORDER BY transactionYear DESC, transactionMonth DESC");
			ps.setInt(1, showHidden);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] row = new Object[2];
				Calendar cal = Calendar.getInstance();
				cal.set(results.getInt("transactionYear"),
						results.getInt("transactionMonth") - 1, 1, 0, 0, 0);
				row[0] = cal.getTime();
				row[1] = results.getDouble("revenue");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<Object[]> getYearlyRevenues() {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT transactionYear,SUM(transactionAmount) as revenue FROM Transactions NATURAL JOIN Accounts  WHERE accountHidden <= ? GROUP BY transactionYear ORDER BY transactionYear DESC");
			ps.setInt(1, showHidden);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] row = new Object[2];
				Calendar cal = Calendar.getInstance();
				cal.set(results.getInt("transactionYear"), 0, 1, 0, 0, 0);
				row[0] = cal.getTime();
				row[1] = results.getDouble("revenue");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<Object[]> getMonthlyAccountRevenues() {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT accountName,transactionYear,transactionMonth,SUM(transactionAmount) as revenue FROM Transactions NATURAL JOIN Accounts  WHERE accountHidden <= ? GROUP BY accountName,transactionYear,transactionMonth ORDER BY transactionYear DESC, transactionMonth DESC, accountName ASC");
			ps.setInt(1, showHidden);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] row = new Object[3];
				Calendar cal = Calendar.getInstance();
				cal.set(results.getInt("transactionYear"),
						results.getInt("transactionMonth") - 1, 1, 0, 0, 0);
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

	public List<Object[]> getYearlyAccountRevenues() {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT accountName,transactionYear,SUM(transactionAmount) as revenue FROM Transactions NATURAL JOIN Accounts  WHERE accountHidden <= ? GROUP BY accountName,transactionYear ORDER BY transactionYear DESC, accountName ASC");
			ps.setInt(1, showHidden);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				Object[] row = new Object[3];
				Calendar cal = Calendar.getInstance();
				cal.set(results.getInt("transactionYear"), 0, 1, 0, 0, 0);
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

	public Double getTotalRevenue() {
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT SUM(transactionAmount) as revenue FROM Transactions NATURAL JOIN Accounts  WHERE accountHidden <= ?");
			ps.setInt(1, showHidden);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				return results.getDouble("revenue");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0.;
	}

	public List<Object[]> getTotalAccountRevenues() {
		ArrayList<Object[]> list = new ArrayList<Object[]>();
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT accountName,SUM(transactionAmount) as revenue FROM Transactions NATURAL JOIN Accounts  WHERE accountHidden <= ? GROUP BY accountName");
			ps.setInt(1, showHidden);
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
			PreparedStatement ps;
			// if (account.isEmpty()) {
			// ps = selectBetweenDates(
			// "Select SUM(transactionAmount) as revenue FROM", "",
			// from, to);
			// } else {
			ps = selectBetweenDates(
					"Select SUM(transactionAmount) as revenue FROM",
					"WHERE accountName IN " + selectedAccounts, from, to);
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
			Date to, Collection<String> accounts, boolean includeTotal) {
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
			PreparedStatement ps = c
					.prepareStatement("Select accountName,accountBalance FROM Accounts WHERE accountHidden <= ? AND accountName IN "
							+ selectedAccounts);
			ps.setInt(1, showHidden);
			ResultSet accs = ps.executeQuery();
			double totalStartBalance = 0;
			while (accs.next()) {
				double startBalance = accs.getDouble("accountBalance");
				totalStartBalance += startBalance;
				String accountName = accs.getString("accountName");
				buildDiagramDataset(from, to, dataset, startBalance,
						accountName, null);
			}
			if (includeTotal) {
				buildDiagramDataset(from, to, dataset, totalStartBalance,
						Language.getString("TOTAL_ACCOUNT_NAME"),
						selectedAccounts);
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
			String accountName, String consideredAccounts) throws SQLException {
		Map<Date, Double> datapoints = new TreeMap<Date, Double>();
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		datapoints.put(to, startBalance);
		PreparedStatement ps;
		Date latest = getNewestTransactionDate();
		latest = latest != null ? latest : new Date();
		if (accountName.equals(Language.getString("TOTAL_ACCOUNT_NAME"))) {
			ps = selectBetweenDates(
					"SELECT sum(transactionAmount) as Amount FROM",
					"WHERE accountName IN " + consideredAccounts, from, latest);
		} else {
			ps = selectBetweenDates(
					"SELECT SUM(transactionAmount) as Amount FROM",
					"WHERE accountName = ?", from, latest);
			ps.setString(14, accountName);
		}
		ResultSet transactions = ps.executeQuery();
		while (transactions.next()) {
			startBalance -= transactions.getDouble("Amount");
		}
		datapoints.put(from, startBalance);
		if (accountName.equals(Language.getString("TOTAL_ACCOUNT_NAME"))) {
			ps = selectBetweenDates(
					"SELECT transactionAmount,transactionYear,transactionMonth,transactionDay FROM",
					"WHERE accountName IN " + consideredAccounts, from, to);
		} else {
			ps = selectBetweenDates(
					"Select transactionAmount,transactionYear,transactionMonth,transactionDay FROM",
					"WHERE accountName = ?", from, to);
			ps.setString(14, accountName);
		}
		transactions = ps.executeQuery();
		while (transactions.next()) {
			Calendar cal = Calendar.getInstance();
			cal.set(transactions.getInt("transactionYear"),
					transactions.getInt("transactionMonth") - 1,
					transactions.getInt("transactionDay"), 0, 0, 0);
			startBalance += transactions.getDouble("transactionAmount");
			datapoints.put(cal.getTime(), startBalance);
		}
		dataset.put(accountName, datapoints);
	}

	private PreparedStatement selectBetweenDates(String sqlSelect,
			String sqlWhere, Date from, Date to) throws SQLException {
		SimpleDateFormat year = new SimpleDateFormat("yyyy");
		SimpleDateFormat month = new SimpleDateFormat("MM");
		SimpleDateFormat day = new SimpleDateFormat("dd");
		String toy = year.format(to);
		String tom = month.format(to);
		String tod = day.format(to);
		String foy = year.format(from);
		String fom = month.format(from);
		String fod = day.format(from);
		String sqlYears = "(SELECT * FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionYear <= ? AND transactionYear >= ?)";
		String sqlMonths = "(SELECT * FROM "
				+ sqlYears
				+ " WHERE NOT (transactionYear == ? AND transactionMonth > ? OR transactionYear == ?  AND transactionmonth < ?))";
		String sqlDays = "(SELECT * FROM "
				+ sqlMonths
				+ " WHERE NOT (transactionYear == ? AND transactionMonth == ? AND transactionDay > ? OR transactionYear == ? AND transactionMonth == ? AND transactionDay < ?))";
		PreparedStatement ps = c
				.prepareStatement(sqlSelect
						+ " "
						+ sqlDays
						+ " "
						+ sqlWhere
						+ "ORDER BY transactionYear,transactionMonth,transactionDay,transactionID ASC");
		ps.setInt(1, showHidden);
		ps.setString(2, toy);
		ps.setString(3, foy);

		ps.setString(4, toy);
		ps.setString(5, tom);
		ps.setString(6, foy);
		ps.setString(7, fom);

		ps.setString(8, toy);
		ps.setString(9, tom);
		ps.setString(10, tod);
		ps.setString(11, foy);
		ps.setString(12, fom);
		ps.setString(13, fod);
		return ps;
	}

	public double getTotalAccountBalanceSum() {
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT SUM(accountBalance) as balanceSum FROM Accounts");
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				return result.getDouble("balanceSum");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0.;
	}

	public double getVisibleAccountBalanceSum() {
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT SUM(accountBalance) as balanceSum FROM Accounts WHERE accountHidden = 0");
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				return result.getDouble("balanceSum");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0.;
	}

	public double getHiddenAccountBalanceSum() {
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT SUM(accountBalance) as balanceSum FROM Accounts WHERE accountHidden = 1");
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				return result.getDouble("balanceSum");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0.;
	}

	public void exportDatabase(OutputStream out) {
		PrintWriter pw = new PrintWriter(out);
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT name,type FROM sqlite_master WHERE type IN ('table','view') AND name NOT LIKE 'sqlite_%'");
			ResultSet rs = ps.executeQuery();
			LinkedList<String> tableNames = new LinkedList<String>();
			long totalCount = 0;
			while (rs.next()) {
				tableNames.add(rs.getString("name"));
				ps = c.prepareStatement("SELECT COUNT(*) as Count FROM "
						+ rs.getString("name"));
				ResultSet row = ps.executeQuery();
				while (row.next()) {
					totalCount += row.getLong("Count");
				}
			}
			pw.println(totalCount);
			ProgressMonitor pm = new ProgressMonitor(null,
					Language.getString("EXPORTING_DATABASE"), "", 0, 100);
			final float percent = 100.0f / totalCount;
			float progress = 0;
			pm.setMillisToPopup(0);
			pm.setMillisToDecideToPopup(0);
			for (String table : tableNames) {
				pm.setNote(table);
				pw.println(table);
				ps = c.prepareStatement("SELECT * FROM " + table);
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

	public void importDatabase(InputStream in) {
		try {
			c.setAutoCommit(false);
			Scanner scan = new Scanner(in);
			long totalCount = 0;
			if (scan.hasNextLine()) {
				totalCount = Long.parseLong(scan.nextLine());
			}
			ProgressMonitor pm = new ProgressMonitor(null,
					Language.getString("IMPORTING_DATABASE"), "", 0, 100);
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
					PreparedStatement ps = c.prepareStatement(sql);
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

	public void openFile(File dbfile) {
		this.dbfile = dbfile;
		try {
			close();
			c = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			if (!this.dbfile.exists() || this.dbfile.length() == 0) {// !this.dbfile.exists())
																		// {
				initdb();
			}
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
			System.exit(0);
		}
		setChanged();
		notifyObservers();
	}

	public File getFile() {
		return dbfile;
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
			PreparedStatement ps = c
					.prepareStatement("SELECT COUNT(*) as number FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ?");
			ps.setInt(1, showHidden);
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
			PreparedStatement ps = c
					.prepareStatement("SELECT COUNT(*) as number FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionAmount > 0");
			ps.setInt(1, showHidden);
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
			PreparedStatement ps = c
					.prepareStatement("SELECT COUNT(*) as number FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionAmount < 0");
			ps.setInt(1, showHidden);
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
			PreparedStatement ps = c
					.prepareStatement("SELECT transactionYear as year,transactionMonth as month,transactionDay as day FROM Transactions NATURAL JOIN Accounts WHERE transactionYear IN ("
							+ year
							+ ") AND transactionMonth IN ("
							+ month
							+ ") AND transactionDay IN (" + day + ")");
			ps.setInt(1, showHidden);
			ps.setInt(2, showHidden);
			ps.setInt(3, showHidden);
			ps.setInt(4, showHidden);
			ps.setInt(5, showHidden);
			ps.setInt(6, showHidden);
			ps.setInt(7, showHidden);
			ResultSet result = ps.executeQuery();
			Date date = null;
			while (result.next()) {
				Calendar cal = Calendar.getInstance();
				cal.set(result.getInt("year"), result.getInt("month") - 1,
						result.getInt("day"), 0, 0, 0);
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
			PreparedStatement ps = c
					.prepareStatement("SELECT transactionID,accountName,transactionAmount FROM Transactions NATURAL JOIN Accounts WHERE accountHidden <= ? AND transactionYear = ? AND transactionMonth = ? AND transactionDay = ? AND transactionComment = ?");
			ps.setInt(1, showHidden);
			ps.setString(2,
					new SimpleDateFormat("yyyy").format(transactionDate));
			ps.setString(3, new SimpleDateFormat("MM").format(transactionDate));
			ps.setString(4, new SimpleDateFormat("dd").format(transactionDate));
			ps.setString(5, transactionComment);

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
			c = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			c.prepareStatement("VACUUM").executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		c.close();
	}

	private void deleteEntries(String table) {
		try {
			c.prepareStatement("DELETE FROM " + table).executeUpdate();
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
			PreparedStatement ps = c
					.prepareStatement("INSERT INTO StoredTransactions(accountName,transactionAmount,transactionComment) VALUES (?,?,?)");
			ps.setString(1, accountName);
			ps.setDouble(2, transactionAmount);
			ps.setString(3, transactionComment);
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
			PreparedStatement ps = c
					.prepareStatement("UPDATE StoredTransactions SET accountName = ?,transactionAmount = ?,transactionComment = ? WHERE transactionID = ?");
			ps.setString(1, accountName);
			ps.setDouble(2, transactionAmount);
			ps.setString(3, transactionComment);
			ps.setLong(4, transactionID);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void removeStoredTransaction(long transactionID) {
		try {
			PreparedStatement ps = c
					.prepareStatement("DELETE FROM StoredTransactions WHERE transactionID = ?");
			ps.setLong(1, transactionID);
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
			PreparedStatement ps = c
					.prepareStatement("SELECT transactionComment FROM StoredTransactions GROUP BY transactionComment");
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
			PreparedStatement ps = c
					.prepareStatement("SELECT transactionID,accountName,transactionAmount FROM StoredTransactions WHERE transactionComment=?");
			ps.setString(1, transactionComment);
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
			PreparedStatement ps = c
					.prepareStatement("SELECT transactionID,accountName,transactionAmount,transactionComment FROM StoredTransactions");
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
}
