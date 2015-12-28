package se.freedrikp.econview.gui;

import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.database.Security;

public class GUI extends JFrame implements Observer {

	private JPanel contentPane;
	// private JLabel revDateLabel;
	private Security sec;
	private final int WIDTH = Integer.parseInt(Utilities.getConfig("WINDOW_WIDTH"));
	private final int HEIGHT = Integer.parseInt(Utilities.getConfig("WINDOW_HEIGHT"));

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
					Security security = new Security(Utilities.getConfig("USERS_DATABASE_FILE"));
					Database db = security.openDatabase(Utilities.getConfig("DATABASE_FILE"));
					GUI frame = new GUI(db,security);
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
	public GUI(Database db, Security sec) {
		super("EconView");
		//setResizable(false);
		// this.dbfile = dbfile;
		this.sec = sec;
		sec.addObserver(this);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//setBounds(100, 100, 1280, 430);
//		setBounds(100, 100, 1360, 500);
		DisplayMode dm = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode();
		setBounds((dm.getWidth()-WIDTH)/2, (dm.getHeight()-HEIGHT)/2, WIDTH, HEIGHT);

		MenuBar menuBar = new MenuBar(db,sec);
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
		tabbedPane.addTab(Utilities.getString("ACCOUNTS_TAB_NAME"), null, accountsPanel, null);

		TransactionsTab transactionsPanel = new TransactionsTab(db);
		tabbedPane.addTab(Utilities.getString("TRANSACTIONS_TAB_NAME"), null, transactionsPanel, null);

		RevenueTab revenuePanel = new RevenueTab(db);
		tabbedPane.addTab(Utilities.getString("REVENUES_TAB_NAME"), null, revenuePanel, null);

		DiagramsTab diagramsPanel = new DiagramsTab(db);
		tabbedPane.addTab(Utilities.getString("DIAGRAMS_TAB_NAME"), null, diagramsPanel, null);

		update(db, null);
	}

	public static double parseAmount(String amount)
			throws NumberFormatException {
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
			return getValueAt(0,columnIndex).getClass();
		}

		public Model(String[] header, int rows) {
			super(header, rows);
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}

	}
	
	public static void resizeTable(JTable table) {
		for (int column = 0; column < table.getColumnCount(); column++)
		{
		    TableColumn tableColumn = table.getColumnModel().getColumn(column);
		    int preferredWidth = tableColumn.getMinWidth();
		    int maxWidth = tableColumn.getMaxWidth();
		 
		    for (int row = 0; row < table.getRowCount(); row++)
		    {
		        TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
		        Component c = table.prepareRenderer(cellRenderer, row, column);
		        int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
		        preferredWidth = Math.max(preferredWidth, width);
		 
		        //  We've exceeded the maximum width, no need to check other rows
		 
		        if (preferredWidth >= maxWidth)
		        {
		            preferredWidth = maxWidth;
		            break;
		        }
		    }
		 
		    tableColumn.setPreferredWidth( preferredWidth );
		}
	}

	public void update(Observable o, Object arg) {
		setTitle("EconView - " + sec.getFile().getAbsolutePath());
		repaint();
	}
	
}
