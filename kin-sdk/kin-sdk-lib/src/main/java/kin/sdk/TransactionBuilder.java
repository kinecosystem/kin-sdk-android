package kin.sdk;

import android.text.TextUtils;
import kin.base.*;
import kin.base.Transaction.Builder;

public class TransactionBuilder {

    private final Builder builder;
    private final KeyPair account;
    private final String appId;
    private Memo memo;

    /**
     * Construct a new transaction builder.
     *
     * @param account The source account for this transaction. This account is the account
     * @param sourceAccount This is the account that originates the transaction. The transaction must be signed by this account,
     * and the transaction fee must be paid by this account. The sequence number of this transaction is based off this account.
     * This account is the account who will use a sequence number. When build() is called, the account object's sequence number
     * will be incremented.
     *
     * @param appId is a 3-4 character string which is added to each transaction memo to identify your application.
     *      * appId must contain only digits and upper and/or lower case letters. String length must be 3 or 4.
     */
    TransactionBuilder(KeyPair account, TransactionBuilderAccount sourceAccount, String appId) {
        this.account = account;
        builder = new Builder(sourceAccount);
        this.appId = appId;
    }

    public int getOperationsCount() {
        return builder.getOperationsCount();
    }

    /**
     * <p>Adds a new operation to this transaction.</p>
     *
     * Transactions contain an arbitrary list of operations inside them. Typically there is just one operation,
     * but it’s possible to have multiple (up to 100).
     * Operations are executed in order as one ACID transaction, meaning that either all operations are applied or none are.
     * If any operation fails, the whole transaction fails. If operations are on accounts other than the source account,
     * then they require signatures of the accounts in question.
     *
     * @return Builder object so you can chain methods.
     * @see Operation
     */
    public TransactionBuilder addOperation(Operation operation) {
        builder.addOperation(operation);
        return this;
    }

    /**
     * Each transaction sets a fee that is paid by the source account.
     * If this fee is below the network minimum the transaction will fail.
     * The more operations in the transaction, the greater the required fee.
     * @param fee this transaction fee
     * @return Builder object so you can chain methods.
     */
    public TransactionBuilder setFee(int fee) {
        builder.addFee(fee);
        return this;
    }

    /**
     * Adds a memo to this transaction.
     * @param memo  is an optional object that contains optional extra information. It is the responsibility
     * of the client to interpret this value.
     * <br/>
     * <br/>
     * <p>Memos can be one of the following types:</p>
     * <ul>
     * <li>MEMO_TEXT : A string encoded using either ASCII or UTF-8, up to 21-bytes long (or 22-bytes long if your appId is 3 letters)</li>
     * <li>MEMO_ID : A 64 bit unsigned integer.</li>
     * <li>MEMO_HASH : A 32 byte hash.</li>
     * <li>MEMO_RETURN : A 32 byte hash intended to be interpreted as the hash of the transaction the sender is refunding.</li>
     * </ul>
     * @return Builder object so you can chain methods.
     * @see Memo
     */
    public TransactionBuilder setMemo(Memo memo) {
        this.memo = memo;
        return this;
    }

    /**
     * <p>Adds a time-bounds to this transaction.</p>
     *
     * Time-bounds is an optional.
     * <br />
     * it is a UNIX timestamp (in seconds), determined by ledger time, of a lower and upper bound of when this transaction will be valid.
     * If a transaction is submitted too early or too late, it will fail to make it into the transaction set. maxTime equal 0 means that it’s not set.
     *
     * @return Builder object so you can chain methods.
     * @see TimeBounds
     */
    public TransactionBuilder setTimeBounds(TimeBounds timeBounds) {
        builder.addTimeBounds(timeBounds);
        return this;
    }

    /**
     * Builds a transaction. It will increment sequence number of the source account.
     */
    public RawTransaction build() {
        if (memo instanceof MemoText) {
            String memoText = ((MemoText) memo).getText();
            Utils.validateMemo(memoText);
            builder.addMemo(Memo.text(Utils.addAppIdToMemo(appId, TextUtils.isEmpty(memoText) ? "" : memoText)));
        }

        Transaction baseTransaction = builder.build();
        if (baseTransaction.getFee() < 0) {
            throw new IllegalArgumentException("Fee can't be negative");
        }
        baseTransaction.sign(account);
        return new RawTransaction(baseTransaction);
    }


}
