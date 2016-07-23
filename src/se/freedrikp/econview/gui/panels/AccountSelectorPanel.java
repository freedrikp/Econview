package se.freedrikp.econview.gui.panels;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import se.freedrikp.econview.common.Language;
import se.freedrikp.econview.database.Database;
import se.freedrikp.econview.database.SQLiteDatabase;

public class AccountSelectorPanel extends JPanel implements Observer,
		ItemListener {
	private Database db;
	private boolean startState;
	private boolean includeTotal;
	private JCheckBox allAccounts;
	private JCheckBox total;
	private JCheckBox[] accountBoxes;

	public AccountSelectorPanel(Database db, boolean startState,
			boolean includeTotal) {
		super();
		this.db = db;
		db.addObserver(this);
		this.startState = startState;
		this.includeTotal = includeTotal;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		allAccounts = new JCheckBox(Language.getString("ALL_ACCOUNTS"),
				startState);
		// allAccounts.setAlignmentX(Component.CENTER_ALIGNMENT);

		allAccounts.addItemListener(this);

		if (includeTotal) {
			total = new JCheckBox(Language.getString("TOTAL_ACCOUNT_NAME"),
					startState);
		}
		// total.setAlignmentX(Component.CENTER_ALIGNMENT);
		update(db, null);
	}

	public void update(Observable o, Object arg) {
		removeAll();

		List<String> accounts = db.getAccountNames();
		HashSet<String> oldSelectedAccounts = new HashSet<String>();
		if (accountBoxes != null) {
			for (JCheckBox checkBox : accountBoxes) {
				if (checkBox.isSelected()) {
					oldSelectedAccounts.add(checkBox.getText());
				}
			}
		}

		add(allAccounts);
		if (includeTotal) {
			add(total);
		}
		add(new JSeparator());

		boolean first = accountBoxes == null;

		accountBoxes = new JCheckBox[accounts.size()];
		for (int i = 0; i < accounts.size(); i++) {
			String account = accounts.get(i);
			boolean state = first ? startState : oldSelectedAccounts
					.contains(account);
			accountBoxes[i] = new JCheckBox(account, state);
			accountBoxes[i].addItemListener(this);
			add(accountBoxes[i]);
		}
		removeListeners();
		boolean allSelected = true;
		for (JCheckBox checkBox : accountBoxes) {
			if (!checkBox.isSelected()) {
				allSelected = false;
			}
		}
		allAccounts.setSelected(allSelected);
		addListeners();
	}

	public Collection<String> getSelectedAccounts() {
		Set<String> accounts = new HashSet<String>();
		for (JCheckBox checkBox : accountBoxes) {
			if (checkBox.isSelected()) {
				accounts.add(checkBox.getText());
			}
		}
		return accounts;
	}

	public boolean isTotalSelected() {
		return includeTotal && total.isSelected();
	}

	public void itemStateChanged(ItemEvent e) {
		removeListeners();
		if (e.getSource() == allAccounts) {
			boolean select = e.getStateChange() == ItemEvent.SELECTED;
			for (JCheckBox checkBox : accountBoxes) {
				checkBox.setSelected(select);
				// update(db,null);
			}

		} else {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				allAccounts.setSelected(false);
			} else {
				boolean allSelected = true;
				for (JCheckBox checkBox : accountBoxes) {
					if (!checkBox.isSelected()) {
						allSelected = false;
					}
				}
				allAccounts.setSelected(allSelected);
			}

		}
		addListeners();
	}

	private void removeListeners() {
		for (JCheckBox box : accountBoxes) {
			box.removeItemListener(this);
		}
		allAccounts.removeItemListener(this);
	}

	private void addListeners() {
		for (JCheckBox box : accountBoxes) {
			box.addItemListener(this);
		}
		allAccounts.addItemListener(this);
	}

}
