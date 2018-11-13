package kin.sdk;

public class WhitelistableTransaction {

    private final String transactionEnvalopXdrBase64;
    private final byte[] networkId;

    public WhitelistableTransaction(String transactionEnvalopXdrBase64, byte[] networkId) {
        this.transactionEnvalopXdrBase64 = transactionEnvalopXdrBase64;
        this.networkId = networkId;
    }

    public String getTransactionEnvalopXdrBase64() {
        return transactionEnvalopXdrBase64;
    }

    public byte[] getNetworkId() {
        return networkId;
    }
}
