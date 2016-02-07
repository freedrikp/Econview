package se.freedrikp.econview.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

public class AutoPreparedStatement {
	private PreparedStatement ps;
	private LinkedList<Integer> indices;
	
	private AutoPreparedStatement(PreparedStatement ps) throws SQLException{
		this.ps = ps;
		indices = new LinkedList<Integer>();
		for (int i = 1; i <= ps.getParameterMetaData().getParameterCount(); i++){
			indices.addLast(i);
		}
	}

	public static AutoPreparedStatement create(Connection c, String sql) throws SQLException {
		return new AutoPreparedStatement(c.prepareStatement(sql));
	}

	public void executeUpdate() throws SQLException {
		ps.executeUpdate();		
	}

	public void setString(String s) throws SQLException {
		ps.setString(indices.removeFirst(), s);
	}

	public void setDouble(double d) throws SQLException {
		ps.setDouble(indices.removeFirst(), d);
	}

	public void setInt(int i) throws SQLException {
		ps.setInt(indices.removeFirst(), i);
	}
	
	public void setPlacedInt(int index,int i) throws SQLException{
		indices.remove(indices.indexOf(index));
		ps.setInt(index,i);
	}

	public void setLong(long l) throws SQLException {
		ps.setLong(indices.removeFirst(), l);		
	}

	public ResultSet executeQuery() throws SQLException {
		return ps.executeQuery();
	}

	public void setPlacedString(int index, String s) throws SQLException {
		indices.remove(indices.indexOf(index));
		ps.setString(index, s);
	}
}
