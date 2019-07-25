package kin.sdk;

/**
 * This class wraps a transaction envelope xdr in base 64(transaction payload)
 * and a network passphrase(the network id as string). *
 * Those fields are necessary for the whitelist server in order to sign this transaction to be a whitelist transaction.
 */
public class WhitelistPayload {

    private final String transactionPayload;
    private final String networkPassphrase;

    public WhitelistPayload(String transactionPayload, String networkPassphrase) {
        this.transactionPayload = transactionPayload;
        this.networkPassphrase = networkPassphrase;
    }

    public String transactionPayload() {
        return transactionPayload;
    }

    public String networkPassphrase() {
        return networkPassphrase;
    }

}
