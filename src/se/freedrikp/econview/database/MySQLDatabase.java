package se.freedrikp.econview.database;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.swing.ProgressMonitor;

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

	protected String helperClause() {
		return " AND username = ? ";
	}

	protected String helperValue() {
		return username;
	}

	protected String helperAdd() {
		return ",username";
	}

	protected String helperAddValue() {
		return ",?";
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
					if (i < row.getMetaData().getColumnCount() - 1) {
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
						if (i < row.getMetaData().getColumnCount() - 1) {
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

	public void close() throws SQLException {
		try {
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		c.close();
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
