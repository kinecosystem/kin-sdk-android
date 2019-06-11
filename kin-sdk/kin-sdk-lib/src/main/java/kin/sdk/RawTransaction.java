package kin.sdk;

import kin.base.Memo;
import kin.base.Operation;

public class RawTransaction extends TransactionBase {

    public RawTransaction(kin.base.Transaction baseTransaction) {
        super(baseTransaction);
    }

    public Memo memo() {
        return baseTransaction().getMemo();
    }

    public Operation[] operations() {
        return baseTransaction().getOperations();
    }

}
