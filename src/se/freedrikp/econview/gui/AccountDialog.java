package se.freedrikp.econview.gui;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class AccountDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField accountNameTextField;
	private JTextField balanceTextField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			AccountDialog dialog = new AccountDialog("","");
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public AccountDialog(String accountName, String accountBalance) {
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
			JLabel balanceLabel = new JLabel("Balance:");
			contentPanel.add(balanceLabel);
		}
		{
			balanceTextField = new JTextField();
			contentPanel.add(balanceTextField);
			balanceTextField.setColumns(10);
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
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
					}
				});
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
