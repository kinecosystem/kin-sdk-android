package kin.sdk;

/**
 * This class wraps a transaction envelope xdr in base 64(transaction payload)
 * and a network passphrase(the network id as string). *
 * Those fields are necessary for the whitelist server in order to sign this transaction to be a whitelist transaction.
 */
public class WhitelistableTransaction {

    private final String transactionPayload;
    private final String networkPassphrase;

    public WhitelistableTransaction(String transactionPayload, String networkPassphrase) {
        this.transactionPayload = transactionPayload;
        this.networkPassphrase = networkPassphrase;
    }

    public String getTransactionPayload() {
        return transactionPayload;
    }

    public String getNetworkPassphrase() {
        return networkPassphrase;
    }

}
