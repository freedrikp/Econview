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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Scanner;
import java.util.TreeMap;

import javax.swing.Icon;

import se.freedrikp.econview.gui.Utilities;

public class Database extends Observable {
	private Connection c;
	private File dbfile;
	private int onlyIncluded;

	public Database(String dbfile) {
		this.dbfile = new File(dbfile);
		onlyIncluded = 1;
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
					+ "accountIncluded INTEGER DEFAULT '1'" + ")";
			c.prepareStatement(sql).executeUpdate();
			sql = "CREATE TABLE Transactions("
					+ "transactionID INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "accountName TEXT," + "transactionAmount REAL,"
					+ "transactionYear TEXT," + "transactionMonth TEXT,"
					+ "transactionDay TEXT," + "transactionComment TEXT" + ")";
			c.prepareStatement(sql).executeUpdate();
			c.commit();
			c.setAutoCommit(true);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addAccount(String accountName, double accountBalance,
			boolean accountIncluded) {
		try {
			PreparedStatement ps = c
					.prepareStatement("INSERT INTO Accounts VALUES (?,?,?)");
			ps.setString(1, accountName);
			ps.setDouble(2, accountBalance);
			int included = accountIncluded ? 1 : 0;
			ps.setInt(3, included);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void editAccount(String oldAccountName, String accountName,
			double accountBalance, boolean accountIncluded) {
		PreparedStatement ps;
		try {
			c.setAutoCommit(false);
			ps = c.prepareStatement("UPDATE Transactions SET accountName=? WHERE accountName=?");
			ps.setString(1, accountName);
			ps.setString(2,oldAccountName);
			ps.executeUpdate();
			ps = c.prepareStatement("UPDATE Accounts SET accountName=?, accountBalance=?, accountIncluded = ? WHERE accountName=?");
			ps.setString(1, accountName);
			ps.setDouble(2, accountBalance);
			int included = accountIncluded ? 1 : 0;
			ps.setInt(3, included);
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
			PreparedStatement ps = c.prepareStatement("DELETE FROM Transactions WHERE accountName=?");
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

	public List<String[]> getAccounts() {
		ArrayList<String[]> list = new ArrayList<String[]>();
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT accountName,accountBalance,accountIncluded FROM Accounts WHERE accountIncluded >= ?");
			ps.setInt(1,onlyIncluded);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				String[] row = new String[3];
				row[0] = results.getString("accountName");
				row[1] = results.getString("accountBalance");
				row[2] = Boolean
						.toString(results.getInt("accountIncluded") == 1);
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<String[]> getTransactions() {
		ArrayList<String[]> list = new ArrayList<String[]>();
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT transactionID,accountName,transactionAmount,transactionYear,transactionMonth,transactionDay,transactionComment FROM Transactions NATURAL JOIN Accounts WHERE accountIncluded >= ?");
			ps.setInt(1,onlyIncluded);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				String[] row = new String[5];
				row[0] = results.getString("transactionID");
				row[1] = results.getString("accountName");
				row[2] = results.getString("transactionAmount");
				row[3] = results.getString("transactionYear") + "-"
						+ results.getString("transactionMonth") + "-"
						+ results.getString("transactionDay");
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
					.prepareStatement("SELECT accountName FROM Accounts WHERE accountIncluded >= ?");
			ps.setInt(1,onlyIncluded);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				list.add(results.getString("accountName"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<String[]> getMonthlyRevenues() {
		ArrayList<String[]> list = new ArrayList<String[]>();
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT transactionYear,transactionMonth,SUM(transactionAmount) as revenue FROM Transactions NATURAL JOIN Accounts  WHERE accountIncluded >= ? GROUP BY transactionYear,transactionMonth");
			ps.setInt(1,onlyIncluded);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				String[] row = new String[3];
				row[0] = results.getString("transactionYear");
				row[1] = results.getString("transactionMonth");
				row[2] = results.getString("revenue");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<String[]> getYearlyRevenues() {
		ArrayList<String[]> list = new ArrayList<String[]>();
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT transactionYear,SUM(transactionAmount) as revenue FROM Transactions NATURAL JOIN Accounts  WHERE accountIncluded >= ? GROUP BY transactionYear");
			ps.setInt(1,onlyIncluded);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				String[] row = new String[2];
				row[0] = results.getString("transactionYear");
				row[1] = results.getString("revenue");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<String[]> getMonthlyAccountRevenues() {
		ArrayList<String[]> list = new ArrayList<String[]>();
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT accountName,transactionYear,transactionMonth,SUM(transactionAmount) as revenue FROM Transactions NATURAL JOIN Accounts  WHERE accountIncluded >= ? GROUP BY accountName,transactionYear,transactionMonth");
			ps.setInt(1,onlyIncluded);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				String[] row = new String[4];
				row[0] = results.getString("transactionYear");
				row[1] = results.getString("transactionMonth");
				row[2] = results.getString("accountName");
				row[3] = results.getString("revenue");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public List<String[]> getYearlyAccountRevenues() {
		ArrayList<String[]> list = new ArrayList<String[]>();
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT accountName,transactionYear,SUM(transactionAmount) as revenue FROM Transactions NATURAL JOIN Accounts  WHERE accountIncluded >= ? GROUP BY accountName,transactionYear");
			ps.setInt(1,onlyIncluded);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				String[] row = new String[3];
				row[0] = results.getString("transactionYear");
				row[1] = results.getString("accountName");
				row[2] = results.getString("revenue");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public String getTotalRevenue() {
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT SUM(transactionAmount) as revenue FROM Transactions NATURAL JOIN Accounts  WHERE accountIncluded >= ?");
			ps.setInt(1,onlyIncluded);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				String res = results.getString("revenue");
				return res != null ? res : "0";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "0";
	}

	public List<String[]> getTotalAccountRevenues() {
		ArrayList<String[]> list = new ArrayList<String[]>();
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT accountName,SUM(transactionAmount) as revenue FROM Transactions NATURAL JOIN Accounts  WHERE accountIncluded >= ? GROUP BY accountName");
			ps.setInt(1,onlyIncluded);
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				String[] row = new String[2];
				row[0] = results.getString("accountName");
				row[1] = results.getString("revenue");
				list.add(row);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public String getRevenue(Date from, Date to, String account) {
		try {
			PreparedStatement ps;
			if (account.isEmpty()) {
				ps = selectBetweenDates(
						"Select SUM(transactionAmount) as revenue FROM", "",
						from, to);
			} else {
				ps = selectBetweenDates(
						"Select SUM(transactionAmount) as revenue FROM",
						"WHERE accountName = ?", from, to);
				ps.setString(13, account);
			}
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				String res = results.getString("revenue");
				return res != null ? res : "0";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "0";
	}

	public Map<String, Map<String, Double>> getCustomDiagramData(Date from,
			Date to) {
		SimpleDateFormat year = new SimpleDateFormat("yyyy");
		SimpleDateFormat month = new SimpleDateFormat("MM");
		SimpleDateFormat day = new SimpleDateFormat("dd");
		String foy = year.format(from);
		String fom = month.format(from);
		String fod = day.format(from);
		// System.out.println(foy + "-" + fom + "-" + fod + "<->" + toy + "-" +
		// tom + "-" + tod);
		Map<String, Map<String, Double>> dataset = new TreeMap<String, Map<String, Double>>();
		try {
			PreparedStatement ps = c
					.prepareStatement("Select accountName,accountBalance FROM Accounts WHERE accountIncluded >= ?");
			ps.setInt(1,onlyIncluded);
			ResultSet accounts = ps.executeQuery();
			double totalStartBalance = 0;
			while (accounts.next()) {
				double startBalance = accounts.getDouble("accountBalance");
				totalStartBalance += startBalance;
				String accountName = accounts.getString("accountName");
				buildDiagramDataset(from, to, dataset, startBalance,
						accountName);
			}
			buildDiagramDataset(from, to, dataset, totalStartBalance, Utilities.getString("TOTAL_ACCOUNT_NAME"));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dataset;
	}

	private void buildDiagramDataset(Date from, Date to,
			Map<String, Map<String, Double>> dataset, double startBalance,
			String accountName) throws SQLException {
		Map<String, Double> datapoints = new TreeMap<String, Double>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		datapoints.put(sdf.format(to), startBalance);
		PreparedStatement ps;
		if (accountName.equals("Total")) {
			ps = selectBetweenDates("SELECT transactionAmount FROM", "", from,
					new Date());
		} else {
			ps = selectBetweenDates("SELECT transactionAmount FROM",
					"WHERE accountName = ?", from, new Date());
			ps.setString(14, accountName);
		}
		ResultSet transactions = ps.executeQuery();
		while (transactions.next()) {
			startBalance -= transactions.getDouble("transactionAmount");
		}
		datapoints.put(sdf.format(from), startBalance);
		if (accountName.equals("Total")) {
			ps = selectBetweenDates(
					"SELECT transactionAmount,transactionYear,transactionMonth,transactionDay FROM",
					"", from, to);
		} else {
			ps = selectBetweenDates(
					"Select transactionAmount,transactionYear,transactionMonth,transactionDay FROM",
					"WHERE accountName = ?", from, to);
			ps.setString(14, accountName);
		}
		transactions = ps.executeQuery();
		while (transactions.next()) {
			String date = transactions.getString("transactionYear") + "-"
					+ transactions.getString("transactionMonth") + "-"
					+ transactions.getString("transactionDay");
			startBalance += transactions.getDouble("transactionAmount");
			datapoints.put(date, startBalance);
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
		String sqlYears = "(SELECT * FROM Transactions NATURAL JOIN Accounts WHERE accountIncluded >= ? AND transactionYear <= ? AND transactionYear >= ?)";
		String sqlMonths = "(SELECT * FROM "
				+ sqlYears
				+ " WHERE NOT (transactionYear == ? AND transactionMonth > ? OR transactionYear == ?  AND transactionmonth < ?))";
		String sqlDays = "(SELECT * FROM "
				+ sqlMonths
				+ " WHERE NOT (transactionYear == ? AND transactionMonth == ? AND transactionDay > ? OR transactionYear == ? AND transactionMonth == ? AND transactionDay < ?))";
		PreparedStatement ps = c.prepareStatement(sqlSelect + " " + sqlDays
				+ " " + sqlWhere);
		ps.setInt(1,onlyIncluded);
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

	public String getAccountBalanceSum(boolean onlyIncluded) {
		try {
			int incl = onlyIncluded ? 1 : 0;
			PreparedStatement ps = c
					.prepareStatement("SELECT SUM(accountBalance) as balanceSum FROM Accounts WHERE accountIncluded >= ?");
			ps.setInt(1,incl);
			ResultSet result = ps.executeQuery();
			while (result.next()) {
				String res = result.getString("balanceSum");
				return res != null ? res : "0";
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "0";
	}

	public void exportDatabase(OutputStream out) {
		PrintWriter pw = new PrintWriter(out);
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT name,type FROM sqlite_master WHERE type IN ('table','view') AND name NOT LIKE 'sqlite_%'");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				pw.println(rs.getString("name"));
				// ps = c.prepareStatement("SELECT COUNT(*) FROM " +
				// rs.getString("name"));
				// ResultSet row = ps.executeQuery();
				// while(row.next()){
				// pw.println(row.getString(1));
				// }
				ps = c.prepareStatement("SELECT * FROM " + rs.getString("name"));
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
			Scanner scan = new Scanner(in);
			while (scan.hasNextLine()) {
				String name = scan.nextLine();
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
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void openFile(File dbfile) {
		this.dbfile = dbfile;
		try {
			c.close();
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
	
	public void setOnlyIncluded(boolean onlyIncluded){
		this.onlyIncluded = onlyIncluded ? 1 : 0;
		setChanged();
		notifyObservers();
	}

	public boolean getOnlyIncluded() {
		return onlyIncluded > 0 ? true : false;
	}
}
