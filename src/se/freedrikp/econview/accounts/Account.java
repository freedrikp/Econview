package se.freedrikp.econview.accounts;

public class Account {
	private String name;
	private AccountHistory history;
	private int balance;
	
	public Account(String name, int balance){
		this.name = name;
		this.balance = balance;
		history = new AccountHistory();
	}
	
	public int deposit(int amount){
		history.addEvent(new AccountEvent(AccountEvent.DEPOSIT, amount, balance));
		balance += amount;
		return balance;
	}
	
	public int withdraw(int amount){
		history.addEvent(new AccountEvent(AccountEvent.WITHDRAW, amount, balance));
		balance -= amount;
		return balance;
	}

	public String getName() {
		return name;
	}

	public AccountHistory getHistory() {
		return history;
	}

	public int getBalance() {
		return balance;
	}
	
	
}
