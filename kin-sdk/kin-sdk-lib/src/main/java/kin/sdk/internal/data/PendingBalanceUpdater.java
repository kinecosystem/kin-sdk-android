package kin.sdk.internal.data;

import kin.sdk.Balance;

public interface PendingBalanceUpdater {

    // TODO: 2019-08-08 this should be async? it should probably take it from the blockchain...
    Balance getPendingBalance();
}
