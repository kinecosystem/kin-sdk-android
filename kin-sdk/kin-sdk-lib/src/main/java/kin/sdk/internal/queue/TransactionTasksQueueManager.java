package kin.sdk.internal.queue;

import kin.sdk.SendTransactionParams;
import kin.sdk.queue.PendingPayment;

import java.util.List;

public interface TransactionTasksQueueManager {

    // TODO: 2019-08-15 add java docs

    void enqueue(List<PendingPayment> queueToSend);

    void enqueue(SendTransactionParams sendTransactionParams);
}
