package kin.sdk;

import java.io.IOException;
import kin.base.KeyPair;
import kin.base.Memo;
import kin.base.Network;
import kin.sdk.exception.DecodeTransactionException;

public class Transaction {

    private final kin.base.Transaction baseTransaction;

    public Transaction(kin.base.Transaction baseTransaction) {
        this.baseTransaction = baseTransaction;
    }

    /**
     * Creates a <code>Transaction</code> instance from previously build <code>TransactionEnvelope</code>
     *
     * @param transactionEnvelope a Base-64 encoded <code>TransactionEnvelope</code>
     */
    public static Transaction decodeTransaction(String transactionEnvelope) throws DecodeTransactionException {
        try {
            kin.base.Transaction transaction = kin.base.Transaction.fromEnvelopeXdr(transactionEnvelope);
            return new Transaction(transaction);
        } catch (IOException e) {
            throw new DecodeTransactionException(e.getMessage(), e.getCause());
        }

    }

    public KeyPair getSource() {
        return baseTransaction.getSourceAccount();
    }

    /**
     * Returns fee paid for transaction in kin base unit (1 base unit = 0.00001 KIN).
     */
    public int getFee() {
        return baseTransaction.getFee();
    }

    public Memo getMemo() {
        return baseTransaction.getMemo();
    }

    /**
     * return The transaction hash
     */
    public TransactionId getId() {
        return new TransactionIdImpl(Utils.byteArrayToHex(baseTransaction.hash()));
    }

    kin.base.Transaction getBaseTransaction() {
        return baseTransaction;
    }

    /**
     * see {@link WhitelistableTransaction} for more information on a whitelistable transaction</p>
     */
    public WhitelistableTransaction getWhitelistableTransaction() {
        return new WhitelistableTransaction(baseTransaction.toEnvelopeXdrBase64(),
            Network.current().getNetworkPassphrase());
    }

    /**
     * Returns base64-encoded TransactionEnvelope object.
     * Transaction need to have at least one signature.
     */
    public String getTransactionEnvelope() {
        return baseTransaction.toEnvelopeXdrBase64();
    }

    /**
     * Adds a new signature to this transaction.
     * @param account {@link KinAccount} object which is the account who we add his signature.
     */
    public void addSignature(KinAccount account) {
        baseTransaction.sign(((KinAccountImpl) account).getKeyPair());
    }

}
