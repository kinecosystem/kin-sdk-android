package kin.sdk;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import kin.base.MemoText;
import kin.base.Network;
import kin.base.Operation;
import kin.base.PaymentOperation;
import kin.base.TimeBounds;
import kin.base.Transaction;
import kin.base.xdr.DecoratedSignature;
import kin.sdk.exception.DecodeTransactionException;

public abstract class TransactionBase {

    private final Transaction baseTransaction;

    TransactionBase(kin.base.Transaction baseTransaction) {
        this.baseTransaction = baseTransaction;
    }

    /**
     * Creates a <code>RawTransaction</code> or a <code>PaymentTransaction</code> instance from previously build <code>TransactionEnvelope</code>
     *
     * @param transactionEnvelope a Base-64 encoded <code>TransactionEnvelope</code>
     */
    public static TransactionBase decodeTransaction(String transactionEnvelope) throws DecodeTransactionException {
        try {
            kin.base.Transaction transaction = kin.base.Transaction.fromEnvelopeXdr(transactionEnvelope);
            Operation[] operations = transaction.getOperations();
            int paymentOperationCount = 0;
            PaymentOperation paymentOperation = null;
            for (Operation operation : operations) {
                if (operation instanceof PaymentOperation) {
                    paymentOperationCount ++;
                    paymentOperation = (PaymentOperation) operation;
                }
            }
            if (paymentOperationCount == 1) {
                return new PaymentTransaction(transaction, paymentOperation.getDestination().getAccountId(),
                    new BigDecimal(paymentOperation.getAmount()), ((MemoText) transaction.getMemo()).getText());
            } else {
                return new RawTransaction(transaction);
            }
        } catch (IOException e) {
            throw new DecodeTransactionException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Creates a <code>RawTransaction</code> instance from previously build <code>TransactionEnvelope</code>
     *
     * @param transactionEnvelope a Base-64 encoded <code>TransactionEnvelope</code>
     */
    public static RawTransaction decodeRawTransaction(String transactionEnvelope) throws DecodeTransactionException {
        try {
            kin.base.Transaction transaction = kin.base.Transaction.fromEnvelopeXdr(transactionEnvelope);
            return new RawTransaction(transaction);
        } catch (IOException e) {
            throw new DecodeTransactionException(e.getMessage(), e.getCause());
        }
    }

    kin.base.Transaction baseTransaction() {
        return baseTransaction;
    }

    /**
     * Returns fee paid for transaction in kin base unit (1 base unit = 0.00001 KIN).
     */
    public int fee() {
        return baseTransaction.getFee();
    }

    /**
     * return The transaction hash
     */
    public TransactionId id() {
        return new TransactionIdImpl(Utils.byteArrayToHex(baseTransaction.hash()));
    }

    /**
     * @return the sequence number of this transaction.
     */
    public long sequenceNumber() {
        return baseTransaction.getSequenceNumber();
    }

    /**
     * @return the source account public address
     */
    public String source() {
        return baseTransaction.getSourceAccount().getAccountId();
    }

    /**
     * @return the time bounds of this transaction, or null (representing no time restrictions)
     */
    public TimeBounds timeBounds() {
        return baseTransaction.getTimeBounds();
    }

    /**
     * @return the list of signatures
     */
    public List<DecoratedSignature> signatures() {
        return baseTransaction.getSignatures();
    }

    /**
     * Returns base64-encoded TransactionEnvelope object.
     * Transaction need to have at least one signature.
     */
    public String transactionEnvelope() {
        return baseTransaction.toEnvelopeXdrBase64();
    }

    /**
     * see {@link WhitelistableTransaction} for more information on a whitelistable transaction</p>
     */
    public WhitelistableTransaction whitelistableTransaction() {
        return new WhitelistableTransaction(baseTransaction.toEnvelopeXdrBase64(),
            Network.current().getNetworkPassphrase());
    }

    /**
     * Adds a new signature to this transaction.
     * @param account {@link KinAccount} object which is the account who we add his signature.
     */
    public void addSignature(KinAccount account) {
        baseTransaction().sign(((KinAccountImpl) account).getKeyPair());
    }

}
