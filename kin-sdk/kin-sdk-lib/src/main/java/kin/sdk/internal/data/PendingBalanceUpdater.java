package kin.sdk.internal.data;

import kin.sdk.Balance;

public interface PendingBalanceUpdater {

    // TODO: 2019-08-08 when reaching this task then need to figure out regarding the async or sync.
    Balance getPendingBalance();
}
