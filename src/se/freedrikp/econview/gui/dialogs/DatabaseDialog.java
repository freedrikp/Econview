package se.freedrikp.econview.gui.dialogs;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import se.freedrikp.econview.database.Database;

public abstract class DatabaseDialog {

	private Database db;
	private String title;
	private String chainQuestion;
	private JPanel dialogPanel;
	private boolean addNotEdit;

	public DatabaseDialog(Database db, String title, String chainQuestion) {
		this.db = db;
		this.title = title;
		this.chainQuestion = chainQuestion;
		dialogPanel = new JPanel();
		dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
	}

	protected abstract void createDialog(JPanel dialogPanel);

	private boolean showDialog(JPanel dialogPanel) {
		int result = JOptionPane.showConfirmDialog(null, dialogPanel, title,
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null);

		if (result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION) {
			if (addNotEdit){
				doAddDatabaseAction(db);
			}else{
				doEditDatabaseAction(db);
			}
		}
		return result == JOptionPane.YES_OPTION;
	}

	protected abstract void doAddDatabaseAction(Database db);
	protected abstract void doEditDatabaseAction(Database db);

	public void showEditDialog(Object[] input) {
		addNotEdit = false;
		dialogPanel.removeAll();
		createDialog(dialogPanel);
		dialogPanel.add(new JLabel(chainQuestion));
		setEditSpecifics(dialogPanel, input);
		if (showDialog(dialogPanel)) {
			showAddDialog();
		}

	}

	protected abstract void setEditSpecifics(JPanel dialogPanel, Object[] input);

	protected abstract void setAddSpecifics(JPanel dialogPanel);

	public void showAddDialog() {
		boolean chain;
		do {
			addNotEdit = true;
			dialogPanel.removeAll();
			createDialog(dialogPanel);
			dialogPanel.add(new JLabel(chainQuestion));
			setAddSpecifics(dialogPanel);
			chain = showDialog(dialogPanel);
		} while (chain);
	}

}
