package se.freedrikp.econview.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Utilities {
	private static HashMap<String, String> config = null;
	private static HashMap<String, HashMap<String, String>> lang = null;
	private static final File configFile = new File("econview.conf");
	private static final String englishFile = "english.lang";

	private static void loadDefaultLanguage() {
		lang = new HashMap<String, HashMap<String, String>>();
		HashMap<String,String> lan = new HashMap<String,String>();
		
		lan.put("TOTAL_ACCOUNT_NAME","Total");
		lan.put("ACCOUNTS_TAB_NAME","Accounts");
		lan.put("TRANSACTIONS_TAB_NAME","Transactions");
		lan.put("REVENUES_TAB_NAME","Revenue");
		lan.put("DIAGRAMS_TAB_NAME","Diagrams");
		lan.put("ACCOUNT_HEADER_ACCOUNT","Account");
		lan.put("ACCOUNT_HEADER_BALANCE", "Balance");
		lan.put("ACCOUNT_HEADER_INCLUDED", "Included");
		lan.put("ADD_ACCOUNT", "Add Account");
		lan.put("EDIT_ACCOUNT", "Edit Account");
		lan.put("REMOVE_ACCOUNT", "Remove Account");
		lan.put("REMOVE_ACCOUNT_PROMPT", "Are you sure you want to remove this account?");
		lan.put("TOTAL_INCLUDED_BALANCE", "Total Included Balance");
		lan.put("TOTAL_BALANCE", "Total Balance");
		lan.put("ADD_ACCOUNT_NAME", "Name");
		lan.put("ADD_ACCOUNT_BALANCE", "Balance");
		lan.put("ADD_ACCOUNT_INCLUDE", "Include in statistics?");
		lan.put("ACCOUNT_DETAILS", "Account Details");
		
		lang.put(getConfig("LANGUAGE"), lan);
	}

	private static void loadDefaultConfig() {
		config = new HashMap<String, String>();
		config.put("DATABASE_FILE", "econview.db");
		config.put("LANGUAGE", englishFile);
	}

	public static String getConfig(String key) {
		if (config == null) {
			loadDefaultConfig();
			if (!configFile.exists() || configFile.length() == 0) {
				writeConfig();
			}
			parseConfig();
		}
		return config.get(key);
	}

	public static void putConfig(String key, String conf) {
		config.put(key, conf);
		writeConfig();
	}

	private static void parseConfig() {
		try {
			Scanner scan = new Scanner(configFile);
			while (scan.hasNextLine()) {
				String line = scan.nextLine().trim();
				String[] conf = line.split("=");
				config.put(conf[0].trim(), conf[1].trim());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void writeConfig() {
		try {
			FileWriter fw = new FileWriter(configFile);
			for (Map.Entry<String, String> entry : config.entrySet()) {
				fw.write(entry.getKey() + "=" + entry.getValue() + "\n");
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getString(String key) {
		if (lang == null) {
			loadDefaultLanguage();
			writeLanguages();
			parseLanguages();
		}
		return lang.get(getConfig("LANGUAGE")).get(key);
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
				scan = new Scanner(f);

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
				FileWriter fw;
				try {
					fw = new FileWriter(langFile);
					for (Map.Entry<String, String> e : entry.getValue()
							.entrySet()) {

						fw.write(e.getKey() + "=" + e.getValue() + "\n");
					}
					fw.flush();
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
