package se.freedrikp.econview.database;

import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Observable;

public abstract class Database extends Observable {

	public abstract void addAccount(String accountName, double accountBalance,
			boolean accountHidden);

	public abstract void editAccount(String oldAccountName, String accountName,
			double accountBalance, boolean accountHidden, Date until);

	public abstract void removeAccount(String accountName);

	public abstract void addTransaction(String accountName,
			double transactionAmount, Date transactionDate,
			String transactionComment);

	public abstract void editTransaction(long transactionID,
			String accountName, double transactionAmount, Date transactionDate,
			String transactionComment);

	public abstract void removeTransaction(long transactionID);

	public abstract List<Object[]> getAccounts(Date until);

	public abstract List<Object[]> getTransactions(Date fromDate, Date toDate,
			Collection<String> accounts);

	public abstract List<String> getAccountNames();

	public abstract List<Object[]> getMonthlyRevenues(Date until);

	public abstract List<Object[]> getYearlyRevenues(Date until);

	public abstract List<Object[]> getMonthlyAccountRevenues(Date until);

	public abstract List<Object[]> getYearlyAccountRevenues(Date until);

	public abstract double getTotalRevenue(Date until);

	public abstract List<Object[]> getTotalAccountRevenues(Date until);

	public abstract double getRevenue(Date from, Date to,
			Collection<String> accounts);

	public abstract Map<String, Map<Date, Double>> getCustomDiagramData(
			Date from, Date to, Collection<String> accounts,
			boolean includeTotal, String totalAccountName);

	public abstract double getTotalAccountBalanceSum(Date until);

	public abstract double getVisibleAccountBalanceSum(Date until);

	public abstract double getHiddenAccountBalanceSum(Date until);

	public abstract void exportDatabase(OutputStream out, String exportMessage);

	public abstract void importDatabase(InputStream in, String importMessage);

	public abstract void openDatabase(String database, String dbUsername,
			String dbPassword, String username);

	public abstract String getDatabase();

	public abstract void setShowHidden(boolean showHidden);

	public abstract boolean getShowHidden();

	public abstract long getNumberOfTransactions();

	public abstract long getNumberOfDeposits();

	public abstract long getNumberOfWithdrawals();

	public abstract Date getOldestTransactionDate();

	public abstract Date getNewestTransactionDate();

	public abstract List<Object[]> getMultiTransactions(Date transactionDate,
			String transactionComment);

	public abstract void close() throws SQLException;

	public abstract void deleteAccounts();

	public abstract void deleteTransactions();

	public abstract void deleteStoredTransactions();

	public abstract void addStoredTransaction(String accountName,
			double transactionAmount, String transactionComment);

	public abstract void editStoredTransaction(long transactionID,
			String accountName, double transactionAmount,
			String transactionComment);

	public abstract void removeStoredTransaction(long transactionID);

	public abstract List<String> getStoredTransactionNames();

	public abstract List<Object[]> getStoredTransaction(
			String transactionComment);

	public abstract List<Object[]> getStoredTransactions();

	public abstract double getAccountBalance(String accountName, Date until);

	public abstract List<Object[]> searchTransactions(long transactionId,
			boolean doID, String accountName, double transactionAmount,
			boolean doAmount, String transactionComment, Date fromDate,
			Date toDate);

}