package se.freedrikp.econview.gui.tabs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import se.freedrikp.econview.common.Common;
import se.freedrikp.econview.common.Configuration;
import se.freedrikp.econview.common.Language;
import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.gui.panels.AccountSelectorPanel;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JSpinnerDateEditor;

public class DiagramsTab extends JPanel implements Observer {
	private Database db;
	private JPanel diagramsLastYearPanel;
	private JPanel diagramsLastMonthPanel;
	private JPanel diagramsThisYearPanel;
	private JPanel diagramsThisMonthPanel;
	private JDateChooser diagFromDateField;
	private JDateChooser diagToDateField;
	private AccountSelectorPanel diagAccountsPanel;
	private JPanel customDiagPanel;
	// private final int DIAGRAM_WIDTH = 350;
	// private final int DIAGRAM_HEIGHT = 175;
	// private final int CUSTOM_DIAGRAM_WIDTH = 480;
	// private final int CUSTOM_DIAGRAM_HEIGHT = 350;
	private final int DIAGRAM_WIDTH = Configuration.getInt("DIAGRAM_WIDTH");
	private final int DIAGRAM_HEIGHT = Configuration.getInt("DIAGRAM_HEIGHT");
	private final int CUSTOM_DIAGRAM_WIDTH = Configuration
			.getInt("CUSTOM_DIAGRAM_WIDTH");
	private final int CUSTOM_DIAGRAM_HEIGHT = Configuration
			.getInt("CUSTOM_DIAGRAM_HEIGHT");
	private final SimpleDateFormat dateFormat;

	public DiagramsTab(final Database db) {
		super();
		this.db = db;
		db.addObserver(this);
		dateFormat = new SimpleDateFormat(
				Configuration.getString("FULL_DATE_FORMAT"));
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		diagramsLastYearPanel = new JPanel();
		diagramsLastMonthPanel = new JPanel();
		diagramsThisYearPanel = new JPanel();
		diagramsThisMonthPanel = new JPanel();
		diagramsThisMonthPanel.setLayout(new BoxLayout(diagramsThisMonthPanel,
				BoxLayout.X_AXIS));
		diagramsLastMonthPanel.setLayout(new BoxLayout(diagramsLastMonthPanel,
				BoxLayout.X_AXIS));
		diagramsLastYearPanel.setLayout(new BoxLayout(diagramsLastYearPanel,
				BoxLayout.X_AXIS));
		diagramsThisYearPanel.setLayout(new BoxLayout(diagramsThisYearPanel,
				BoxLayout.X_AXIS));
		if (Configuration.getString("DIAGRAMS_STYLE_SPLIT_OR_TAB").equals(
				"SPLIT")) {
			JSplitPane diagramYearSplitPane = new JSplitPane(
					JSplitPane.VERTICAL_SPLIT, diagramsLastYearPanel,
					diagramsThisYearPanel);
			JSplitPane diagramMonthSplitPane = new JSplitPane(
					JSplitPane.VERTICAL_SPLIT, diagramsLastMonthPanel,
					diagramsThisMonthPanel);
			JSplitPane diagramSplitPane = new JSplitPane(
					JSplitPane.HORIZONTAL_SPLIT, diagramYearSplitPane,
					diagramMonthSplitPane);
			add(diagramSplitPane);
		} else if (Configuration.getString("DIAGRAMS_STYLE_SPLIT_OR_TAB")
				.equals("TAB")) {
			JTabbedPane diagrams = new JTabbedPane();
			diagrams.add(Language.getString("LAST_YEAR"), diagramsLastYearPanel);
			diagrams.add(Language.getString("LAST_MONTH"),
					diagramsLastMonthPanel);
			diagrams.add(
					Integer.toString(Calendar.getInstance().get(Calendar.YEAR)),
					diagramsThisYearPanel);
			diagrams.add(
					Calendar.getInstance().getDisplayName(Calendar.MONTH,
							Calendar.LONG, Locale.getDefault()),
					diagramsThisMonthPanel);
			add(diagrams);
		}

		JPanel diagramControlPanel = new JPanel();
		add(diagramControlPanel);
		diagramControlPanel.setLayout(new BoxLayout(diagramControlPanel,
				BoxLayout.Y_AXIS));

		// diagFromDateField = new JTextField(df.format(new Date()));
		diagFromDateField = new JDateChooser(new JSpinnerDateEditor());
		diagFromDateField.setDateFormatString(dateFormat.toPattern());
		diagFromDateField.setDate(new Date());
		diagFromDateField.setMaximumSize(new Dimension(Integer
				.parseInt(Configuration.getString("DATE_FIELD_WIDTH")), Integer
				.parseInt(Configuration.getString("DATE_FIELD_HEIGHT"))));
		diagramControlPanel.add(diagFromDateField);
		// diagFromDateField.setColumns(10);

		JLabel diagDateSepLabel = new JLabel("<->");
		diagDateSepLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		diagramControlPanel.add(diagDateSepLabel);

		// diagToDateField = new JTextField(df.format(new Date()));
		diagToDateField = new JDateChooser(new JSpinnerDateEditor());
		diagToDateField.setDateFormatString(dateFormat.toPattern());
		diagToDateField.setDate(new Date());
		diagToDateField.setMaximumSize(new Dimension(Integer
				.parseInt(Configuration.getString("DATE_FIELD_WIDTH")), Integer
				.parseInt(Configuration.getString("DATE_FIELD_HEIGHT"))));
		diagramControlPanel.add(diagToDateField);
		// diagToDateField.setColumns(10);

		diagAccountsPanel = new AccountSelectorPanel(db, false, true);
		diagAccountsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		diagramControlPanel.add(new JScrollPane(diagAccountsPanel));

		JButton btnCustomDiagram = new JButton(
				Language.getString("CUSTOM_DIAGRAM"));
		btnCustomDiagram.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				update(db, null);
			}
		});
		btnCustomDiagram.setAlignmentX(Component.CENTER_ALIGNMENT);
		diagramControlPanel.add(btnCustomDiagram);

		customDiagPanel = new JPanel();
		add(customDiagPanel);

		update(db, null);
	}

	public void update(Observable o, Object arg) {
		updateDiagrams();
		validate();
		repaint();
	}

	private void updateDiagrams() {
		diagramsLastYearPanel.removeAll();
		diagramsLastMonthPanel.removeAll();
		diagramsThisMonthPanel.removeAll();
		diagramsThisYearPanel.removeAll();
		customDiagPanel.removeAll();

		List<String> accounts = db.getAccountNames();

		Calendar start = Common.getFlattenCalendar(null);
		Calendar end = Common.getFlattenCalendar(null);

		start.set(Calendar.MONTH, start.getActualMinimum(Calendar.MONTH));
		start.set(Calendar.DATE, start.getActualMinimum(Calendar.DATE));
		end.set(Calendar.MONTH, start.getActualMaximum(Calendar.MONTH));
		end.set(Calendar.DATE, start.getActualMaximum(Calendar.DATE));
		generateDiagram(start.getTime(), end.getTime(),
				Integer.toString(Calendar.getInstance().get(Calendar.YEAR)),
				diagramsThisYearPanel, DIAGRAM_WIDTH, DIAGRAM_HEIGHT, accounts,
				true);
		start = Common.getFlattenCalendar(null);
		end = Common.getFlattenCalendar(null);
		start.set(Calendar.DATE, start.getActualMinimum(Calendar.DATE));
		end.set(Calendar.DATE, start.getActualMaximum(Calendar.DATE));
		generateDiagram(
				start.getTime(),
				end.getTime(),
				Calendar.getInstance().getDisplayName(Calendar.MONTH,
						Calendar.LONG, Locale.getDefault()),
				diagramsThisMonthPanel, DIAGRAM_WIDTH, DIAGRAM_HEIGHT,
				accounts, true);
		start = Common.getFlattenCalendar(null);
		end = Common.getFlattenCalendar(null);
		start.add(Calendar.YEAR, -1);
		generateDiagram(start.getTime(), end.getTime(),
				Language.getString("LAST_YEAR"), diagramsLastYearPanel,
				DIAGRAM_WIDTH, DIAGRAM_HEIGHT, accounts, true);
		start = Common.getFlattenCalendar(null);
		end = Common.getFlattenCalendar(null);
		start.add(Calendar.MONTH, -1);
		generateDiagram(start.getTime(), end.getTime(),
				Language.getString("LAST_MONTH"), diagramsLastMonthPanel,
				DIAGRAM_WIDTH, DIAGRAM_HEIGHT, accounts, true);
		// try {
		// generateDiagram(
		// df.parse(diagFromDateField.getText()),
		// df.parse(diagToDateField.getText()),"Custom Diagram",customDiagPanel,400,300);

		generateDiagram(Common.getFlattenCalendar(diagFromDateField.getDate())
				.getTime(), Common
				.getFlattenCalendar(diagToDateField.getDate()).getTime(),
				Language.getString("CUSTOM_DIAGRAM"), customDiagPanel,
				CUSTOM_DIAGRAM_WIDTH, CUSTOM_DIAGRAM_HEIGHT,
				diagAccountsPanel.getSelectedAccounts(),
				diagAccountsPanel.isTotalSelected());

		// } catch (ParseException e) {
		// e.printStackTrace();
		// }

	}

	private void generateDiagram(Date from, Date to, String title,
			JPanel panel, int width, int height, Collection<String> accounts,
			boolean includeTotal) {
		Map<String, Map<Date, Double>> diagramData = db.getCustomDiagramData(
				from, to, accounts, includeTotal,Language.getString("TOTAL_ACCOUNT_NAME"));
		TimeSeriesCollection collection = new TimeSeriesCollection();
		for (Map.Entry<String, Map<Date, Double>> dataset : diagramData
				.entrySet()) {
			double previousAmount = 0;
			Date previousDate = from;
			TimeSeries series = new TimeSeries(dataset.getKey());
			// System.out.println("-----------------------");
			for (Map.Entry<Date, Double> datapoint : dataset.getValue()
					.entrySet()) {
				Calendar start = Calendar.getInstance();
				start.setTime(previousDate);
				start.add(Calendar.DATE, 1);
				Calendar end = Calendar.getInstance();
				end.setTime(datapoint.getKey());

				for (Date date = start.getTime(); start.before(end); start.add(
						Calendar.DATE, 1), date = start.getTime()) {
					series.add(new Day(date), previousAmount);
				}

				series.addOrUpdate(new Day(datapoint.getKey()),
						datapoint.getValue());
				// System.out.println(datapoint.getKey().toString());
				previousAmount = datapoint.getValue();
				previousDate = datapoint.getKey();
			}
			collection.addSeries(series);
		}
		JFreeChart chart = ChartFactory.createTimeSeriesChart(title,
				Language.getString("DIAGRAM_DATE"),
				Language.getString("DIAGRAM_BALANCE"), collection);
		XYPlot xyPlot = (XYPlot) chart.getPlot();
		DateAxis daxis = (DateAxis) xyPlot.getDomainAxis();
		if (from.before(to)) {
			daxis.setRange(from, to);
		}
		// daxis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY,7));
		NumberAxis naxis = (NumberAxis) xyPlot.getRangeAxis();
		naxis.setNumberFormatOverride(NumberFormat.getCurrencyInstance());
		ChartPanel diagram = new ChartPanel(chart);
		diagram.setPreferredSize(new Dimension(width, height));
		panel.add(diagram);
	}

}
