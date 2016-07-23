package se.freedrikp.econview.database;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Observable;

public abstract class Security extends Observable {

	public abstract Database openNewDatabase(String database, String username,
			String password) throws Exception;

	public abstract boolean openDatabase(String selectedDatabase, Database db,
			String username, String password);

	public abstract boolean saveDatabase(String destinationDatabase,
			String username, String password);

	public abstract String getDatabase();

	public abstract boolean addUser(String username, String password,
			boolean admin);

	public abstract boolean changePassword(String username, String oldPass,
			String newPass, List<File> files);

	public abstract String getUser();

	public abstract List<Object[]> listUsers();

	public abstract boolean removeUser(String username);

	public abstract boolean isAdmin();

	public abstract void setAdmin(String username);

	public abstract void changePasswordAdmin(String username, String password);

	public abstract void close() throws SQLException;

	public abstract boolean usersExist();
	
	public abstract void update(Observable o, Object arg);

}