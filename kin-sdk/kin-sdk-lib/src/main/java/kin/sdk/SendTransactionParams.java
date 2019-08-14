package kin.sdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import kin.base.Memo;
import kin.base.Operation;

import java.math.BigDecimal;
import java.util.List;

public class SendTransactionParams {

    // TODO: 2019-08-06 implement

    // Private Constructor, cannot init from outside, only by factory methods
    private SendTransactionParams() {

    }

    public static SendTransactionParams createSendPaymentParams(@NonNull String publicAddress,
                                                                @NonNull BigDecimal amount, int fee) {
        return null;
    }

    public static SendTransactionParams createSendPaymentParams(@NonNull String publicAddress,
                                                                @NonNull BigDecimal amount, int fee,
                                                                @Nullable String memo) {
        return null;
    }

    public List<Operation> operations() {
        return null;
    }

    public int fee() {
        return 0;
    }

    @Nullable
    public Memo memo() {
        return null;
    }

}
