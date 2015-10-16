package se.freedrikp.econview.accounts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class AccountList {
	private ArrayList<Account> accs;
	private File f = new File("econdata.dat");

	public AccountList() {
		accs = new ArrayList<Account>();
	}

	public boolean addAccount(Account acc) {
		return accs.add(acc);
	}

	public boolean removeAccount(int index) {
		return accs.remove(accs.get(index));
	}

	public Account getAccount(int index) {
		return accs.get(index);
	}
	
	public void save(){
		try {
			ObjectOutputStream out = new ObjectOutputStream(
					new FileOutputStream(f));
			out.writeObject(accs);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void load(){
		try {
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(f));
			accs = (ArrayList<Account>) in.readObject();
		} catch (Exception e) {
			try {
				f.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(1);
			}
		}
	}

}
