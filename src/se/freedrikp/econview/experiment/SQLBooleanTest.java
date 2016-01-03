package se.freedrikp.econview.experiment;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLBooleanTest {
	private Connection c;

	public SQLBooleanTest(String dbfile) {
		try {
			File db = new File(dbfile);
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:" + dbfile);
			if (!db.exists() || db.length() == 0) {
				initdb();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void initdb() {
		try {
			String sql = "CREATE TABLE Accounts(" + "accountName TEXT,"
					+ "accountBalance REAL ," + "accountHidden INTEGER)";
			c.prepareStatement(sql).executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void insert() {
		try {
			String sql = "INSERT INTO Accounts VALUES (?,?,?)";
			PreparedStatement ps = c.prepareStatement(sql);
			ps.setString(1, "Test");
			ps.setDouble(2, 513.92);
			ps.setBoolean(3, true);
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SQLBooleanTest test = new SQLBooleanTest("boolean_test.db");
		test.insert();

	}

}
