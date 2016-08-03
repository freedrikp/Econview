package se.freedrikp.econview.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Configuration {
	private static final File configFile = new File("econview.conf");
	private static HashMap<String, String> config = null;

	private static void loadDefaultConfig() {
		config.put("DATABASE_FILE", "econview.db");
		config.put("LANGUAGE", "english.lang");
		config.put("WINDOW_WIDTH", "1360");
		config.put("WINDOW_HEIGHT", "500");
		config.put("DIAGRAM_WIDTH", "600");
		config.put("DIAGRAM_HEIGHT", "350");
		config.put("CUSTOM_DIAGRAM_WIDTH", "480");
		config.put("CUSTOM_DIAGRAM_HEIGHT", "350");
		config.put("FULL_DATE_FORMAT", "yyyy-MM-dd");
		config.put("MONTH_FORMAT", "MMMM");
		config.put("YEAR_FORMAT", "yyyy");
		config.put("SETTINGS_PANEL_WIDTH", "400");
		config.put("SETTINGS_PANEL_HEIGHT", "600");
		config.put("ADD_TRANSACTION_PANEL_WIDTH", "500");
		config.put("ADD_TRANSACTION_PANEL_HEIGHT", "400");
		config.put("DATE_FIELD_WIDTH", "150");
		config.put("DATE_FIELD_HEIGHT", "15");
		config.put("DIAGRAMS_STYLE_SPLIT_OR_TAB", "TAB");
		config.put("USERS_DATABASE_FILE", "econview_users.db");
		config.put("USER_PANEL_WIDTH", "400");
		config.put("USER_PANEL_HEIGHT", "200");
		config.put("SECURITY_TRUE_FALSE", "TRUE");
		config.put("SEARCH_PANEL_WIDTH", "500");
		config.put("SEARCH_PANEL_HEIGHT", "500");
		config.put("DATABASE_DIRECTORY", "data");
		config.put("MYSQL_DATABASE", "MYSQL_DATABASE");
		config.put("MYSQL_USERNAME", "MYSQL_USERNAME");
		config.put("MYSQL_PASSWORD", "MYSQL_PASSWORD");
		config.put("DATABASE_SYSTEM_SQLITE_OR_MYSQL", "SQLITE");
	}

	public static String getString(String key) {
		if (config == null) {
			config = new HashMap<String, String>();
			loadDefaultConfig();
			if (!configFile.exists() || configFile.length() == 0) {
				writeConfig();
			}
			parseConfig();
			writeConfig();
		}
		return config.get(key);
	}

	public static boolean getBoolean(String key) {
		return Boolean.parseBoolean(getString(key).toLowerCase());
	}

	public static int getInt(String key) {
		return Integer.parseInt(getString(key).toLowerCase());
	}

	public static void putConfig(String key, String conf) {
		config.put(key, conf);
		writeConfig();
	}

	private static void parseConfig() {
		try {
			Scanner scan = new Scanner(configFile, "UTF-8");
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
			PrintWriter fw = new PrintWriter(configFile, "UTF-8");
			for (Map.Entry<String, String> entry : config.entrySet()) {
				fw.println(entry.getKey() + "=" + entry.getValue());
			}
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, String> listAllConfigs() {
		getString("UNKNOWN");
		return config;
	}

}
