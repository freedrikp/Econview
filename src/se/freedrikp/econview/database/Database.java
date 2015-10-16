package se.freedrikp.econview.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;

public class Database extends Observable {
	private Connection c;

	public Database(String dbfile) {
		boolean initdb = !new File(dbfile).exists();
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			if (initdb) {
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
					+ "accountBalance REAL DEFAULT 0.0" + ")";
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

	public void addAccount(String accountName, double accountBalance) {
		try {
			PreparedStatement ps = c
					.prepareStatement("INSERT INTO Accounts VALUES (?,?)");
			ps.setString(1, accountName);
			ps.setDouble(2, accountBalance);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void editAccount(String oldAccountName, String accountName,
			double accountBalance) {
		PreparedStatement ps;
		try {
			ps = c.prepareStatement("UPDATE Accounts SET accountName=?, accountBalance=? WHERE accountName=?");
			ps.setString(1, accountName);
			ps.setDouble(2, accountBalance);
			ps.setString(3, oldAccountName);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		setChanged();
		notifyObservers();
	}

	public void removeAccount(String accountName) {
		try {
			PreparedStatement ps = c
					.prepareStatement("DELETE FROM Accounts where accountName=?");
			ps.setString(1, accountName);
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
					.prepareStatement("SELECT accountName,accountBalance FROM Accounts");
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				String[] row = new String[2];
				row[0] = results.getString("accountName");
				row[1] = results.getString("accountBalance");
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
					.prepareStatement("SELECT transactionID,accountName,transactionAmount,transactionYear,transactionMonth,transactionDay,transactionComment FROM transactions");
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
					.prepareStatement("SELECT accountName FROM Accounts");
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
					.prepareStatement("SELECT transactionYear,transactionMonth,SUM(transactionAmount) as revenue FROM Transactions GROUP BY transactionYear,transactionMonth");
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
					.prepareStatement("SELECT transactionYear,SUM(transactionAmount) as revenue FROM Transactions GROUP BY transactionMonth");
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

	public String getTotalRevenue() {
		try {
			PreparedStatement ps = c
					.prepareStatement("SELECT SUM(transactionAmount) as revenue FROM Transactions");
			ResultSet results = ps.executeQuery();
			while (results.next()) {
				return results.getString("revenue");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "<PLACEHOLDER>";
	}

	public String getRevenue(Date from, Date to) {
		SimpleDateFormat year = new SimpleDateFormat("yyyy");
		SimpleDateFormat month = new SimpleDateFormat("MM");
		SimpleDateFormat day = new SimpleDateFormat("dd");
		String toy = year.format(to);
		String tom = month.format(to);
		String tod = day.format(to);
		String foy = year.format(from);
		String fom = month.format(from);
		String fod = day.format(from);
		String sqlYears = "(SELECT transactionYear,transactionMonth,transactionDay,transactionAmount FROM Transactions WHERE transactionYear <= ? AND transactionYear >= ?)";
		String sqlMonths = "(SELECT transactionYear,transactionMonth,transactionDay,transactionAmount FROM "
				+ sqlYears
				+ " WHERE NOT (transactionYear == ? AND transactionMonth > ? OR transactionYear == ?  AND transactionmonth < ?))";
		String sqlDays = "(SELECT transactionYear,transactionMonth,transactionDay,transactionAmount FROM "
				+ sqlMonths
				+ " WHERE NOT (transactionYear == ? AND transactionMonth == ? AND transactionDay > ? OR transactionYear == ? AND transactionMonth == ? AND transactionDay < ?))";
		try {
			PreparedStatement ps = c
					.prepareStatement("Select SUM(transactionAmount) as revenue FROM "
							+ sqlDays);

			ps.setString(1, toy);
			ps.setString(2, foy);

			ps.setString(3, toy);
			ps.setString(4, tom);
			ps.setString(5, foy);
			ps.setString(6, fom);

			ps.setString(7, toy);
			ps.setString(8, tom);
			ps.setString(9, tod);
			ps.setString(10, foy);
			ps.setString(11, fom);
			ps.setString(12, fod);

			ResultSet results = ps.executeQuery();
			while (results.next()) {
				return results.getString("revenue");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "<PLACEHOLDER>";
	}

	public Map<String, Map<String, Double>> getCustomDiagramData(Date from,
			Date to) {
		SimpleDateFormat year = new SimpleDateFormat("yyyy");
		SimpleDateFormat month = new SimpleDateFormat("MM");
		SimpleDateFormat day = new SimpleDateFormat("dd");
		String toy = year.format(to);
		String tom = month.format(to);
		String tod = day.format(to);
		String foy = year.format(from);
		String fom = month.format(from);
		String fod = day.format(from);
		//System.out.println(foy + "-" + fom + "-" + fod + "<->" + toy + "-" + tom + "-" + tod);
		Map<String, Map<String, Double>> dataset = new HashMap<String, Map<String, Double>>();
		try {
			PreparedStatement ps = c
					.prepareStatement("Select accountName,accountBalance FROM Accounts");
			ResultSet accounts = ps.executeQuery();
			while (accounts.next()) {
				double startBalance = accounts.getDouble("accountBalance");
				ps = c.prepareStatement("SELECT transactionAmount FROM Transactions WHERE accountName = ? AND transactionYear >= ? AND transactionMonth >= ? AND transactionDay >= ?");
				String accountName = accounts.getString("accountName");
				ps.setString(1, accountName);
				ps.setString(2, foy);
				ps.setString(3, fom);
				ps.setString(4, fod);
				ResultSet transactions = ps.executeQuery();
				while (transactions.next()) {
					startBalance -= transactions.getDouble("transactionAmount");
				}
				String sqlYears = "(SELECT * FROM Transactions WHERE transactionYear <= ? AND transactionYear >= ?)";
				String sqlMonths = "(SELECT * FROM "
						+ sqlYears
						+ " WHERE NOT (transactionYear == ? AND transactionMonth > ? OR transactionYear == ?  AND transactionmonth < ?))";
				String sqlDays = "(SELECT * FROM "
						+ sqlMonths
						+ " WHERE NOT (transactionYear == ? AND transactionMonth == ? AND transactionDay > ? OR transactionYear == ? AND transactionMonth == ? AND transactionDay < ?))";
				ps = c.prepareStatement("Select transactionAmount,transactionYear,transactionMonth,transactionDay FROM "
						+ sqlDays + "WHERE accountName = ?");

				ps.setString(1, toy);
				ps.setString(2, foy);

				ps.setString(3, toy);
				ps.setString(4, tom);
				ps.setString(5, foy);
				ps.setString(6, fom);

				ps.setString(7, toy);
				ps.setString(8, tom);
				ps.setString(9, tod);
				ps.setString(10, foy);
				ps.setString(11, fom);
				ps.setString(12, fod);
				ps.setString(13, accountName);
				transactions = ps.executeQuery();
				Map<String, Double> datapoints = new HashMap<String, Double>();
				while (transactions.next()) {
					String date = transactions.getString("transactionYear")
							+ "-" + transactions.getString("transactionMonth")
							+ "-" + transactions.getString("transactionDay");
					startBalance += transactions.getDouble("transactionAmount");
					datapoints.put(date, startBalance);
				}
				dataset.put(accountName, datapoints);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return dataset;
	}
	
	private PreparedStatment selectBetweenDates(String  sql){
		
	}
	
	public String getAccountBalanceSum(){
		try {
			PreparedStatement ps = c.prepareStatement("SELECT SUM(accountBalance) as balanceSum FROM Accounts");
			ResultSet result = ps.executeQuery();
			while (result.next()){
				return result.getString("balanceSum");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "<PLACEHOLDER>";
	}
}
