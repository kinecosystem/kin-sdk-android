package kin.sdk.transaction_data;

import java.math.BigDecimal;
import java.util.List;

public class BatchPaymentTransaction extends Transaction {

    public interface PaymentOperation {
        String source();

        String destination();

        BigDecimal amount();
    }


    public BatchPaymentTransaction(kin.base.Transaction baseTransaction) {
        super(baseTransaction);
    }

    public List<PaymentOperation> payments() {
        // TODO: 2019-08-04 implement
        return null;
    }

    public String memo() {
        // TODO: 2019-08-04 implement
        return null;
    }


}
