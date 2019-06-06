package kin.sdk;

import java.io.IOException;
import kin.base.KeyPair;
import kin.base.Memo;
import kin.base.Network;
import kin.sdk.exception.DecodeTransactionException;

public class Transaction {

    /**
     * The transaction hash
     */
    private final TransactionId id;

    private final kin.base.Transaction baseTransaction;

    public Transaction(TransactionId id, kin.base.Transaction baseTransaction) {
        this.id = id;
        this.baseTransaction = baseTransaction;
    }

    public static Transaction decodeTransaction(String transactionEnvelope) throws DecodeTransactionException {
        try {
            kin.base.Transaction transaction = kin.base.Transaction.fromEnvelopeXdr(transactionEnvelope);
            TransactionId id = new TransactionIdImpl(Utils.byteArrayToHex(transaction.hash()));
            return new Transaction(id, transaction);
        } catch (IOException e) {
            throw new DecodeTransactionException(e.getMessage(), e.getCause());
        }

    }

    public KeyPair getSource() {
        return baseTransaction.getSourceAccount();
    }

    public int getFee() {
        return baseTransaction.getFee();
    }

    public Memo getMemo() {
        return baseTransaction.getMemo();
    }

    public TransactionId getId() {
        return id;
    }

    kin.base.Transaction getBaseTransaction() {
        return baseTransaction;
    }

    public WhitelistableTransaction getWhitelistableTransaction() {
        return new WhitelistableTransaction(baseTransaction.toEnvelopeXdrBase64(),
            Network.current().getNetworkPassphrase());
    }

    public String getTransactionEnvelope() {
        return baseTransaction.toEnvelopeXdrBase64();
    }

    public void addSignature(KinAccount account) {
        baseTransaction.sign(((KinAccountImpl) account).getKeyPair());
    }

}
