package kin.sdk;

public class WhitelistableTransaction {

    private final String transactionEnvelopXdrBase64;
    private final String networkPassphrase;

    public WhitelistableTransaction(String transactionEnvelopXdrBase64, String networkPassphrase) {
        this.transactionEnvelopXdrBase64 = transactionEnvelopXdrBase64;
        this.networkPassphrase = networkPassphrase;
    }

    public String getTransactionEnvelopXdrBase64() {
        return transactionEnvelopXdrBase64;
    }

    public String getNetworkPassphrase() {
        return networkPassphrase;
    }

}
