package kin.sdk.internal.data;

import kin.sdk.TransactionId;

public final class TransactionIdImpl implements TransactionId {

    private String id;

    public TransactionIdImpl(String id) {
        this.id = id;
    }

    @Override
    public String id() {
        return id;
    }
}
