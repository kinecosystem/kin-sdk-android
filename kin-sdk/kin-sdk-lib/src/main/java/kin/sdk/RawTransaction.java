package kin.sdk;

import kin.base.Memo;
import kin.base.Operation;

public class RawTransaction extends TransactionBase {

    RawTransaction(kin.base.Transaction baseTransaction) {
        super(baseTransaction);
    }

    /**
     * return the  memo of this transaction.
     * memo is an optional parameter the contain extra information
     */
    public Memo memo() {
        return baseTransaction().getMemo();
    }

    /**
     * return the list of operation in this transaction.
     */
    public Operation[] operations() {
        return baseTransaction().getOperations();
    }

}
