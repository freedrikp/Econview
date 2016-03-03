package se.freedrikp.econview.gui.frames;

import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import se.freedrikp.econview.common.Configuration;
import se.freedrikp.econview.common.Language;
import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.database.Security;
import se.freedrikp.econview.gui.menubar.MenuBar;
import se.freedrikp.econview.gui.tabs.AccountsTab;
import se.freedrikp.econview.gui.tabs.DiagramsTab;
import se.freedrikp.econview.gui.tabs.RevenueTab;
import se.freedrikp.econview.gui.tabs.TransactionsTab;

public class MainFrame extends JFrame implements Observer {

	private JPanel contentPane;
	// private JLabel revDateLabel;
	private Security sec;
	private Database db;
	private final int WIDTH = Configuration.getInt("WINDOW_WIDTH");
	private final int HEIGHT = Configuration.getInt("WINDOW_HEIGHT");

	/**
	 * Create the frame.
	 */
	public MainFrame(Database db, Security sec) {
		super("EconView");
		// setResizable(false);
		// this.dbfile = dbfile;
		this.sec = sec;
		this.db = db;
		if (sec != null) {
			sec.addObserver(this);
		} else {
			db.addObserver(this);
		}
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new Window(this, db, sec));
		// setBounds(100, 100, 1280, 430);
		// setBounds(100, 100, 1360, 500);
		DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getDefaultScreenDevice().getDisplayMode();
		setBounds((dm.getWidth() - WIDTH) / 2, (dm.getHeight() - HEIGHT) / 2,
				WIDTH, HEIGHT);

		MenuBar menuBar = new MenuBar(db, sec, this);
		setJMenuBar(menuBar);

		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

		JTabbedPane tabbedPane = new JTabbedPane();
		contentPane.add(tabbedPane);

		// JPanel startPanel = new JPanel();
		// tabbedPane.addTab("Start", null, startPanel, null);

		AccountsTab accountsPanel = new AccountsTab(db);
		tabbedPane.addTab(Language.getString("ACCOUNTS_TAB_NAME"), null,
				accountsPanel, null);

		TransactionsTab transactionsPanel = new TransactionsTab(db);
		tabbedPane.addTab(Language.getString("TRANSACTIONS_TAB_NAME"), null,
				transactionsPanel, null);

		RevenueTab revenuePanel = new RevenueTab(db);
		tabbedPane.addTab(Language.getString("REVENUES_TAB_NAME"), null,
				revenuePanel, null);

		DiagramsTab diagramsPanel = new DiagramsTab(db);
		tabbedPane.addTab(Language.getString("DIAGRAMS_TAB_NAME"), null,
				diagramsPanel, null);

		update(db, null);
	}

	public static double parseAmount(String amount) {// throws
														// NumberFormatException
														// {
		if (amount == null || amount.isEmpty()) {
			throw new NumberFormatException();
		}
		double result;
		try {
			result = NumberFormat.getCurrencyInstance().parse(amount)
					.doubleValue();
		} catch (ParseException e) {
			amount = amount.replace(',', '.');
			result = Double.parseDouble(amount);
		}
		return result;
	}

	public static class Model extends DefaultTableModel {

		public Class<?> getColumnClass(int columnIndex) {
			if (getRowCount() == 0) {
				return Object.class;
			}
			return getValueAt(0, columnIndex).getClass();
		}

		public Model(String[] header, int rows) {
			super(header, rows);
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}

	}

	public static void resizeTable(JTable table) {
		for (int column = 0; column < table.getColumnCount(); column++) {
			TableColumn tableColumn = table.getColumnModel().getColumn(column);
			int preferredWidth = tableColumn.getMinWidth();
			int maxWidth = tableColumn.getMaxWidth();

			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer cellRenderer = table.getCellRenderer(row,
						column);
				Component c = table.prepareRenderer(cellRenderer, row, column);
				int width = c.getPreferredSize().width
						+ table.getIntercellSpacing().width;
				preferredWidth = Math.max(preferredWidth, width);

				// We've exceeded the maximum width, no need to check other rows

				if (preferredWidth >= maxWidth) {
					preferredWidth = maxWidth;
					break;
				}
			}

			tableColumn.setPreferredWidth(preferredWidth);
		}
	}

	public void update(Observable o, Object arg) {
		String file;
		if (sec != null) {
			file = sec.getFile().getAbsolutePath();
		} else {
			file = db.getFile().getAbsolutePath();
		}
		setTitle("EconView - " + file);
		// repaint();
	}

	private static class Window extends WindowAdapter {
		private MainFrame gui;
		private Security sec;
		private Database db;

		public Window(MainFrame gui, Database db, Security sec) {
			this.gui = gui;
			this.sec = sec;
			this.db = db;
		}

		public void windowClosing(WindowEvent e) {
			try {
				db.close();
				if (sec != null) {
					sec.close();
					Files.delete(new File(Configuration
							.getString("DATABASE_DIRECTORY")
							+ "/"
							+ Configuration.getString("DATABASE_FILE"))
							.toPath());
				}
			} catch (SQLException | IOException e1) {
				e1.printStackTrace();
			}
			gui.dispose();
			System.exit(0);
		}
	}

}
