package kin.sdk;

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
