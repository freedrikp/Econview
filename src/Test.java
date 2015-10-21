import se.freedrikp.econview.accounts.Account;
import se.freedrikp.econview.accounts.AccountList;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	AccountList accs = new AccountList();
	Account acc = new Account("Kort", 0);
	accs.addAccount(acc);
	System.out.println();

	}

}
