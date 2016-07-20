package se.freedrikp.econview.gui.dialogs;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import se.freedrikp.econview.database.SQLiteDatabase;

public abstract class DatabaseDialog {

	protected SQLiteDatabase db;
	private String title;
	private String chainQuestion;
	private JPanel dialogPanel;
	private boolean addNotEdit;

	public DatabaseDialog(SQLiteDatabase db, String title, String chainQuestion) {
		this.db = db;
		this.title = title;
		this.chainQuestion = chainQuestion;
		dialogPanel = new JPanel();
		dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
	}

	protected abstract JComponent createDialog(JPanel dialogPanel);

	private boolean showDialog(JComponent comp) {
		int result = JOptionPane.showConfirmDialog(null, comp, title,
				JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
				null);

		if (result == JOptionPane.YES_OPTION || result == JOptionPane.NO_OPTION) {
			if (addNotEdit) {
				doAddDatabaseAction();
			} else {
				doEditDatabaseAction();
			}
		}
		return result == JOptionPane.YES_OPTION;
	}

	protected abstract void doAddDatabaseAction();

	protected abstract void doEditDatabaseAction();

	public void showEditDialog(Object[] input) {
		addNotEdit = false;
		dialogPanel.removeAll();
		JComponent comp = createDialog(dialogPanel);
		dialogPanel.add(new JLabel(chainQuestion));
		setEditSpecifics(dialogPanel, input);
		if (showDialog(comp)) {
			showAddDialog();
		}

	}

	protected abstract void setEditSpecifics(JPanel dialogPanel, Object[] input);

	protected abstract void setAddSpecifics(JPanel dialogPanel);

	protected abstract void setAddSpecifics(JPanel dialogPanel, Object[] input);

	public void showAddDialog() {
		boolean chain;
		do {
			addNotEdit = true;
			dialogPanel.removeAll();
			JComponent comp = createDialog(dialogPanel);
			dialogPanel.add(new JLabel(chainQuestion));
			setAddSpecifics(dialogPanel);
			chain = showDialog(comp);
		} while (chain);
	}

	public void showAddDialog(Object[] input) {
		addNotEdit = true;
		dialogPanel.removeAll();
		JComponent comp = createDialog(dialogPanel);
		dialogPanel.add(new JLabel(chainQuestion));
		setAddSpecifics(dialogPanel, input);
		if (showDialog(comp)) {
			showAddDialog();
		}
	}

}
