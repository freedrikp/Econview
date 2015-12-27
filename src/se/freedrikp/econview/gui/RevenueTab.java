package se.freedrikp.econview.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.GUI.Model;

import com.toedter.calendar.JDateChooser;

public class RevenueTab extends JPanel implements Observer {
	private Database db;

	private JTable yearlyRevTable;

	private JTable monthlyRevTable;

	private JTable yearlyAccountRevTable;

	private JTable monthlyAccountRevTable;

	// private JTable totalAccountRevTable;

	private JLabel totalRevLabel;

	private JLabel customRevLabel;

	private JDateChooser revDateFromField;

	private JDateChooser revDateToField;

	// private JComboBox accountRevBox;

	private JCheckBox[] selectedAccounts;

	private JCheckBox allAccounts;

	private JPanel customRevAccountPanel;
	private static final String[] monthlyRevHeader = {
			Utilities.getString("REVENUE_HEADER_YEAR"),
			Utilities.getString("REVENUE_HEADER_MONTH"),
			Utilities.getString("REVENUE_HEADER_REVENUE") };
	private static final String[] yearlyRevHeader = {
			Utilities.getString("REVENUE_HEADER_YEAR"),
			Utilities.getString("REVENUE_HEADER_REVENUE") };
	private static final String[] monthlyAccountRevHeader = {
			Utilities.getString("REVENUE_HEADER_YEAR"),
			Utilities.getString("REVENUE_HEADER_MONTH"),
			Utilities.getString("REVENUE_HEADER_ACCOUNT"),
			Utilities.getString("REVENUE_HEADER_REVENUE") };
	private static final String[] yearlyAccountRevHeader = {
			Utilities.getString("REVENUE_HEADER_YEAR"),
			Utilities.getString("REVENUE_HEADER_ACCOUNT"),
			Utilities.getString("REVENUE_HEADER_REVENUE") };
	// private static final String[] totalAccountRevHeader = {
	// Utilities.getString("REVENUE_HEADER_ACCOUNT"),
	// Utilities.getString("REVENUE_HEADER_REVENUE") };
	private final SimpleDateFormat dateFormat;
	private final SimpleDateFormat monthFormat;
	private final SimpleDateFormat yearFormat;

	public RevenueTab(final Database db) {
		super();
		this.db = db;
		db.addObserver(this);
		dateFormat = new SimpleDateFormat(
				Utilities.getConfig("FULL_DATE_FORMAT"));
		monthFormat = new SimpleDateFormat(Utilities.getConfig("MONTH_FORMAT"));
		yearFormat = new SimpleDateFormat(Utilities.getConfig("YEAR_FORMAT"));
		setLayout(new GridLayout(1, 0, 0, 0));

		JScrollPane yearlyRevPane = new JScrollPane();
		add(yearlyRevPane);

		yearlyRevTable = new JTable();
		yearlyRevTable.setEnabled(false);
//		yearlyRevTable.setAutoCreateRowSorter(true);
		yearlyRevPane.setViewportView(yearlyRevTable);

		JScrollPane monthlyRevPane = new JScrollPane();
		add(monthlyRevPane);

		monthlyRevTable = new JTable();
		monthlyRevTable.setEnabled(false);
//		monthlyRevTable.setAutoCreateRowSorter(true);
		monthlyRevPane.setViewportView(monthlyRevTable);

		JScrollPane yearlyAccountRevPane = new JScrollPane();
		add(yearlyAccountRevPane);

		yearlyAccountRevTable = new JTable();
		yearlyAccountRevTable.setEnabled(false);
//		yearlyAccountRevTable.setAutoCreateRowSorter(true);
		yearlyAccountRevPane.setViewportView(yearlyAccountRevTable);

		JScrollPane monthlyAccountRevPane = new JScrollPane();
		add(monthlyAccountRevPane);

		monthlyAccountRevTable = new JTable();
		monthlyAccountRevTable.setEnabled(false);
//		monthlyAccountRevTable.setAutoCreateRowSorter(true);
		monthlyAccountRevPane.setViewportView(monthlyAccountRevTable);

		// JScrollPane totalAccountRevPane = new JScrollPane();
		// add(totalAccountRevPane);

		// totalAccountRevTable = new JTable();
		// totalAccountRevTable.setEnabled(false);
		// totalAccountRevPane.setViewportView(totalAccountRevTable);

		JPanel sideRevenuePanel = new JPanel();
		add(sideRevenuePanel);
		sideRevenuePanel.setLayout(new BoxLayout(sideRevenuePanel,
				BoxLayout.Y_AXIS));

		JLabel lblTotalRevenue = new JLabel(
				Utilities.getString("TOTAL_REVENUE") + ":");
		lblTotalRevenue.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideRevenuePanel.add(lblTotalRevenue);

		totalRevLabel = new JLabel("");
		totalRevLabel.setForeground(Color.RED);
		totalRevLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideRevenuePanel.add(totalRevLabel);

		JSeparator totCustomRevSep = new JSeparator();
		sideRevenuePanel.add(totCustomRevSep);

		// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		JLabel lblCustomRevenue = new JLabel(
				Utilities.getString("CUSTOM_REVENUE") + ":");
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
		revDateFromField = new JDateChooser(new Date(), dateFormat.toPattern());
		revDateFromField.setMaximumSize(new Dimension(Integer.parseInt(Utilities.getConfig("DATE_FIELD_WIDTH")),Integer.parseInt(Utilities.getConfig("DATE_FIELD_HEIGHT"))));
		sideRevenuePanel.add(revDateFromField);
		// revDateFromField.setColumns(7);

		JLabel revDateSepLabel = new JLabel("<->");
		revDateSepLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideRevenuePanel.add(revDateSepLabel);

		// revDateToField = new JTextField(df.format(new Date()));
		revDateToField = new JDateChooser(new Date(), dateFormat.toPattern());
		revDateToField.setMaximumSize(new Dimension(Integer.parseInt(Utilities.getConfig("DATE_FIELD_WIDTH")),Integer.parseInt(Utilities.getConfig("DATE_FIELD_HEIGHT"))));
		sideRevenuePanel.add(revDateToField);
		// revDateToField.setColumns(7);

		JButton customRevButton = new JButton(
				Utilities.getString("CUSTOM_REVENUE"));
		customRevButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update(db, null);
			}
		});

		// accountRevBox = new JComboBox();
		// sideRevenuePanel.add(accountRevBox);

		customRevAccountPanel = new JPanel();
		customRevAccountPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		customRevAccountPanel.setLayout(new BoxLayout(customRevAccountPanel,
				BoxLayout.Y_AXIS));
		sideRevenuePanel.add(new JScrollPane(customRevAccountPanel));

		allAccounts = new JCheckBox(Utilities.getString("ALL_ACCOUNTS"), false);

		allAccounts.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean select = e.getStateChange() == ItemEvent.SELECTED;
				for (JCheckBox checkBox : selectedAccounts) {
					checkBox.setSelected(select);
					// update(db,null);
				}
			}

		});

		customRevButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideRevenuePanel.add(customRevButton);

//		Component verticalStrut = Box.createVerticalStrut(2000);
//		sideRevenuePanel.add(verticalStrut);

		update(db, null);
	}

	public void update(Observable o, Object arg) {
		updateYearlyRevList();
		GUI.resizeTable(yearlyRevTable);
		updateMonthlyRevList();
		GUI.resizeTable(monthlyRevTable);
		updateYearlyAccountRevList();
		GUI.resizeTable(yearlyAccountRevTable);
		updateMonthlyAccountRevList();
		GUI.resizeTable(monthlyAccountRevTable);
		// updateTotalAccountRevList();
		// GUI.resizeTable(totalAccountRevTable);
		updateTotalRevLabel();
		updateCustomRevLabel();
		validate();
		repaint();
	}

	private void updateYearlyRevList() {
		Model m = new Model(yearlyRevHeader, 0);
		for (Object[] row : db.getYearlyRevenues()) {
			row[0] = yearFormat.format(row[0]);
			row[1] = NumberFormat.getCurrencyInstance().format(row[1]);
			m.addRow(row);
		}
		yearlyRevTable.setModel(m);
		TableRowSorter<Model> sorter = new TableRowSorter<Model>(m);
		yearlyRevTable.setRowSorter(sorter);
		sorter.setComparator(1, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return Double.compare(GUI.parseAmount(o1), GUI.parseAmount(o2));
			}
		}
		);
	}

	private void updateYearlyAccountRevList() {
		Model m = new Model(yearlyAccountRevHeader, 0);
		for (Object[] row : db.getYearlyAccountRevenues()) {
			row[0] = yearFormat.format(row[0]);
			row[2] = NumberFormat.getCurrencyInstance().format(row[2]);
			m.addRow(row);
		}
		yearlyAccountRevTable.setModel(m);
		TableRowSorter<Model> sorter = new TableRowSorter<Model>(m);
		yearlyAccountRevTable.setRowSorter(sorter);
		sorter.setComparator(2, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return Double.compare(GUI.parseAmount(o1), GUI.parseAmount(o2));
			}
		}
		);
	}

	private void updateTotalRevLabel() {
		totalRevLabel.setText(NumberFormat.getCurrencyInstance().format(
				db.getTotalRevenue()));
	}

	private void updateCustomRevLabel() {
		// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		// try {
		// revDateLabel.setText("(" + revDateFromField.getText() + " <-> "
		// + revDateToField.getText() + "):");
		// customRevLabel.setText(db.getRevenue(
		// df.parse(revDateFromField.getText()),
		// df.parse(revDateToField.getText())));
		// revDateLabel.setText("(" + df.format(revDateFromField.getDate())
		// + " <-> " + df.format(revDateToField.getDate()) + "):");

		// List<String> accountNames = db.getAccountNames();
		// accountNames.add(0, Utilities.getString("ALL_ACCOUNTS"));
		// String selectedAccount = (String) accountRevBox.getModel()
		// .getSelectedItem();
		// accountRevBox
		// .setModel(new DefaultComboBoxModel(accountNames.toArray()));
		// accountRevBox.getModel().setSelectedItem(selectedAccount);
		// if (selectedAccount == null) {
		// selectedAccount = "";
		// accountRevBox.setSelectedIndex(0);
		// } else if
		// (selectedAccount.equals(Utilities.getString("ALL_ACCOUNTS"))) {
		// selectedAccount = "";
		// }
		customRevAccountPanel.removeAll();
		List<String> accounts = db.getAccountNames();
		HashSet<String> oldSelectedAccounts = new HashSet<String>();
		if (selectedAccounts != null) {
			for (JCheckBox checkBox : selectedAccounts) {
				if (checkBox.isSelected()) {
					oldSelectedAccounts.add(checkBox.getText());
				}
			}
		}

		customRevAccountPanel.add(allAccounts);
		customRevAccountPanel.add(new JSeparator());
		selectedAccounts = new JCheckBox[accounts.size()];
		for (int i = 0; i < accounts.size(); i++) {
			String account = accounts.get(i);
			selectedAccounts[i] = new JCheckBox(account,
					oldSelectedAccounts.contains(account));
			customRevAccountPanel.add(selectedAccounts[i]);
		}

		customRevLabel.setText(NumberFormat.getCurrencyInstance().format(
				db.getRevenue(revDateFromField.getDate(),
						revDateToField.getDate(), oldSelectedAccounts)));
		// } catch (ParseException e) {
		// e.printStackTrace();
		// }
	}

	private void updateMonthlyRevList() {
		Model m = new Model(monthlyRevHeader, 0);
		for (Object[] row : db.getMonthlyRevenues()) {
			Object[] data = new Object[row.length + 1];
			data[0] = yearFormat.format(row[0]);
			data[1] = monthFormat.format(row[0]);
			data[2] = NumberFormat.getCurrencyInstance().format(row[1]);
			m.addRow(data);
		}
		monthlyRevTable.setModel(m);
		TableRowSorter<Model> sorter = new TableRowSorter<Model>(m);
		monthlyRevTable.setRowSorter(sorter);
		sorter.setComparator(2, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return Double.compare(GUI.parseAmount(o1), GUI.parseAmount(o2));
			}
		}
		);
	}

	private void updateMonthlyAccountRevList() {
		Model m = new Model(monthlyAccountRevHeader, 0);
		for (Object[] row : db.getMonthlyAccountRevenues()) {
			Object[] data = new Object[row.length + 1];
			data[0] = yearFormat.format(row[0]);
			data[1] = monthFormat.format(row[0]);
			data[2] = row[1];
			data[3] = NumberFormat.getCurrencyInstance().format(row[2]);
			m.addRow(data);
		}
		monthlyAccountRevTable.setModel(m);
		TableRowSorter<Model> sorter = new TableRowSorter<Model>(m);
		monthlyAccountRevTable.setRowSorter(sorter);
		sorter.setComparator(3, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return Double.compare(GUI.parseAmount(o1), GUI.parseAmount(o2));
			}
		}
		);
	}

	// private void updateTotalAccountRevList() {
	// Model m = new Model(totalAccountRevHeader, 0);
	// for (String[] row : db.getTotalAccountRevenues()) {
	// row[1] = NumberFormat.getCurrencyInstance().format(
	// Double.parseDouble(row[1]));
	// m.addRow(row);
	// }
	// totalAccountRevTable.setModel(m);
	// }

}
