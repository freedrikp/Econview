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
	private static final String[] accountHeader = { "Account", "Balance", "Included" };
	private static final String[] transactionHeader = { "ID", "Account",
			"Amount", "Date", "Comment" };
	private static final String[] monthlyRevHeader = { "Year", "Month",
			"Revenue" };
	private static final String[] yearlyRevHeader = { "Year", "Revenue" };
	private static final String[] monthlyAccountRevHeader = { "Year", "Month",
			"Account", "Revenue" };
	private static final String[] yearlyAccountRevHeader = { "Year", "Account",
			"Revenue" };
	private static final String[] totalAccountRevHeader = { "Account",
			"Revenue" };
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

		JPanel accountsPanel = new JPanel();
		tabbedPane.addTab("Accounts", null, accountsPanel, null);
		accountsPanel.setLayout(new GridLayout(0, 2, 0, 0));

		accountsPane = new JScrollPane();
		accountsPanel.add(accountsPane);

		accountsTable = new JTable();
		accountsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		accountsPane.setViewportView(accountsTable);

		JPanel accountsButtonPanel = new JPanel();
		accountsPanel.add(accountsButtonPanel);

		JButton btnAddAccount = new JButton("Add Account");
		btnAddAccount.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnAddAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// String accountName = askUser("Account Name",
				// "What is the name of the new account?", null, null);
				// if (accountName == null) {
				// return;
				// }
				// String accountBalance = askUser("Account Balance",
				// "What is the balance of the new account?", null, null);
				// if (accountBalance == null) {
				// return;
				// }
				// db.addAccount(accountName,
				// Double.parseDouble(accountBalance));
				String[] accountDetails = askUserAccount(null, null, Boolean.toString(true));
				if (accountDetails != null) {
					db.addAccount(accountDetails[0],
							parseAmount(accountDetails[1]),
							Boolean.parseBoolean(accountDetails[2]));
				}
			}
		});
		accountsButtonPanel.setLayout(new BoxLayout(accountsButtonPanel,
				BoxLayout.Y_AXIS));
		accountsButtonPanel.add(btnAddAccount);

		JButton btnEditAccount = new JButton("Edit Account");
		btnEditAccount.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnEditAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// String accountName = askUser(
				// "Account Name",
				// "What is the name of the account?",
				// (String) accountsTable.getModel().getValueAt(
				// accountsTable.getSelectedRow(), 0), null);
				// if (accountName == null) {
				// return;
				// }
				// String accountBalance = askUser(
				// "Account Balance",
				// "What is the balance of the account?",
				// (String) accountsTable.getModel().getValueAt(
				// accountsTable.getSelectedRow(), 1), null);
				// if (accountBalance == null) {
				// return;
				// }
				// db.editAccount(
				// (String) accountsTable.getModel().getValueAt(
				// accountsTable.getSelectedRow(), 0),
				// accountName, Double.parseDouble(accountBalance));
				String[] accountDetails = askUserAccount(
						(String) accountsTable.getModel().getValueAt(
								accountsTable.getSelectedRow(), 0),
						(String) accountsTable.getModel().getValueAt(
								accountsTable.getSelectedRow(), 1),
						(String) accountsTable.getModel().getValueAt(
								accountsTable.getSelectedRow(), 2));
				if (accountDetails != null) {
					db.editAccount((String) accountsTable.getModel()
							.getValueAt(accountsTable.getSelectedRow(), 0),
							accountDetails[0], parseAmount(accountDetails[1]),
							Boolean.parseBoolean(accountDetails[2]));
				}
			}
		});
		accountsButtonPanel.add(btnEditAccount);

		JButton btnRemoveAccount = new JButton("Remove Account");
		btnRemoveAccount.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnRemoveAccount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				db.removeAccount((String) accountsTable.getModel().getValueAt(
						accountsTable.getSelectedRow(), 0));
			}
		});
		accountsButtonPanel.add(btnRemoveAccount);

		JLabel totalBalanceLabelText = new JLabel("Total Balance:");
		accountsButtonPanel.add(totalBalanceLabelText);
		totalBalanceLabelText.setAlignmentX(Component.CENTER_ALIGNMENT);

		totalBalanceLabel = new JLabel("");
		totalBalanceLabel.setForeground(Color.RED);
		accountsButtonPanel.add(totalBalanceLabel);
		totalBalanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel transactionsPanel = new JPanel();
		tabbedPane.addTab("Transactions", null, transactionsPanel, null);
		transactionsPanel.setLayout(new GridLayout(0, 2, 0, 0));

		transactionsPane = new JScrollPane();
		transactionsPanel.add(transactionsPane);

		transactionsTable = new JTable();
		transactionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		transactionsPane.setViewportView(transactionsTable);

		JPanel transactionsButtonPanel = new JPanel();
		transactionsPanel.add(transactionsButtonPanel);

		JButton btnAddTransaction = new JButton("Add Transaction");
		btnAddTransaction.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnAddTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// String accountName = askUser("Account Name",
				// "What is the name of the account?", null, db
				// .getAccountNames().toArray());
				// if (accountName == null) {
				// return;
				// }
				// String transactionAmount = askUser("Transaction Amount",
				// "What is the amount of the transaction?", null, null);
				// if (transactionAmount == null) {
				// return;
				// }
				// String transactionDate = askUser("Transaction Date",
				// "What is the date of the transaction?", null, null);
				// if (transactionDate == null) {
				// return;
				// }
				// String transactionComment = askUser("Transaction Comment",
				// "What is the comment of the tansaction?", null, null);
				// if (transactionComment == null) {
				// return;
				// }
				// try {
				// db.addTransaction(accountName, Double
				// .parseDouble(transactionAmount),
				// new SimpleDateFormat("yyyy-mm-dd")
				// .parse(transactionDate), transactionComment);
				// } catch (NumberFormatException e1) {
				// e1.printStackTrace();
				// } catch (ParseException e1) {
				// e1.printStackTrace();
				// }

				String[] transactionDetails = askUserTransaction(db
						.getAccountNames().toArray(), null, null,
						new SimpleDateFormat("yyyy-MM-dd").format(new Date()),
						null);

				if (transactionDetails != null) {
					try {
						db.addTransaction(transactionDetails[0],
								parseAmount(transactionDetails[1]),
								new SimpleDateFormat("yyyy-MM-dd")
										.parse(transactionDetails[2]),
								transactionDetails[3]);
					} catch (NumberFormatException | ParseException e1) {
						e1.printStackTrace();
					}
				}

			}

		});
		transactionsButtonPanel.setLayout(new BoxLayout(
				transactionsButtonPanel, BoxLayout.Y_AXIS));
		transactionsButtonPanel.add(btnAddTransaction);

		JButton btnEditTransaction = new JButton("Edit Transaction");
		btnEditTransaction.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnEditTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// String accountName = askUser(
				// "Account Name",
				// "What is the name of the account?",
				// (String) transactionsTable.getModel().getValueAt(
				// transactionsTable.getSelectedRow(), 1),
				// db.getAccountNames().toArray());
				// if (accountName == null) {
				// return;
				// }
				// String transactionAmount = askUser(
				// "Transaction Amount",
				// "What is the amount of the transaction?",
				// (String) transactionsTable.getModel().getValueAt(
				// transactionsTable.getSelectedRow(), 2), null);
				// if (transactionAmount == null) {
				// return;
				// }
				// String transactionDate = askUser(
				// "Transaction Date",
				// "What is the date of the transaction?",
				// (String) transactionsTable.getModel().getValueAt(
				// transactionsTable.getSelectedRow(), 3), null);
				// if (transactionDate == null) {
				// return;
				// }
				// String transactionComment = askUser(
				// "Transaction Comment",
				// "What is the comment of the tansaction?",
				// (String) transactionsTable.getModel().getValueAt(
				// transactionsTable.getSelectedRow(), 4), null);
				// if (transactionComment == null) {
				// return;
				// }
				// try {
				// db.editTransaction(Long
				// .parseLong((String) transactionsTable.getModel()
				// .getValueAt(transactionsTable.getSelectedRow(),
				// 0)), accountName, Double
				// .parseDouble(transactionAmount),
				// new SimpleDateFormat("yyyy-mm-dd")
				// .parse(transactionDate), transactionComment);
				// } catch (NumberFormatException e1) {
				// e1.printStackTrace();
				// } catch (ParseException e1) {
				// e1.printStackTrace();
				// }
				String[] transactionDetails = askUserTransaction(
						db.getAccountNames().toArray(),
						(String) transactionsTable.getModel().getValueAt(
								transactionsTable.getSelectedRow(), 1),
						(String) transactionsTable.getModel().getValueAt(
								transactionsTable.getSelectedRow(), 2),
						(String) transactionsTable.getModel().getValueAt(
								transactionsTable.getSelectedRow(), 3),
						(String) transactionsTable.getModel().getValueAt(
								transactionsTable.getSelectedRow(), 4));
				if (transactionDetails != null) {
					try {
						db.editTransaction(Long
								.parseLong((String) transactionsTable
										.getModel().getValueAt(
												transactionsTable
														.getSelectedRow(), 0)),
								transactionDetails[0],
								parseAmount(transactionDetails[1]),
								new SimpleDateFormat("yyyy-MM-dd")
										.parse(transactionDetails[2]),
								transactionDetails[3]);
					} catch (NumberFormatException | ParseException e1) {
						e1.printStackTrace();
					}
				}

			}
		});
		transactionsButtonPanel.add(btnEditTransaction);

		JButton btnRemoveTransaction = new JButton("Remove Transaction");
		btnRemoveTransaction.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnRemoveTransaction.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				db.removeTransaction(Long.parseLong((String) transactionsTable
						.getModel().getValueAt(
								transactionsTable.getSelectedRow(), 0)));
			}
		});
		transactionsButtonPanel.add(btnRemoveTransaction);

		JPanel revenuePanel = new JPanel();
		tabbedPane.addTab("Revenue", null, revenuePanel, null);
		revenuePanel.setLayout(new GridLayout(1, 0, 0, 0));

		JScrollPane yearlyRevPane = new JScrollPane();
		revenuePanel.add(yearlyRevPane);

		yearlyRevTable = new JTable();
		yearlyRevTable.setEnabled(false);
		yearlyRevPane.setViewportView(yearlyRevTable);

		JScrollPane monthlyRevPane = new JScrollPane();
		revenuePanel.add(monthlyRevPane);

		monthlyRevTable = new JTable();
		monthlyRevTable.setEnabled(false);
		monthlyRevPane.setViewportView(monthlyRevTable);

		JScrollPane yearlyAccountRevPane = new JScrollPane();
		revenuePanel.add(yearlyAccountRevPane);

		yearlyAccountRevTable = new JTable();
		yearlyAccountRevTable.setEnabled(false);
		yearlyAccountRevPane.setViewportView(yearlyAccountRevTable);

		JScrollPane monthlyAccountRevPane = new JScrollPane();
		revenuePanel.add(monthlyAccountRevPane);

		monthlyAccountRevTable = new JTable();
		monthlyAccountRevTable.setEnabled(false);
		monthlyAccountRevPane.setViewportView(monthlyAccountRevTable);

		JScrollPane totalAccountRevPane = new JScrollPane();
		revenuePanel.add(totalAccountRevPane);

		totalAccountRevTable = new JTable();
		totalAccountRevTable.setEnabled(false);
		totalAccountRevPane.setViewportView(totalAccountRevTable);

		JPanel sideRevenuePanel = new JPanel();
		revenuePanel.add(sideRevenuePanel);
		sideRevenuePanel.setLayout(new BoxLayout(sideRevenuePanel,
				BoxLayout.Y_AXIS));

		JLabel lblTotalRevenue = new JLabel("Total Revenue:");
		lblTotalRevenue.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideRevenuePanel.add(lblTotalRevenue);

		totalRevLabel = new JLabel("");
		totalRevLabel.setForeground(Color.RED);
		totalRevLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideRevenuePanel.add(totalRevLabel);

		JSeparator totCustomRevSep = new JSeparator();
		sideRevenuePanel.add(totCustomRevSep);

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		JLabel lblCustomRevenue = new JLabel("Custom Revenue:");
		lblCustomRevenue.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideRevenuePanel.add(lblCustomRevenue);

		// revDateLabel = new JLabel("");
		// revDateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		// sideRevenuePanel.add(revDateLabel);

		customRevLabel = new JLabel("");
		customRevLabel.setForeground(Color.RED);
		customRevLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideRevenuePanel.add(customRevLabel);

		JSeparator customRevSep = new JSeparator();
		sideRevenuePanel.add(customRevSep);

		// revDateFromField = new JTextField(df.format(new Date()));
		revDateFromField = new JDateChooser(new Date(), "yyyy-MM-dd");
		sideRevenuePanel.add(revDateFromField);
		// revDateFromField.setColumns(7);

		JLabel revDateSepLabel = new JLabel("<->");
		revDateSepLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideRevenuePanel.add(revDateSepLabel);

		// revDateToField = new JTextField(df.format(new Date()));
		revDateToField = new JDateChooser(new Date(), "yyyy-MM-dd");
		sideRevenuePanel.add(revDateToField);
		// revDateToField.setColumns(7);

		JButton customRevButton = new JButton("Custom Revenue");
		customRevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update(db, null);
			}
		});

		accountRevBox = new JComboBox();
		sideRevenuePanel.add(accountRevBox);
		customRevButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideRevenuePanel.add(customRevButton);

		Component verticalStrut = Box.createVerticalStrut(2000);
		sideRevenuePanel.add(verticalStrut);

		JPanel diagramsPanel = new JPanel();
		tabbedPane.addTab("Diagrams", null, diagramsPanel, null);

		diagramsLastYearPanel = new JPanel();
		diagramsLastMonthPanel = new JPanel();
		diagramsThisYearPanel = new JPanel();
		diagramsThisMonthPanel = new JPanel();
		JSplitPane diagramYearSplitPane = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT, diagramsLastYearPanel,
				diagramsThisYearPanel);
		JSplitPane diagramMonthSplitPane = new JSplitPane(
				JSplitPane.VERTICAL_SPLIT, diagramsLastMonthPanel,
				diagramsThisMonthPanel);
		diagramsThisMonthPanel.setLayout(new BoxLayout(diagramsThisMonthPanel,
				BoxLayout.X_AXIS));
		diagramsLastMonthPanel.setLayout(new BoxLayout(diagramsLastMonthPanel,
				BoxLayout.X_AXIS));
		JSplitPane diagramSplitPane = new JSplitPane(
				JSplitPane.HORIZONTAL_SPLIT, diagramYearSplitPane,
				diagramMonthSplitPane);
		diagramsPanel.add(diagramSplitPane);

		JPanel diagramControlPanel = new JPanel();
		diagramsPanel.add(diagramControlPanel);
		diagramControlPanel.setLayout(new BoxLayout(diagramControlPanel,
				BoxLayout.Y_AXIS));

		// diagFromDateField = new JTextField(df.format(new Date()));
		diagFromDateField = new JDateChooser(new Date(), "yyyy-MM-dd");
		diagramControlPanel.add(diagFromDateField);
		// diagFromDateField.setColumns(10);

		JLabel diagDateSepLabel = new JLabel("<->");
		diagDateSepLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		diagramControlPanel.add(diagDateSepLabel);

		// diagToDateField = new JTextField(df.format(new Date()));
		diagToDateField = new JDateChooser(new Date(), "yyyy-MM-dd");
		diagramControlPanel.add(diagToDateField);
		// diagToDateField.setColumns(10);

		JButton btnCustomDiagram = new JButton("Custom Diagram");
		btnCustomDiagram.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update(db, null);
			}
		});
		btnCustomDiagram.setAlignmentX(Component.CENTER_ALIGNMENT);
		diagramControlPanel.add(btnCustomDiagram);

		customDiagPanel = new JPanel();
		diagramsPanel.add(customDiagPanel);

		update(db, null);
	}

	private double parseAmount(String amount) throws NumberFormatException {
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
		updateAccountList();
		totalBalanceLabel.setText((NumberFormat.getCurrencyInstance()
				.format(Double.parseDouble(db.getAccountBalanceSum()))));
		updateTransactionList();
		accountsPane.getVerticalScrollBar().setValue(
				accountsPane.getVerticalScrollBar().getMaximum());
		transactionsPane.getVerticalScrollBar().setValue(
				transactionsPane.getVerticalScrollBar().getMaximum());
		// accountsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		// transactionsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		updateYearlyRevList();
		updateMonthlyRevList();
		updateYearlyAccountRevList();
		updateMonthlyAccountRevList();
		updateTotalRevLabel();
		updateTotalAccountRevList();
		updateCustomRevLabel();
		updateDiagrams();
		// pack();
		repaint();
	}

	private void updateDiagrams() {
		diagramsLastYearPanel.removeAll();
		diagramsLastMonthPanel.removeAll();
		diagramsThisMonthPanel.removeAll();
		diagramsThisYearPanel.removeAll();
		customDiagPanel.removeAll();
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		start.set(Calendar.MONTH, start.getActualMinimum(Calendar.MONTH));
		start.set(Calendar.DATE, start.getActualMinimum(Calendar.DATE));
		end.set(Calendar.MONTH, start.getActualMaximum(Calendar.MONTH));
		end.set(Calendar.DATE, start.getActualMaximum(Calendar.DATE));
		generateDiagram(start.getTime(), end.getTime(),
				Integer.toString(Calendar.getInstance().get(Calendar.YEAR)),
				diagramsThisYearPanel, 300, 150);
		start = Calendar.getInstance();
		end = Calendar.getInstance();
		start.set(Calendar.DATE, start.getActualMinimum(Calendar.DATE));
		end.set(Calendar.DATE, start.getActualMaximum(Calendar.DATE));
		generateDiagram(
				start.getTime(),
				end.getTime(),
				Calendar.getInstance().getDisplayName(Calendar.MONTH,
						Calendar.LONG, Locale.getDefault()),
				diagramsThisMonthPanel, 300, 150);
		start = Calendar.getInstance();
		end = Calendar.getInstance();
		start.add(Calendar.YEAR, -1);
		generateDiagram(start.getTime(), end.getTime(), "Last Year",
				diagramsLastYearPanel, 300, 150);
		start = Calendar.getInstance();
		end = Calendar.getInstance();
		start.add(Calendar.MONTH, -1);
		generateDiagram(start.getTime(), end.getTime(), "Last Month",
				diagramsLastMonthPanel, 300, 150);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		// try {
		// generateDiagram(
		// df.parse(diagFromDateField.getText()),
		// df.parse(diagToDateField.getText()),"Custom Diagram",customDiagPanel,400,300);
		generateDiagram(diagFromDateField.getDate(), diagToDateField.getDate(),
				"Custom Diagram", customDiagPanel, 400, 300);
		// } catch (ParseException e) {
		// e.printStackTrace();
		// }
	}

	private void generateDiagram(Date from, Date to, String title,
			JPanel panel, int width, int height) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Map<String, Map<String, Double>> diagramData = db.getCustomDiagramData(
				from, to);
		TimeSeriesCollection collection = new TimeSeriesCollection();
		for (Map.Entry<String, Map<String, Double>> dataset : diagramData
				.entrySet()) {
			double previousAmount = 0;
			Date previousDate = from;
			TimeSeries series = new TimeSeries(dataset.getKey());
			for (Map.Entry<String, Double> datapoint : dataset.getValue()
					.entrySet()) {
				try {

					Calendar start = Calendar.getInstance();
					start.setTime(previousDate);
					start.add(Calendar.DATE, 1);
					Calendar end = Calendar.getInstance();
					end.setTime(df.parse(datapoint.getKey()));

					for (Date date = start.getTime(); start.before(end); start
							.add(Calendar.DATE, 1), date = start.getTime()) {
						series.add(new Day(date), previousAmount);
					}
					series.add(new Day(df.parse(datapoint.getKey())),
							datapoint.getValue());
					previousAmount = datapoint.getValue();
					previousDate = df.parse(datapoint.getKey());
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			collection.addSeries(series);
		}
		JFreeChart chart = ChartFactory.createTimeSeriesChart(title, "Date",
				"Balance", collection);
		diagramsLastYearPanel.setLayout(new BoxLayout(diagramsLastYearPanel,
				BoxLayout.X_AXIS));
		diagramsThisYearPanel.setLayout(new BoxLayout(diagramsThisYearPanel,
				BoxLayout.X_AXIS));
		XYPlot xyPlot = (XYPlot) chart.getPlot();
		DateAxis daxis = (DateAxis) xyPlot.getDomainAxis();
		daxis.setRange(from, to);
		// daxis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY,1));
		NumberAxis naxis = (NumberAxis) xyPlot.getRangeAxis();
		naxis.setNumberFormatOverride(NumberFormat.getCurrencyInstance());
		ChartPanel diagram = new ChartPanel(chart);
		diagram.setPreferredSize(new Dimension(width, height));
		panel.add(diagram);
	}

	private void updateAccountList() {
		Model m = new Model(accountHeader, 0);
		for (String[] row : db.getAccounts()) {
			row[1] = NumberFormat.getCurrencyInstance().format(
					Double.parseDouble(row[1]));
			row[2] = Boolean.toString(row[2].equals("1"));
			m.addRow(row);
		}
		accountsTable.setModel(m);
	}

	private void updateTransactionList() {
		Model m = new Model(transactionHeader, 0);
		for (String[] row : db.getTransactions()) {
			row[2] = NumberFormat.getCurrencyInstance().format(
					Double.parseDouble(row[2]));
			m.addRow(row);
		}
		transactionsTable.setModel(m);
	}

	private void updateYearlyRevList() {
		Model m = new Model(yearlyRevHeader, 0);
		for (String[] row : db.getYearlyRevenues()) {
			row[1] = NumberFormat.getCurrencyInstance().format(
					Double.parseDouble(row[1]));
			m.addRow(row);
		}
		yearlyRevTable.setModel(m);
	}

	private void updateYearlyAccountRevList() {
		Model m = new Model(yearlyAccountRevHeader, 0);
		for (String[] row : db.getYearlyAccountRevenues()) {
			row[2] = NumberFormat.getCurrencyInstance().format(
					Double.parseDouble(row[2]));
			m.addRow(row);
		}
		yearlyAccountRevTable.setModel(m);
	}

	private void updateTotalRevLabel() {
		totalRevLabel.setText(NumberFormat.getCurrencyInstance().format(
				Double.parseDouble(db.getTotalRevenue())));
	}

	private void updateCustomRevLabel() {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		// try {
		// revDateLabel.setText("(" + revDateFromField.getText() + " <-> "
		// + revDateToField.getText() + "):");
		// customRevLabel.setText(db.getRevenue(
		// df.parse(revDateFromField.getText()),
		// df.parse(revDateToField.getText())));
		// revDateLabel.setText("(" + df.format(revDateFromField.getDate())
		// + " <-> " + df.format(revDateToField.getDate()) + "):");
		List<String> accountNames = db.getAccountNames();
		accountNames.add(0, ("All accounts"));
		String selectedAccount = (String) accountRevBox.getModel()
				.getSelectedItem();
		accountRevBox
				.setModel(new DefaultComboBoxModel(accountNames.toArray()));
		accountRevBox.getModel().setSelectedItem(selectedAccount);
		if (selectedAccount == null) {
			selectedAccount = "";
			accountRevBox.setSelectedIndex(0);
		} else if (selectedAccount.equals("All accounts")) {
			selectedAccount = "";
		}
		customRevLabel.setText(NumberFormat.getCurrencyInstance().format(
				Double.parseDouble(db.getRevenue(revDateFromField.getDate(),
						revDateToField.getDate(), selectedAccount))));
		// } catch (ParseException e) {
		// e.printStackTrace();
		// }
	}

	private void updateMonthlyRevList() {
		Model m = new Model(monthlyRevHeader, 0);
		for (String[] row : db.getMonthlyRevenues()) {
			try {
				row[1] = new SimpleDateFormat("MMMM")
						.format(new SimpleDateFormat("MM").parse(row[1]));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			row[2] = NumberFormat.getCurrencyInstance().format(
					Double.parseDouble(row[2]));
			m.addRow(row);
		}
		monthlyRevTable.setModel(m);
	}

	private void updateMonthlyAccountRevList() {
		Model m = new Model(monthlyAccountRevHeader, 0);
		for (String[] row : db.getMonthlyAccountRevenues()) {
			try {
				row[1] = new SimpleDateFormat("MMMM")
						.format(new SimpleDateFormat("MM").parse(row[1]));
			} catch (ParseException e) {
				e.printStackTrace();
			}
			row[3] = NumberFormat.getCurrencyInstance().format(
					Double.parseDouble(row[3]));
			m.addRow(row);
		}
		monthlyAccountRevTable.setModel(m);
	}

	private void updateTotalAccountRevList() {
		Model m = new Model(totalAccountRevHeader, 0);
		for (String[] row : db.getTotalAccountRevenues()) {
			row[1] = NumberFormat.getCurrencyInstance().format(
					Double.parseDouble(row[1]));
			m.addRow(row);
		}
		totalAccountRevTable.setModel(m);
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

	private String[] askUserAccount(String selectedName,
			String selectedBalance, String selectedIncluded) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JTextField nameField = new JTextField("", 15);
		nameField.setText(selectedName);
		JPanel namePanel = new JPanel();
		namePanel.add(new JLabel("Name:"));
		namePanel.add(nameField);
		panel.add(namePanel);

		JTextField balanceField = new JTextField("", 7);
		balanceField.setText(selectedBalance);
		JPanel balancePanel = new JPanel();
		balancePanel.add(new JLabel("Balance:"));
		balancePanel.add(balanceField);
		panel.add(balancePanel);
		JCheckBox includedBox = new JCheckBox("Include in statistics? ",Boolean.parseBoolean(selectedIncluded));
		panel.add(includedBox);

		int result = JOptionPane.showConfirmDialog(null, panel,
				"Account Details", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null);

		if (result == JOptionPane.OK_OPTION) {
			String[] details = new String[3];
			details[0] = nameField.getText();
			details[1] = balanceField.getText();
			details[2] = Boolean.toString(includedBox.isSelected());
 			return details;
		}
		return null;
	}

	private String[] askUserTransaction(Object[] accountValues,
			String selectedAccount, String selectedAmount, String selectedDate,
			String selectedComment) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JComboBox accountField = new JComboBox(accountValues);
		accountField.setSelectedItem(selectedAccount);
		JPanel accountPanel = new JPanel();
		accountPanel.add(new JLabel("Account:"));
		accountPanel.add(accountField);
		panel.add(accountPanel);

		JTextField amountField = new JTextField("", 7);
		amountField.setText(selectedAmount);
		JPanel amountPanel = new JPanel();
		amountPanel.add(new JLabel("Amount:"));
		amountPanel.add(amountField);
		panel.add(amountPanel);

		// JTextField dateField = new JTextField("", 7);
		// dateField.setText(selectedDate);
		JPanel datePanel = new JPanel();
		datePanel.add(new JLabel("Date:"));
		// -----
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		JDateChooser dateSelector = null;
		try {
			dateSelector = new JDateChooser(df.parse(selectedDate),
					"yyyy-MM-dd");
		} catch (ParseException e) {
			e.printStackTrace();
		}
		datePanel.add(dateSelector);
		// -----
		// datePanel.add(dateField);
		panel.add(datePanel);

		JTextField commentField = new JTextField("", 15);
		commentField.setText(selectedComment);
		JPanel commentPanel = new JPanel();
		commentPanel.add(new JLabel("Comment:"));
		commentPanel.add(commentField);
		panel.add(commentPanel);

		int result = JOptionPane.showConfirmDialog(null, panel,
				"Transaction Details", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE, null);

		if (result == JOptionPane.OK_OPTION) {
			String[] details = new String[4];
			details[0] = (String) accountField.getSelectedItem();
			details[1] = amountField.getText();
			details[2] = df.format(dateSelector.getDate());// dateField.getText();
			details[3] = commentField.getText();
			return details;
		}
		return null;
	}

	private class Model extends DefaultTableModel {

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
