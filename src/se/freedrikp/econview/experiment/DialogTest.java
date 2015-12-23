package se.freedrikp.econview.experiment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class DialogTest extends JFrame implements ActionListener{
	private boolean clicked;
	public DialogTest(){
		super("DialogTest");
		JButton button = new JButton("Ok");
		add(button);
		button.addActionListener(this);
		setVisible(true);
		setBounds(0,0,300,300);
		clicked = false;
		waitForClick();
//		setVisible(false);
		dispose();
	}
	
	public synchronized void waitForClick(){
		while(!clicked){
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void click(){
		clicked = true;
		notifyAll();
	}
	

	public void actionPerformed(ActionEvent e) {
		click();
	}
	

	public static void main(String[] args) {
		new DialogTest();
	}




}
