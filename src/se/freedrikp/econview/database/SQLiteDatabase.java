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

	protected String helperClause() {
		return "";
	}

	protected String helperValue() {
		return "";
	}
	
	protected String helperAdd() {
		return "";
	}

	protected String helperAddValue() {
		return "";
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
