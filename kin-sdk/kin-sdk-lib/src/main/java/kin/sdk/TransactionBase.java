package kin.sdk;

import java.util.List;
import kin.base.Transaction;
import kin.base.xdr.DecoratedSignature;

public class TransactionBase {

    private final Transaction baseTransaction;

    public TransactionBase(kin.base.Transaction baseTransaction) {
        this.baseTransaction = baseTransaction;
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

    public long sequenceNumber() {
        return baseTransaction.getSequenceNumber();
    }

    public String source() {
        return baseTransaction.getSourceAccount().getAccountId();
    }

    public List<DecoratedSignature> signatures() {
        return baseTransaction.getSignatures();
    }

}
