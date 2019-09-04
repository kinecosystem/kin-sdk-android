package kin.sdk.internal.blockchain;

import kin.sdk.transactiondata.Transaction;

public class TransactionInternal extends Transaction {

    private final kin.base.Transaction baseTransaction;

    public TransactionInternal(kin.base.Transaction baseTransaction) {
        super(baseTransaction);
        this.baseTransaction = baseTransaction;
    }

    kin.base.Transaction baseTransaction() {
        return baseTransaction;
    }

}
