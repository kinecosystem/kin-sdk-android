package kin.sdk.transaction_data;

import kin.base.*;
import kin.base.xdr.DecoratedSignature;
import kin.sdk.KinAccount;
import kin.sdk.TransactionId;
import kin.sdk.WhitelistPayload;
import kin.sdk.exception.DecodeTransactionException;
import kin.sdk.internal.Utils;
import kin.sdk.internal.account.KinAccountImpl;
import kin.sdk.internal.data.TransactionIdImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public abstract class Transaction {

    private final kin.base.Transaction baseTransaction;

    public Transaction(kin.base.Transaction baseTransaction) {
        this.baseTransaction = baseTransaction;
    }

    /**
     * Creates a <code>RawTransaction</code> or a <code>PaymentTransaction</code> instance from previously build
     * <code>TransactionEnvelope</code> string representation.
     *
     * <p>Once a transaction is ready to be signed, the transaction object is wrapped in an object called
     * a Transaction Envelope, which contains the transaction as well as a set of signatures.
     * A Transaction Envelope can be represented as a string base-64 encoded.</p>
     *
     * @param transactionEnvelope a Base-64 encoded <code>TransactionEnvelope</code>
     * @return a Transaction instance from a previously build transaction envelope.
     */
    public static Transaction decodeTransaction(String transactionEnvelope) throws DecodeTransactionException {
        try {
            kin.base.Transaction transaction = kin.base.Transaction.fromEnvelopeXdr(transactionEnvelope);
            Operation[] operations = transaction.getOperations();
            int paymentOperationCount = 0;
            PaymentOperation paymentOperation = null;
            for (Operation operation : operations) {
                if (operation instanceof PaymentOperation) {
                    paymentOperationCount++;
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
     * <p> See {@link Transaction#decodeTransaction(String)} for more details</p>
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

    public kin.base.Transaction baseTransaction() {
        return baseTransaction;
    }

    /**
     * Each transaction sets a fee that is paid by the source account.
     * If this fee is below the network minimum the transaction will fail.
     * The more operations in the transaction, the greater the required fee.
     *
     * @return the fee paid for transaction in kin base unit which is quark (1 quark = 0.00001 KIN).
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
     * Each transaction has a sequence number. Transactions follow a strict ordering rule when it comes to processing
     * of transactions per account. For the transaction to be valid, the sequence number must be 1 greater than the
     * sequence number stored in the source account entry when the transaction is applied. As the transaction is
     * applied, the source account’s stored sequence number is incremented by 1 before applying operations. If the
     * sequence number on the account is 4, then the incoming transaction should have a sequence number of 5. After
     * the transaction is applied, the sequence number on the account is bumped to 5.
     * <br/>
     * <br/>
     * <b>Note</b> that if several transactions with the same source account make it into the same transaction set,
     * they are
     * ordered and applied according to sequence number. For example, if 3 transactions are submitted and the account
     * is at sequence number 5, the transactions must have sequence numbers 6, 7, and 8.
     *
     * @return the sequence number of this transaction.
     */
    public long sequenceNumber() {
        return baseTransaction.getSequenceNumber();
    }

    /**
     * Source Account is the account that originates the transaction. The transaction must be signed by this account,
     * and the transaction fee must be paid by this account. The sequence number of this transaction is based
     * off this account.
     *
     * @return the source account public address
     */
    public String source() {
        return baseTransaction.getSourceAccount().getAccountId();
    }

    /**
     * Time-bounds is an optional.
     * <br /> it is a UNIX timestamp (in seconds), determined by ledger time, of a lower and upper bound of
     * when this transaction will be valid.
     * If a transaction is submitted too early or too late, it will fail to make it into the transaction set.
     * maxTime equal 0 means that it’s not set.
     *
     * @return the time bounds of this transaction, or null (representing no time restrictions)
     */
    public TimeBounds timeBounds() {
        return baseTransaction.getTimeBounds();
    }

    /**
     * @return the list of signatures (see {@link Transaction#addSignature(KinAccount)} for more information)
     */
    public List<DecoratedSignature> signatures() {
        return baseTransaction.getSignatures();
    }

    /**
     * Once a transaction is ready to be signed, the transaction object is wrapped in an object called
     * a Transaction Envelope, which contains the transaction as well as a set of signatures.
     * A Transaction Envelope can be represented as a string base-64 encoded.
     *
     * @return base64-encoded TransactionEnvelope object.
     */
    public String transactionEnvelope() {
        return baseTransaction.toEnvelopeXdrBase64();
    }

    /**
     * see {@link WhitelistPayload} for more information</p>
     */
    public WhitelistPayload whitelistPayload() {
        return new WhitelistPayload(baseTransaction.toEnvelopeXdrBase64(),
                Network.current().getNetworkPassphrase());
    }

    /**
     * <p>Adds a new signature to this transaction.</p>
     * <p>Transactions always need authorization from at least one public key in order to be considered valid.
     * Generally, transactions only need authorization from the public key of the source account.</p>
     * <br />
     * <p>Up to 20 signatures can be attached to a transaction. See next section for more information. A transaction is
     * considered invalid if it includes signatures that aren’t needed to authorize the transaction—superfluous
     * signatures aren’t allowed.
     * Signatures are required to authorize operations and to authorize changes to the source account (fee and
     * sequence number).</p>
     * <br />
     * <p>In two cases, a transaction may need more than one signature. If the transaction has operations that
     * affect more than one account, it will need authorization from every account in question.
     * A transaction will also need additional signatures if the account associated with the transaction has multiple
     * public keys.</p>
     * <p>Currently we use the ed25519 signature scheme</p>
     *
     * @param account {@link KinAccount} object which is the account who we add his signature.
     */
    public void addSignature(KinAccount account) {
        baseTransaction().sign(((KinAccountImpl) account).getKeyPair());
    }

}
