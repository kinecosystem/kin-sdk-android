package kin.sdk;

import java.io.IOException;
import kin.base.Memo;
import kin.base.Network;
import kin.base.Operation;
import kin.base.TimeBounds;
import kin.sdk.exception.DecodeTransactionException;

public class RawTransaction extends TransactionBase {

    public RawTransaction(kin.base.Transaction baseTransaction) {
        super(baseTransaction);
    }

    /**
     * Creates a <code>Transaction</code> instance from previously build <code>TransactionEnvelope</code>
     *
     * @param transactionEnvelope a Base-64 encoded <code>TransactionEnvelope</code>
     */
    public static RawTransaction decodeTransaction(String transactionEnvelope) throws DecodeTransactionException {
        try {
            kin.base.Transaction transaction = kin.base.Transaction.fromEnvelopeXdr(transactionEnvelope);
            return new RawTransaction(transaction);
        } catch (IOException e) {
            throw new DecodeTransactionException(e.getMessage(), e.getCause());
        }
    }

    public Memo memo() {
        return baseTransaction().getMemo();
    }

    /**
     * see {@link WhitelistableTransaction} for more information on a whitelistable transaction</p>
     */
    public WhitelistableTransaction whitelistableTransaction() {
        return new WhitelistableTransaction(baseTransaction().toEnvelopeXdrBase64(),
            Network.current().getNetworkPassphrase());
    }

    /**
     * Returns base64-encoded TransactionEnvelope object.
     * Transaction need to have at least one signature.
     */
    public String transactionEnvelope() {
        return baseTransaction().toEnvelopeXdrBase64();
    }

    public Operation[] operations() {
        return baseTransaction().getOperations();
    }

    /**
     * @return TimeBounds, or null (representing no time restrictions)
     */
    public TimeBounds timeBounds() {
        return baseTransaction().getTimeBounds();
    }

    /**
     * Adds a new signature to this transaction.
     * @param account {@link KinAccount} object which is the account who we add his signature.
     */
    public void addSignature(KinAccount account) {
        baseTransaction().sign(((KinAccountImpl) account).getKeyPair());
    }

}
