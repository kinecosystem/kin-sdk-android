package kin.base;

public class AccountLedgerEntryChange extends LedgerEntryChange {

    private KeyPair accountID;
    private String balance;

    AccountLedgerEntryChange() {
    }

    public KeyPair getAccount() {
        return this.accountID;
    }

    public String getBalance() {
        return this.balance;
    }

    public static AccountLedgerEntryChange fromXdr(kin.base.xdr.AccountEntry xdr) {
        AccountLedgerEntryChange entry = new AccountLedgerEntryChange();
        entry.accountID = KeyPair.fromXdrPublicKey(xdr.getAccountID().getAccountID());
        entry.balance = Operation.fromXdrAmount(xdr.getBalance().getInt64());
        return entry;
    }


}
