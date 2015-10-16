package se.freedrikp.econview.accounts;

import java.util.ArrayList;

public class AccountHistory {
	private ArrayList<AccountEvent> history;
	
	public AccountHistory(){
		history = new ArrayList<AccountEvent>();
	}
	
	public boolean addEvent(AccountEvent acce){
		return history.add(acce);
	}
	
	public AccountEvent getAccountEvent(int index){
		return history.get(index);
	}
	
	public int size(){
		return history.size();
	}
}
