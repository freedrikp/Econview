package se.freedrikp.econview.accounts;

public class AccountEvent {
	public static String WITHDRAW = "Withdrawal";
	public static String DEPOSIT = "Deposit";
	private String type;
	private int amount;
	private int prevBalance;
	
	public AccountEvent(String type, int amount, int prevBalance){
		this.type = type;
		this.amount = amount;
		this.prevBalance = prevBalance;
	}

	public String getType() {
		return type;
	}

	public int getAmount() {
		return amount;
	}

	public int getPreviousBalance() {
		return prevBalance;
	}
	
}
