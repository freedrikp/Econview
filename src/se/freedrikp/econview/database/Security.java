package se.freedrikp.econview.database;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Observable;

public interface Security {

	public abstract Database openDatabase(String database,
			String username, String password) throws Exception;

	public abstract void update(Observable o, Object arg);

	public abstract boolean openFile(File selectedFile, Database db,
			String username, String password);

	public abstract boolean saveFile(File toFile, String username,
			String password);

	public abstract File getFile();

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

}