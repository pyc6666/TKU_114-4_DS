class BankAccount {
    private String accountId;
    private int balance;

    BankAccount(String accountId, int openingBalance) {
        this.accountId = accountId;
        this.balance = Math.max(0, openingBalance);
    }

    void deposit(int amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    boolean withdraw(int amount) {
        if (amount <= 0 || amount > balance) {
            return false;
        }
        balance -= amount;
        return true;
    }

    String getAccountId() {
        return accountId;
    }

    int getBalance() {
        return balance;
    }
}

public class BankAccountReview {
    public static void main(String[] args) {
        BankAccount account = new BankAccount("A101", 1000);

        account.deposit(500);
        System.out.println("提款 300：" + account.withdraw(300));
        System.out.println("提款 5000：" + account.withdraw(5000));
        System.out.println(account.getAccountId() + " 餘額：" +
            account.getBalance());
    }
}