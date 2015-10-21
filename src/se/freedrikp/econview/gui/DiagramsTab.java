package se.freedrikp.econview.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import com.toedter.calendar.JDateChooser;

import se.freedrikp.econview.database.Database;

public class DiagramsTab extends JPanel implements Observer{
	private Database db;
	private JPanel diagramsLastYearPanel;
	private JPanel diagramsLastMonthPanel;
	private JPanel diagramsThisYearPanel;
	private JPanel diagramsThisMonthPanel;
	private JDateChooser diagFromDateField;
	private JDateChooser diagToDateField;
	private JPanel customDiagPanel;
	
	public DiagramsTab(final Database db){
		super();
		this.db = db;
		db.addObserver(this);
		
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
		add(diagramSplitPane);

		JPanel diagramControlPanel = new JPanel();
		add(diagramControlPanel);
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
		add(customDiagPanel);

		update(db,null);
	}


	public void update(Observable o, Object arg) {
		updateDiagrams();
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


}
