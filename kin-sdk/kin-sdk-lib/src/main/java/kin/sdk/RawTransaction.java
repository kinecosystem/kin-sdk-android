package kin.sdk;

import kin.base.Memo;
import kin.base.Operation;

public class RawTransaction extends TransactionBase {

    RawTransaction(kin.base.Transaction baseTransaction) {
        super(baseTransaction);
    }

    /**
     * return the  memo of this transaction.
     * Memo is an optional object that contains optional extra information. It is the responsibility of the client to interpret
     * this value.
     *
     * <p>Memos can be one of the following types:</p>
     * <ul>
     * <li>MEMO_TEXT : A string encoded using either ASCII or UTF-8, up to 21-bytes long or 22-bytes long if your appId is 3 letters</li>
     * <li>MEMO_ID : A 64 bit unsigned integer.</li>
     * <li>MEMO_HASH : A 32 byte hash.</li>
     * <li>MEMO_RETURN : A 32 byte hash intended to be interpreted as the hash of the transaction the sender is refunding.</li>
     * </ul>
     */
    public Memo memo() {
        return baseTransaction().getMemo();
    }

    /**
     * return the list of operation in this transaction.
     *
     * Transactions contain an arbitrary list of operations inside them. Typically there is just one operation,
     * but it’s possible to have multiple (up to 100).
     * Operations are executed in order as one ACID transaction, meaning that either all operations are applied or none are.
     * If any operation fails, the whole transaction fails. If operations are on accounts other than the source account,
     * then they require signatures of the accounts in question.
     */
    public Operation[] operations() {
        return baseTransaction().getOperations();
    }

}
