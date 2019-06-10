package kin.sdk;

import java.io.IOException;
import java.util.List;
import kin.base.KeyPair;
import kin.base.Memo;
import kin.base.Network;
import kin.base.Operation;
import kin.base.TimeBounds;
import kin.base.xdr.DecoratedSignature;
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

    public KeyPair source() {
        return baseTransaction.getSourceAccount();
    }

    /**
     * Returns fee paid for transaction in kin base unit (1 base unit = 0.00001 KIN).
     */
    public int fee() {
        return baseTransaction.getFee();
    }

    public Memo memo() {
        return baseTransaction.getMemo();
    }

    /**
     * return The transaction hash
     */
    public TransactionId id() {
        return new TransactionIdImpl(Utils.byteArrayToHex(baseTransaction.hash()));
    }

    kin.base.Transaction baseTransaction() {
        return baseTransaction;
    }

    /**
     * see {@link WhitelistableTransaction} for more information on a whitelistable transaction</p>
     */
    public WhitelistableTransaction whitelistableTransaction() {
        return new WhitelistableTransaction(baseTransaction.toEnvelopeXdrBase64(),
            Network.current().getNetworkPassphrase());
    }

    /**
     * Returns base64-encoded TransactionEnvelope object.
     * Transaction need to have at least one signature.
     */
    public String transactionEnvelope() {
        return baseTransaction.toEnvelopeXdrBase64();
    }

    public long sequenceNumber() {
        return baseTransaction.getSequenceNumber();
    }

    public Operation[] operations() {
        return baseTransaction.getOperations();
    }

    public List<DecoratedSignature> signatures() {
        return baseTransaction.getSignatures();
    }

    /**
     * @return TimeBounds, or null (representing no time restrictions)
     */
    public TimeBounds timeBounds() {
        return baseTransaction.getTimeBounds();
    }

    /**
     * Adds a new signature to this transaction.
     * @param account {@link KinAccount} object which is the account who we add his signature.
     */
    public void addSignature(KinAccount account) {
        baseTransaction.sign(((KinAccountImpl) account).getKeyPair());
    }

}
