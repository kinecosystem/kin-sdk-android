package kin.sdk;

/**
 * This class represents a controlled account. Controlled Account is an account that other account has also control over
 * him, for example, to send transactions with a payment operation on behalf of him. The account that has the control is
 * often called the master account.
 * Master account can control more than one account and can sign for another.
 */
public class ControlledAccount {

    private Balance balance;
    private String publicAddress;

    ControlledAccount(Balance balance, String publicAddress) {
        this.balance = balance;
        this.publicAddress = publicAddress;
    }

    public Balance getBalance() {
        return balance;
    }

    public String getPublicAddress() {
        return publicAddress;
    }

}
