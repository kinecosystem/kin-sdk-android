package kin.sdk;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import kin.base.KeyPair;
import kin.sdk.exception.AccountDeletedException;
import kin.sdk.internal.account.KinAccountImpl;
import kin.sdk.internal.blockchain.AccountInfoRetriever;
import kin.sdk.internal.blockchain.TransactionSender;
import kin.sdk.internal.blockchain.events.BlockchainEventsCreator;
import kin.sdk.internal.data.BalanceImpl;
import kin.sdk.internal.data.TransactionIdImpl;
import kin.sdk.queue.PaymentQueue;
import kin.sdk.transactiondata.PaymentTransaction;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KinAccountImplTest {

    @Mock
    private TransactionSender mockTransactionSender;
    @Mock
    private AccountInfoRetriever mockAccountInfoRetriever;
    @Mock
    private BlockchainEventsCreator mockBlockchainEventsCreator;
    @Mock
    private PaymentQueue mockPaymentQueue;

    private KinAccountImpl kinAccount;
    private KeyPair expectedRandomAccount;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    private void initWithRandomAccount() {
        expectedRandomAccount = KeyPair.random();
        kinAccount = new KinAccountImpl(expectedRandomAccount, new FakeBackupRestore(),
                mockTransactionSender, mockAccountInfoRetriever, mockBlockchainEventsCreator,
                mockPaymentQueue);
    }

    @Test
    public void getPublicAddress_ExistingAccount() throws Exception {
        initWithRandomAccount();

        assertEquals(expectedRandomAccount.getAccountId(), kinAccount.getPublicAddress());
    }

    @Test
    public void sendTransactionSync() throws Exception {
        initWithRandomAccount();

        String expectedAccountId = "GDKJAMCTGZGD6KM7RBEII6QUYAHQQUGERXKM3ESHBX2UUNTNAVNB3OGX";
        BigDecimal expectedAmount = new BigDecimal("12.2");
        TransactionId expectedTransactionId = new TransactionIdImpl("myId");

        when(mockTransactionSender.sendTransaction((PaymentTransaction) any())).thenReturn(expectedTransactionId);

        PaymentTransaction transaction = kinAccount.buildTransactionSync(expectedAccountId,
                expectedAmount, 100);
        TransactionId transactionId = kinAccount.sendTransactionSync(transaction);

        verify(mockTransactionSender).sendTransaction(transaction);
        assertEquals(expectedTransactionId, transactionId);
    }

    @Test
    public void sendTransactionSync_WithMemo() throws Exception {
        initWithRandomAccount();

        String expectedAccountId = "GDKJAMCTGZGD6KM7RBEII6QUYAHQQUGERXKM3ESHBX2UUNTNAVNB3OGX";
        BigDecimal expectedAmount = new BigDecimal("12.2");
        TransactionId expectedTransactionId = new TransactionIdImpl("myId");
        String memo = "Dummy Memo";

        when(mockTransactionSender.sendTransaction((PaymentTransaction) any())).thenReturn(expectedTransactionId);

        PaymentTransaction transaction = kinAccount.buildTransactionSync(expectedAccountId,
                expectedAmount, 100, memo);
        TransactionId transactionId = kinAccount.sendTransactionSync(transaction);

        verify(mockTransactionSender).sendTransaction(transaction);
        assertEquals(expectedTransactionId, transactionId);
    }

    @Test
    public void getBalanceSync() throws Exception {
        initWithRandomAccount();

        Balance expectedBalance = new BalanceImpl(new BigDecimal("11.0"));
        when(mockAccountInfoRetriever.getBalance(anyString())).thenReturn(expectedBalance);

        Balance balance = kinAccount.getBalanceSync();

        assertEquals(expectedBalance, balance);
        verify(mockAccountInfoRetriever).getBalance(expectedRandomAccount.getAccountId());
    }

    @Test
    public void getStatusSync() throws Exception {
        initWithRandomAccount();

        when(mockAccountInfoRetriever.getStatus(anyString())).thenReturn(AccountStatus.CREATED);

        int status = kinAccount.getStatusSync();

        assertEquals(AccountStatus.CREATED, status);
        verify(mockAccountInfoRetriever).getStatus(expectedRandomAccount.getAccountId());
    }

    @Test(expected = AccountDeletedException.class)
    public void sendTransactionSync_DeletedAccount_Exception() throws Exception {
        initWithRandomAccount();
        kinAccount.markAsDeleted();

        PaymentTransaction transaction = kinAccount.buildTransactionSync(
                "GDKJAMCTGZGD6KM7RBEII6QUYAHQQUGERXKM3ESHBX2UUNTNAVNB3OGX",
                new BigDecimal("12.2"), 100);
        kinAccount.sendTransactionSync(transaction);
    }

    @Test(expected = AccountDeletedException.class)
    public void sendWhitelistTransaction_DeletedAccount_Exception() throws Exception {
        initWithRandomAccount();
        kinAccount.markAsDeleted();

        String whitelist = "whitelist test string";
        kinAccount.sendWhitelistTransactionSync(whitelist);
    }

    @Test(expected = AccountDeletedException.class)
    public void getBalanceSync_DeletedAccount_Exception() throws Exception {
        initWithRandomAccount();

        kinAccount.markAsDeleted();
        kinAccount.getBalanceSync();
    }

    @Test(expected = AccountDeletedException.class)
    public void getStatusSync_DeletedAccount_Exception() throws Exception {
        initWithRandomAccount();

        kinAccount.markAsDeleted();
        kinAccount.getStatusSync();
    }

    @Test
    public void getPublicAddress_DeletedAccount_Empty() throws Exception {
        initWithRandomAccount();
        kinAccount.markAsDeleted();

        assertNull(kinAccount.getPublicAddress());
    }
}