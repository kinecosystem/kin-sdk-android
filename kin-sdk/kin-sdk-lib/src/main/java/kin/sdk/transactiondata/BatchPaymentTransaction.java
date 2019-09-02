package kin.sdk.transactiondata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import kin.base.Operation;
import kin.base.Transaction;
import kin.sdk.internal.blockchain.TransactionInternal;
import kin.sdk.internal.data.PaymentOperationImpl;

public class BatchPaymentTransaction extends TransactionInternal {

    private final Transaction baseTransaction;

    public interface PaymentOperation {

        /**
         * Destination account public address.
         */
        String destinationPublicAddress();

        /**
         * Source account public address.
         */
        String sourcePublicAddress();

        /**
         * Payment amount in kin.
         */
        BigDecimal amount();
    }

    public BatchPaymentTransaction(kin.base.Transaction baseTransaction) {
        super(baseTransaction);
        this.baseTransaction = baseTransaction;
    }

    /**
     * @return the list of payment operations that this batch payment transaction consist of.
     * Can be empty.
     */
    public List<PaymentOperation> payments() {
        List<PaymentOperation> paymentOperations = new ArrayList<>();
        for (Operation o : baseTransaction.getOperations()) {
            if (o instanceof kin.base.PaymentOperation) {
                kin.base.PaymentOperation operation = (kin.base.PaymentOperation) o;
                PaymentOperation paymentOperation =
                        new PaymentOperationImpl(operation.getDestination().getAccountId(),
                                operation.getSourceAccount().getAccountId(), new BigDecimal(operation.getAmount()));
                paymentOperations.add(paymentOperation);
            }
        }
        return paymentOperations;
    }

    /**
     * @return the memo of this transaction.
     * In this case you will get a string representation of a memo as opposed to {@link RawTransaction#memo()}
     */
    public String memo() {
        return memo();
    }

}
