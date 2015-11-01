package se.freedrikp.econview.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;

import com.toedter.calendar.JDateChooser;

import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.GUI.Model;

public class RevenueTab extends JPanel implements Observer{
	private Database db;

	private JTable yearlyRevTable;

	private JTable monthlyRevTable;

	private JTable yearlyAccountRevTable;

	private JTable monthlyAccountRevTable;

	private JTable totalAccountRevTable;

	private JLabel totalRevLabel;

	private JLabel customRevLabel;

	private JDateChooser revDateFromField;

	private JDateChooser revDateToField;

	private JComboBox accountRevBox;
	
	private static final String[] monthlyRevHeader = { Utilities.getString("REVENUE_HEADER_YEAR"), Utilities.getString("REVENUE_HEADER_MONTH"),
	Utilities.getString("REVENEUE_HEADER_REVENUE") };
private static final String[] yearlyRevHeader = { Utilities.getString("REVENUE_HEADER_YEAR"), Utilities.getString("REVENEUE_HEADER_REVENUE") };
private static final String[] monthlyAccountRevHeader = { Utilities.getString("REVENUE_HEADER_YEAR"), Utilities.getString("REVENUE_HEADER_MONTH"),
	Utilities.getString("REVENEUE_HEADER_REVENUE"), Utilities.getString("REVENEUE_HEADER_ACCOUNT") };
private static final String[] yearlyAccountRevHeader = { Utilities.getString("REVENUE_HEADER_YEAR"), Utilities.getString("REVENEUE_HEADER_ACCOUNT"),
	Utilities.getString("REVENEUE_HEADER_REVENUE") };
private static final String[] totalAccountRevHeader = { Utilities.getString("REVENEUE_HEADER_ACCOUNT"),
	Utilities.getString("REVENEUE_HEADER_REVENUE") };

	public RevenueTab(final Database db){
		super();
		this.db = db;
		db.addObserver(this);
		setLayout(new GridLayout(1, 0, 0, 0));

		JScrollPane yearlyRevPane = new JScrollPane();
		add(yearlyRevPane);

		yearlyRevTable = new JTable();
		yearlyRevTable.setEnabled(false);
		yearlyRevPane.setViewportView(yearlyRevTable);

		JScrollPane monthlyRevPane = new JScrollPane();
		add(monthlyRevPane);

		monthlyRevTable = new JTable();
		monthlyRevTable.setEnabled(false);
		monthlyRevPane.setViewportView(monthlyRevTable);

		JScrollPane yearlyAccountRevPane = new JScrollPane();
		add(yearlyAccountRevPane);

		yearlyAccountRevTable = new JTable();
		yearlyAccountRevTable.setEnabled(false);
		yearlyAccountRevPane.setViewportView(yearlyAccountRevTable);

		JScrollPane monthlyAccountRevPane = new JScrollPane();
		add(monthlyAccountRevPane);

		monthlyAccountRevTable = new JTable();
		monthlyAccountRevTable.setEnabled(false);
		monthlyAccountRevPane.setViewportView(monthlyAccountRevTable);

		JScrollPane totalAccountRevPane = new JScrollPane();
		add(totalAccountRevPane);

		totalAccountRevTable = new JTable();
		totalAccountRevTable.setEnabled(false);
		totalAccountRevPane.setViewportView(totalAccountRevTable);

		JPanel sideRevenuePanel = new JPanel();
		add(sideRevenuePanel);
		sideRevenuePanel.setLayout(new BoxLayout(sideRevenuePanel,
				BoxLayout.Y_AXIS));

		JLabel lblTotalRevenue = new JLabel(Utilities.getString("TOTAL_REVENUE") + ":");
		lblTotalRevenue.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideRevenuePanel.add(lblTotalRevenue);

		totalRevLabel = new JLabel("");
		totalRevLabel.setForeground(Color.RED);
		totalRevLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		sideRevenuePanel.add(totalRevLabel);

		JSeparator totCustomRevSep = new JSeparator();
		sideRevenuePanel.add(totCustomRevSep);

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

		JLabel lblCustomRevenue = new JLabel(Utilities.getString("CUSTOM_REVENUE") + ":");
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

		JButton customRevButton = new JButton(Utilities.getString("CUSTOM_REVENUE"));
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
		
		update(db,null);
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
		updateTotalAccountRevList();
		GUI.resizeTable(totalAccountRevTable);
		updateTotalRevLabel();
		updateCustomRevLabel();
		repaint();
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
		accountNames.add(0, Utilities.getString("ALL_ACCOUNTS"));
		String selectedAccount = (String) accountRevBox.getModel()
				.getSelectedItem();
		accountRevBox
				.setModel(new DefaultComboBoxModel(accountNames.toArray()));
		accountRevBox.getModel().setSelectedItem(selectedAccount);
		if (selectedAccount == null) {
			selectedAccount = "";
			accountRevBox.setSelectedIndex(0);
		} else if (selectedAccount.equals(Utilities.getString("ALL_ACCOUNTS"))) {
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

}
