package se.freedrikp.econview.gui;

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

import se.freedrikp.econview.database.Database;

public class AccountSelectorPanel extends JPanel implements Observer {
	private Database db;
	private boolean startState;
	private boolean includeTotal;
	private JCheckBox allAccounts;
	private JCheckBox total;
	private JCheckBox[] selectedAccounts;

	public AccountSelectorPanel(Database db, boolean startState,
			boolean includeTotal) {
		super();
		this.db = db;
		db.addObserver(this);
		this.startState = startState;
		this.includeTotal = includeTotal;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		allAccounts = new JCheckBox(Utilities.getString("ALL_ACCOUNTS"),
				startState);
		// allAccounts.setAlignmentX(Component.CENTER_ALIGNMENT);

		allAccounts.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				boolean select = e.getStateChange() == ItemEvent.SELECTED;
					for (JCheckBox checkBox : selectedAccounts) {
						checkBox.setSelected(select);
						// update(db,null);
					}
				}

		});
		if (includeTotal) {
			total = new JCheckBox(Utilities.getString("TOTAL_ACCOUNT_NAME"),
					startState);
		}
		// total.setAlignmentX(Component.CENTER_ALIGNMENT);
		update(db, null);
	}

	public void update(Observable o, Object arg) {
		removeAll();

		List<String> accounts = db.getAccountNames();
		HashSet<String> oldSelectedAccounts = new HashSet<String>();
		if (selectedAccounts != null) {
			for (JCheckBox checkBox : selectedAccounts) {
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

		boolean first = selectedAccounts == null;

		selectedAccounts = new JCheckBox[accounts.size()];
		for (int i = 0; i < accounts.size(); i++) {
			String account = accounts.get(i);
			boolean state = first ? startState : oldSelectedAccounts
					.contains(account);
			selectedAccounts[i] = new JCheckBox(account, state);
//			selectedAccounts[i].addItemListener(new ItemListener() {
//				public void itemStateChanged(ItemEvent e) {
//					if (e.getStateChange() == ItemEvent.DESELECTED) {
//						allAccounts.setSelected(false);
//					} else {
////						boolean allSelected = true;
////						for (JCheckBox checkBox : selectedAccounts) {
////							if (!checkBox.isSelected()) {
////								allSelected = false;
////							}
////						}
////						allAccounts.setSelected(allSelected);
//					}
//
//				}
//			});
			add(selectedAccounts[i]);
		}
	}

	public Collection<String> getSelectedAccounts() {
		Set<String> accounts = new HashSet<String>();
		for (JCheckBox checkBox : selectedAccounts) {
			if (checkBox.isSelected()) {
				accounts.add(checkBox.getText());
			}
		}
		return accounts;
	}

	public boolean isTotalSelected() {
		return includeTotal && total.isSelected();
	}

}
