package se.freedrikp.econview.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Language {
	private static HashMap<String, HashMap<String, String>> lang = null;

	private static void loadDefaultLanguage() {
		HashMap<String, String> lan = new HashMap<String, String>();

		lan.put("USER_DETAILS_PROMPT", "User Access Credentials");
		lan.put("PROMPT_USERNAME", "Username");
		lan.put("PROMPT_PASSWORD", "Password");
		lan.put("PROMPT_ACCESS_DENIED", "Wrong credentials, access denied.");
		lan.put("TOTAL_ACCOUNT_NAME", "Total");
		lan.put("EXPORTING_DATABASE", "Exporting Database");
		lan.put("IMPORTING_DATABASE", "Importing Database");
		lan.put("ACCOUNTS_TAB_NAME", "Accounts");
		lan.put("TRANSACTIONS_TAB_NAME", "Transactions");
		lan.put("REVENUES_TAB_NAME", "Revenue");
		lan.put("DIAGRAMS_TAB_NAME", "Diagrams");
		lan.put("ACCOUNT_HEADER_ACCOUNT", "Account");
		lan.put("ACCOUNT_HEADER_BALANCE", "Balance");
		lan.put("ACCOUNT_HEADER_HIDDEN", "Hidden");
		lan.put("ADD_ACCOUNT", "Add Account");
		lan.put("EDIT_ACCOUNT", "Edit Account");
		lan.put("REMOVE_ACCOUNT", "Remove Account");
		lan.put("REMOVE_ACCOUNT_PROMPT",
				"Are you sure you want to remove this account?");
		lan.put("TOTAL_VISIBLE_BALANCE", "Total Visible Balance");
		lan.put("TOTAL_BALANCE", "Total Balance");
		lan.put("TOTAL_HIDDEN_BALANCE", "Total Hidden Balance");
		lan.put("ADD_ACCOUNT_NAME", "Name");
		lan.put("ADD_ACCOUNT_BALANCE", "Balance");
		lan.put("ADD_ACCOUNT_HIDDEN", "Hide Account?");
		lan.put("ADD_ACCOUNT_CHAIN", "Do you wish to add a new account?");
		lan.put("ACCOUNT_DETAILS", "Account Details");
		lan.put("TRANSACTION_HEADER_ID", "ID");
		lan.put("TRANSACTION_HEADER_ACCOUNT", "Account");
		lan.put("TRANSACTION_HEADER_AMOUNT", "Amount");
		lan.put("TRANSACTION_HEADER_DATE", "Date");
		lan.put("TRANSACTION_HEADER_COMMENT", "Comment");
		lan.put("UPDATE_TRANSACTIONS_VIEW", "Update View");
		lan.put("ADD_TRANSACTION", "Add Transaction");
		lan.put("EDIT_TRANSACTION", "Edit Transaction");
		lan.put("REMOVE_TRANSACTION", "Remove Transaction");
		lan.put("REMOVE_TRANSACTION_PROMPT",
				"Are you sure you want to remove this transaction?");
		lan.put("ADD_TRANSACTION_ACCOUNT", "Account");
		lan.put("ADD_TRANSACTION_AMOUNT", "Amount");
		lan.put("ADD_TRANSACTION_DATE", "Date");
		lan.put("ADD_TRANSACTION_COMMENT", "Comment");
		lan.put("ADD_TRANSACTION_CHAIN",
				"Do you wish to add a new transaction?");
		lan.put("TRANSACTION_DETAILS", "Transaction Details");
		lan.put("OLDEST_TRANSACTION_DATE", "Oldest Transaction Date");
		lan.put("NEWEST_TRANSACTION_DATE", "Newest Transaction Date");
		lan.put("NUMBER_OF_TRANSACTIONS", "Number Of Transactions");
		lan.put("NUMBER_OF_DEPOSITS", "Number Of Deposits");
		lan.put("NUMBER_OF_WITHDRAWALS", "Number Of Withdrawals");
		lan.put("REVENUE_HEADER_YEAR", "Year");
		lan.put("REVENUE_HEADER_MONTH", "Month");
		lan.put("REVENUE_HEADER_ACCOUNT", "Account");
		lan.put("REVENUE_HEADER_REVENUE", "Revenue");
		lan.put("TOTAL_REVENUE", "Total Revenue");
		lan.put("CUSTOM_REVENUE", "Custom Revenue");
		lan.put("ALL_ACCOUNTS", "All Accounts");
		lan.put("CUSTOM_DIAGRAM", "Custom Diagram");
		lan.put("LAST_YEAR", "Last Year");
		lan.put("LAST_MONTH", "Last Month");
		lan.put("DIAGRAM_DATE", "Date");
		lan.put("DIAGRAM_BALANCE", "Balance");
		lan.put("MENUBAR_FILE", "File");
		lan.put("MENUBAR_FILE_OPEN_DATABASE", "Open Database");
		lan.put("MENUBAR_FILE_SAVE_DATABASE_AS", "Save Database As");
		lan.put("MENUBAR_IMPORT_EXPORT", "Import/Export");
		lan.put("MENUBAR_IMPORT_EXPORT_IMPORT", "Import");
		lan.put("MENUBAR_IMPORT_EXPORT_EXPORT", "Export");
		lan.put("MENUBAR_HIDDEN", "Hidden");
		lan.put("MENUBAR_HIDDEN_SHOW_HIDDEN", "Show Hidden");
		lan.put("COPYING_DATABASE", "Copying Database...");
		lan.put("MENUBAR_SETTINGS", "Settings");
		lan.put("MENUBAR_SETTINGS_CONFIGURATION", "Configuration");
		lan.put("SETTINGS_CONFIGURATION", "Configuration Settings");
		lan.put("MENUBAR_SETTINGS_LANGUAGE", "Language");
		lan.put("SETTINGS_LANGUAGE", "Language Settings");
		lan.put("MENUBAR_USERS", "Users");
		lan.put("MENUBAR_ADD_USER", "Add User");
		lan.put("MENUBAR_CHANGE_PASSWORD", "Change Password");
		lan.put("MENUBAR_MANAGE_USERS", "Manage Users");
		lan.put("PASSWORDS_NOT_MATCH", "Passwords do not match.");
		lan.put("PASSWORD_ERROR", "Password Error");
		lan.put("USER_EXISTS", "User already exists, could not add user.");
		lan.put("USER_ERROR", "User Error");
		lan.put("PROMPT_OLD_PASSWORD", "Old Password");
		lan.put("PROMPT_NEW_PASSWORD", "New Password");
		lan.put("BUTTON_SET_ADMIN", "Set Admin");
		lan.put("REMOVE_USER", "Remove User");
		lan.put("REMOVE_USER_PROMPT",
				"Are you sure you want to remove this user?");
		lan.put("MENUBAR_DELETE", "Delete");
		lan.put("MENUBAR_DELETE_ACCOUNTS", "Delete All Accounts");
		lan.put("MENUBAR_DELETE_TRANSACTIONS", "Delete All Transactions");
		lan.put("PROMPT_DELETE_ACCOUNTS",
				"Are you sure you want to delete all accounts?");
		lan.put("PROMPT_DELETE_TRANSACTIONS",
				"Are you sure you want to delete all transactions?");
		lan.put("USER_HEADER_USERNAME", "Username");
		lan.put("USER_HEADER_ADMIN", "Admin");
		lan.put("CHANGE_PASSWORD_FILES",
				"Select files to reencrypt with the new password.");
		lan.put("MENUBAR_STORED_TRANSACTIONS", "Stored Transactions");
		lan.put("MENUBAR_STORED_TRANSACTIONS_ADD", "Add Stored Transaction");
		lan.put("MENUBAR_STORED_TRANSACTIONS_EDIT", "Edit Stored Transaction");
		lan.put("MENUBAR_STORED_TRANSACTIONS_REMOVE",
				"Remove Stored Transaction");
		lan.put("MENUBAR_DELETE_STORED_TRANSACTIONS",
				"Delete All Stored Transactions");
		lan.put("PROMPT_DELETE_STORED_TRANSACTIONS",
				"Are you sure you want to delete all stored transactions?");

		lang.put(Configuration.getString("LANGUAGE"), lan);
	}

	public static String getString(String key) {
		if (lang == null) {
			lang = new HashMap<String, HashMap<String, String>>();
			loadDefaultLanguage();
			writeLanguages();
			parseLanguages();
			writeDefaultLanguage();
		}
		String string = lang.get(Configuration.getString("LANGUAGE")).get(key);
		return string != null ? string : "<unknown>";
	}

	private static void writeDefaultLanguage() {
		File langFile = new File(Configuration.getString("LANGUAGE"));
		PrintWriter fw;
		try {
			fw = new PrintWriter(langFile, "UTF-8");

			for (Map.Entry<String, String> e : lang.get(
					Configuration.getString("LANGUAGE")).entrySet()) {

				fw.println(e.getKey() + "=" + e.getValue());
			}
			fw.flush();
			fw.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	private static void parseLanguages() {
		File dir = new File(".");
		File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".lang");
			}
		});
		for (File f : files) {
			HashMap<String, String> language = lang.containsKey(f.getName()) ? lang
					.get(f.getName()) : new HashMap<String, String>();
			Scanner scan;
			try {
				scan = new Scanner(f, "UTF-8");

				while (scan.hasNextLine()) {
					String line = scan.nextLine().trim();
					String[] lan = line.split("=");
					language.put(lan[0].trim(), lan[1].trim());
				}
				lang.put(f.getName(), language);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private static void writeLanguages() {
		for (Map.Entry<String, HashMap<String, String>> entry : lang.entrySet()) {
			File langFile = new File(entry.getKey());
			if ((!langFile.exists() || langFile.length() == 0)) {
				PrintWriter fw;
				try {
					fw = new PrintWriter(langFile, "UTF-8");
					for (Map.Entry<String, String> e : entry.getValue()
							.entrySet()) {

						fw.println(e.getKey() + "=" + e.getValue());
					}
					fw.flush();
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static Map<String, String> listAllStrings() {
		getString("UNKNOWN");
		return lang.get(Configuration.getString("LANGUAGE"));
	}

	public static void putString(String key, String string) {
		lang.get(Configuration.getString("LANGUAGE")).put(key, string);
		writeDefaultLanguage();
	}
}
