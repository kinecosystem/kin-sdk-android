package kin.sdk;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kin.base.KeyPair;
import kin.sdk.exception.AccountDeletedException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class KinAccountImplTest {

    @Mock
    private TransactionSender mockTransactionSender;
    @Mock
    private AccountInfoRetriever mockAccountInfoRetriever;
    @Mock
    private BlockchainEventsCreator mockBlockchainEventsCreator;
    private KinAccountImpl kinAccount;
    private KeyPair expectedRandomAccount;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    private void initWithRandomAccount() {
        expectedRandomAccount = KeyPair.random();
        kinAccount = new KinAccountImpl(expectedRandomAccount, new FakeBackupRestore(), mockTransactionSender,
            mockAccountInfoRetriever, mockBlockchainEventsCreator);
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

        when(mockTransactionSender.sendTransaction((RawTransaction) any())).thenReturn(expectedTransactionId);

        PaymentTransaction transaction = kinAccount.buildPaymentTransactionSync(expectedAccountId, expectedAmount, 100);
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

        when(mockTransactionSender.sendTransaction((RawTransaction) any())).thenReturn(expectedTransactionId);

        PaymentTransaction transaction = kinAccount.buildPaymentTransactionSync(expectedAccountId, expectedAmount, 100, memo);
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
    public void getAggregatedBalanceSync() throws Exception {
        initWithRandomAccount();

        Balance expectedAggregatedBalance = new BalanceImpl(new BigDecimal("150.0"));
        when(mockAccountInfoRetriever.getAggregatedBalance(anyString())).thenReturn(expectedAggregatedBalance);

        Balance balance = kinAccount.getAggregatedBalanceSync();

        assertEquals(expectedAggregatedBalance, balance);
        verify(mockAccountInfoRetriever).getAggregatedBalance(expectedRandomAccount.getAccountId());
    }

    @Test
    public void getControlledAccountsSync() throws Exception {
        initWithRandomAccount();

        ControlledAccount controlledAccount1 = new ControlledAccount(new BalanceImpl(new BigDecimal("150.0")),
            "first account public Address");
        ControlledAccount controlledAccount2 = new ControlledAccount(new BalanceImpl(new BigDecimal("10.0")),
            "second account public Address");
        ControlledAccount controlledAccount3 = new ControlledAccount(new BalanceImpl(new BigDecimal("55.0")),
            "third account public Address");
        List<ControlledAccount> expectedControlledAccounts = new ArrayList<>();
        expectedControlledAccounts.add(controlledAccount1);
        expectedControlledAccounts.add(controlledAccount2);
        expectedControlledAccounts.add(controlledAccount3);

        when(mockAccountInfoRetriever.getControlledAccounts(anyString())).thenReturn(expectedControlledAccounts);

        List<ControlledAccount> controlledAccounts = kinAccount.getControlledAccountsSync();

        assertThat(expectedControlledAccounts, equalTo(controlledAccounts));
        verify(mockAccountInfoRetriever).getControlledAccounts(expectedRandomAccount.getAccountId());
    }

    @Test
    public void getAccountDataSync() throws Exception {
        initWithRandomAccount();

        Map<String, String> data = new HashMap<>();
        data.put("public address 1", "package id 1");
        data.put("public address 2", "package id 2");
        data.put("public address 3", "package id 3");
        AccountData expectedAccountData = new AccountData(expectedRandomAccount.getAccountId(),
            123456789L, "paging token,", 1,
            null, null, null, null, data);

        when(mockAccountInfoRetriever.getAccountData(anyString())).thenReturn(expectedAccountData);

        AccountData accountData = kinAccount.getAccountDataSync();

        assertThat(expectedAccountData, equalTo(accountData));
        verify(mockAccountInfoRetriever).getAccountData(expectedRandomAccount.getAccountId());
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

        PaymentTransaction transaction = kinAccount
            .buildPaymentTransactionSync("GDKJAMCTGZGD6KM7RBEII6QUYAHQQUGERXKM3ESHBX2UUNTNAVNB3OGX", new BigDecimal("12.2"),
                100);
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

    @Test(expected = AccountDeletedException.class)
    public void getAggregatedBalanceSync_DeletedAccount_Exception() throws Exception {
        initWithRandomAccount();
        kinAccount.markAsDeleted();

        kinAccount.getAggregatedBalanceSync();
    }

    @Test(expected = AccountDeletedException.class)
    public void getControlledAccountsSync_DeletedAccount_Exception() throws Exception {
        initWithRandomAccount();
        kinAccount.markAsDeleted();

        kinAccount.getControlledAccountsSync();

    }

    @Test(expected = AccountDeletedException.class)
    public void getAccountDataSync_DeletedAccount_Exception() throws Exception {
        initWithRandomAccount();
        kinAccount.markAsDeleted();

        kinAccount.getAccountDataSync();
    }

    @Test
    public void getPublicAddress_DeletedAccount_Empty() throws Exception {
        initWithRandomAccount();
        kinAccount.markAsDeleted();

        assertNull(kinAccount.getPublicAddress());
    }
}