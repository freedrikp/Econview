package se.freedrikp.econview.experiment;


import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class TransactionDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField datetextField;
	private JTextField accountNameTextField;
	private JTextField commentTextField;
	private JTextField amountTextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			TransactionDialog dialog = new TransactionDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public TransactionDialog() {
		setBounds(100, 100, 450, 241);
		getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel);
		contentPanel.setLayout(new GridLayout(5, 2, 0, 0));
		{
			JLabel accountNameLabel = new JLabel("Account Name:");
			contentPanel.add(accountNameLabel);
		}
		{
			accountNameTextField = new JTextField();
			contentPanel.add(accountNameTextField);
			accountNameTextField.setColumns(10);
		}
		{
			JLabel amountLabel = new JLabel("Amount:");
			contentPanel.add(amountLabel);
		}
		{
			amountTextField = new JTextField();
			contentPanel.add(amountTextField);
			amountTextField.setColumns(10);
		}
		{
			JLabel dateLabel = new JLabel("Date:");
			contentPanel.add(dateLabel);
		}
		{
			datetextField = new JTextField();
			contentPanel.add(datetextField);
			datetextField.setColumns(10);
		}
		{
			JLabel commentLabel = new JLabel("Comment");
			contentPanel.add(commentLabel);
		}
		{
			commentTextField = new JTextField();
			contentPanel.add(commentTextField);
			commentTextField.setColumns(10);
		}
		{
			JLabel placeholder = new JLabel("");
			contentPanel.add(placeholder);
		}
		{
			JPanel buttonPane = new JPanel();
			contentPanel.add(buttonPane);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

}
