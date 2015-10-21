package se.freedrikp.econview.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import se.freedrikp.econview.database.Database;

import com.toedter.calendar.JDateChooser;

public class GUI extends JFrame implements Observer {

	private Database db;
	private JPanel contentPane;
	private JTable accountsTable;
	private JTable transactionsTable;
	

	
	private JTable yearlyRevTable;
	private JTable monthlyRevTable;
	private JLabel totalRevLabel;
	private JLabel customRevLabel;
	private JDateChooser revDateFromField;
	private JDateChooser revDateToField;
	// private JLabel revDateLabel;
	private JScrollPane transactionsPane;
	private JScrollPane accountsPane;
	private JPanel diagramsLastYearPanel;
	private JPanel diagramsLastMonthPanel;
	private JDateChooser diagFromDateField;
	private JDateChooser diagToDateField;
	private JPanel diagramsThisYearPanel;
	private JPanel diagramsThisMonthPanel;
	private JPanel customDiagPanel;
	private JLabel totalBalanceLabel;
	private String dbfile;
	private JTable yearlyAccountRevTable;
	private JTable monthlyAccountRevTable;
	private JTable totalAccountRevTable;
	private JComboBox accountRevBox;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI("econview.db");
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUI(String dbfile) {
		setResizable(false);
		this.dbfile = dbfile;
		this.db = new Database(dbfile);
		db.addObserver(this);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1280, 430);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmOpenDatabase = new JMenuItem("Open Database");
		mntmOpenDatabase.addActionListener(new OpenDatabaseListener(this));
		mnFile.add(mntmOpenDatabase);

		JMenuItem mntmSaveDatabaseAs = new JMenuItem("Save Database as");
		mntmSaveDatabaseAs.addActionListener(new SaveDatabaseListener(this));
		mnFile.add(mntmSaveDatabaseAs);

		JMenu mnImportExport = new JMenu("Import/Export");
		menuBar.add(mnImportExport);

		JMenuItem mntmImport = new JMenuItem("Import");
		mntmImport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(System
						.getProperty("user.dir"));
				int result = fc.showOpenDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					try {
						FileInputStream fis = new FileInputStream(fc
								.getSelectedFile());
						db.importDatabase(fis);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		mnImportExport.add(mntmImport);

		JMenuItem mntmExport = new JMenuItem("Export");
		mntmExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fc = new JFileChooser(System
						.getProperty("user.dir"));
				int result = fc.showSaveDialog(null);
				if (result == JFileChooser.APPROVE_OPTION) {
					try {
						FileOutputStream fos = new FileOutputStream(fc
								.getSelectedFile());
						db.exportDatabase(fos);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		mnImportExport.add(mntmExport);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));

		JTabbedPane tabbedPane = new JTabbedPane();
		contentPane.add(tabbedPane);

		// JPanel startPanel = new JPanel();
		// tabbedPane.addTab("Start", null, startPanel, null);

		AccountsTab accountsPanel = new AccountsTab(db);
		tabbedPane.addTab("Accounts", null, accountsPanel, null);
		

		TransactionsTab transactionsPanel = new TransactionsTab(db);
		tabbedPane.addTab("Transactions", null, transactionsPanel, null);
		

		RevenueTab revenuePanel = new RevenueTab(db);
		tabbedPane.addTab("Revenue", null, revenuePanel, null);
		

		DiagramsTab diagramsPanel = new DiagramsTab(db);
		tabbedPane.addTab("Diagrams", null, diagramsPanel, null);

		
		update(db, null);
	}

	public static double parseAmount(String amount) throws NumberFormatException {
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

	public void update(Observable o, Object arg) {
		// accountsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		// transactionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		// pack();
		repaint();
	}

	
	

	

	// private String askUser(String title, String question, String
	// initialValue,
	// Object[] selectionValues) {
	// String result = null;
	// boolean first = true;
	// do {
	// if (!first) {
	// result = (String) JOptionPane.showInputDialog(this,
	// "Missing input for \"" + title + "\" try again!\n"
	// + question, title,
	// JOptionPane.QUESTION_MESSAGE, null, selectionValues,
	// initialValue);
	//
	// } else {
	// first = false;
	// result = (String) JOptionPane.showInputDialog(this, question,
	// title, JOptionPane.QUESTION_MESSAGE, null,
	// selectionValues, initialValue);
	// }
	// if (result == null) {
	// break;
	// }
	// } while (result.isEmpty());
	// return result;
	// }

	

	

	public static class Model extends DefaultTableModel {

		public Model(String[] header, int rows) {
			super(header, rows);
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}

	}

	private class OpenDatabaseListener implements ActionListener {
		private GUI gui;

		public OpenDatabaseListener(GUI gui) {
			this.gui = gui;
		}

		public void actionPerformed(ActionEvent arg0) {
			JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
			int result = fc.showOpenDialog(gui);
			if (result == JFileChooser.APPROVE_OPTION) {
				db = new Database(fc.getSelectedFile().getAbsolutePath());
				gui.setDatabase(db, fc.getSelectedFile().getAbsolutePath());
			}
		}
	}

	private class SaveDatabaseListener implements ActionListener {
		private GUI gui;

		public SaveDatabaseListener(GUI gui) {
			this.gui = gui;
		}

		public void actionPerformed(ActionEvent arg0) {
			JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
			int result = fc.showSaveDialog(gui);
			if (result == JFileChooser.APPROVE_OPTION) {
				File toFile = fc.getSelectedFile();
				File fromFile = new File(dbfile);
				try {
					FileInputStream fis = new FileInputStream(fromFile);
					FileOutputStream fos = new FileOutputStream(toFile);
					ProgressMonitor pm = new ProgressMonitor(gui, null,
							"Copying Database...", 0, (int) fromFile.length());
					pm.setMillisToPopup(0);
					pm.setMillisToDecideToPopup(0);
					byte[] buffer = new byte[1024];
					int bytesRead = 0;
					long totalRead = 0;
					while ((bytesRead = fis.read(buffer)) > -1) {
						totalRead += bytesRead;
						fos.write(buffer, 0, bytesRead);
						pm.setProgress((int) totalRead);
					}
					fos.flush();
					fos.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public String getDBFile() {
		return dbfile;
	}

	public void setDatabase(Database db, String dbfile) {
		this.dbfile = dbfile;
		this.db = db;
		db.addObserver(this);
		update(db, null);
	}
}
