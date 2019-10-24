package kin.sdk;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import kin.base.Account;
import kin.base.AssetTypeNative;
import kin.base.KeyPair;
import kin.base.PaymentOperation;
import kin.sdk.exception.AccountDeletedException;
import kin.sdk.internal.KinAccountImpl;
import kin.sdk.internal.services.AccountInfoRetriever;
import kin.sdk.internal.services.TransactionSender;
import kin.sdk.internal.utils.BlockchainEventsCreator;
import kin.sdk.models.AccountStatus;
import kin.sdk.models.Balance;
import kin.sdk.models.Transaction;
import kin.sdk.models.TransactionId;
import kin.sdk.models.WhitelistableTransaction;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KinAccountImplTest {

    private KeyPair source = KeyPair.fromSecretSeed("SCH27VUZZ6UAKB67BDNF6FA42YMBMQCBKXWGMFD5TZ6S5ZZCZFLRXKHS");
    private KeyPair destination = KeyPair.fromAccountId("GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR");
    private Account sourceAccount = new Account(source, 2908908335136768L);
    private BigDecimal expectedAmount = new BigDecimal("12.2");
    private TransactionId expectedTransactionId = new TransactionId("myId");
    private int expectedFee = 100;
    private String memo = "Dummy Memo";
    private Transaction transaction = new Transaction(
            source,
            destination,
            expectedAmount,
            expectedFee,
            memo,
            expectedTransactionId,
            new kin.base.Transaction.Builder(sourceAccount)
                    .addOperation(
                            new PaymentOperation.Builder(
                                    destination,
                                    new AssetTypeNative(),
                                    expectedAmount.toString()
                            ).build()
                    )
                    .build(),
            new WhitelistableTransaction("", "")
    );

    @Mock
    private TransactionSender mockTransactionSender;
    @Mock
    private AccountInfoRetriever mockAccountInfoRetriever;
    @Mock
    private BlockchainEventsCreator mockBlockchainEventsCreator;

    private KinAccountImpl sut;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        sut = new KinAccountImpl(
                source, new FakeBackupRestore(),
                mockTransactionSender,
                mockAccountInfoRetriever,
                mockBlockchainEventsCreator
        );

        when(mockTransactionSender.buildTransaction(eq(source), eq(destination.getAccountId()), eq(expectedAmount), eq(expectedFee))).thenReturn(transaction);
        when(mockTransactionSender.buildTransaction(eq(source), eq(destination.getAccountId()), eq(expectedAmount), eq(expectedFee), eq(memo))).thenReturn(transaction);
        when(mockTransactionSender.sendTransaction(any())).thenReturn(expectedTransactionId);
    }

    @Test
    public void getPublicAddress_ExistingAccount() throws Exception {
        assertEquals(source.getAccountId(), sut.getPublicAddress());
    }

    @Test
    public void sendTransactionSync() throws Exception {
        Transaction transaction = sut.buildTransactionSync(destination.getAccountId(), expectedAmount, expectedFee);
        TransactionId transactionId = sut.sendTransactionSync(transaction);

        verify(mockTransactionSender).sendTransaction(transaction);
        assertEquals(expectedTransactionId, transactionId);
    }

    @Test
    public void sendTransactionSync_WithMemo() throws Exception {
        Transaction transaction = sut.buildTransactionSync(destination.getAccountId(), expectedAmount, expectedFee, memo);
        TransactionId transactionId = sut.sendTransactionSync(transaction);

        verify(mockTransactionSender).sendTransaction(transaction);
        assertEquals(expectedTransactionId, transactionId);
    }

    @Test
    public void getBalanceSync() throws Exception {
        Balance expectedBalance = new Balance(new BigDecimal("11.0"));
        when(mockAccountInfoRetriever.getBalance(anyString())).thenReturn(expectedBalance);

        Balance balance = sut.getBalanceSync();

        assertEquals(expectedBalance, balance);
        verify(mockAccountInfoRetriever).getBalance(source.getAccountId());
    }

    @Test
    public void getStatusSync() throws Exception {
        when(mockAccountInfoRetriever.getStatus(anyString())).thenReturn(AccountStatus.CREATED);

        int status = sut.getStatusSync();

        assertEquals(AccountStatus.CREATED, status);
        verify(mockAccountInfoRetriever).getStatus(source.getAccountId());
    }

    @Test(expected = AccountDeletedException.class)
    public void sendTransactionSync_DeletedAccount_Exception() throws Exception {
        sut.markAsDeleted();

        Transaction transaction = sut.buildTransactionSync(source.getAccountId(), expectedAmount, expectedFee);
        sut.sendTransactionSync(transaction);
    }

    @Test(expected = AccountDeletedException.class)
    public void sendWhitelistTransaction_DeletedAccount_Exception() throws Exception {
        sut.markAsDeleted();

        String whitelist = "whitelist test string";
        sut.sendWhitelistTransactionSync(whitelist);
    }

    @Test(expected = AccountDeletedException.class)
    public void getBalanceSync_DeletedAccount_Exception() throws Exception {
        sut.markAsDeleted();
        sut.getBalanceSync();
    }

    @Test(expected = AccountDeletedException.class)
    public void getStatusSync_DeletedAccount_Exception() throws Exception {
        sut.markAsDeleted();
        sut.getStatusSync();
    }

    @Test
    public void getPublicAddress_DeletedAccount_Empty() throws Exception {
        sut.markAsDeleted();
        assertNull(sut.getPublicAddress());
    }
}
