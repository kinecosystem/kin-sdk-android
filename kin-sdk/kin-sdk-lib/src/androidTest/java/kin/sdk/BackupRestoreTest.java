package kin.sdk;


import static junit.framework.Assert.assertEquals;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.util.UUID;
import kin.base.KeyPair;
import kin.sdk.exception.CorruptedDataException;
import kin.sdk.exception.CryptoException;
import org.hamcrest.CoreMatchers;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class BackupRestoreTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private static final String APP_ID = "1a2c";
    private Environment environment;
    private KinClientInternal kinClient1;
    private KinClientInternal kinClient2;
    private KeyStore keystore1;
    private KeyStore keystore2;
    private BackupRestore backupRestore;

    @Before
    public void setup() {
        environment = new Environment(IntegConsts.TEST_NETWORK_URL, IntegConsts.TEST_NETWORK_ID);
        backupRestore = new BackupRestoreImpl();
        keystore1 = new FakeKeyStore(backupRestore);
        keystore2 = new FakeKeyStore(backupRestore);
        kinClient1 = createNewKinClient(keystore1);
        kinClient2 = createNewKinClient(keystore2);
        kinClient1.clearAllAccounts();
        kinClient2.clearAllAccounts();
    }

    private KinClientInternal createNewKinClient(KeyStore keyStore) {
        return new KinClientInternal(keyStore, environment, APP_ID, backupRestore);
    }

    @Test
    public void backupAndRestore_Success() throws CryptoException, CorruptedDataException {
        for (int i = 0; i < 50; i++) {
            KeyPair keyPair = KeyPair.random();
            String passpharse = UUID.randomUUID().toString();
            String exportedJson = backupRestore.exportWallet(keyPair, passpharse);
            KeyPair importKeyPair = backupRestore.importWallet(exportedJson, passpharse);
            assertThat(importKeyPair.getAccountId(), equalTo(keyPair.getAccountId()));
            assertThat(importKeyPair.getSecretSeed(), equalTo(keyPair.getSecretSeed()));
        }
    }

    @Test
    public void backupAndRestore_WrongPassphrase_CryptoException() throws CryptoException, CorruptedDataException {
        expectedEx.expect(CryptoException.class);

        KeyPair keyPair = KeyPair.random();
        String exportedJson = backupRestore.exportWallet(keyPair, "1234567890abcefghijkl");
        backupRestore.importWallet(exportedJson, "1234567890abcefghijklX");
    }

    @Test
    public void import_BadJson_CryptoException() throws CryptoException, CorruptedDataException {
        expectedEx.expect(CorruptedDataException.class);
        expectedEx.expectCause(isA(JSONException.class));

        backupRestore.importWallet("not a real json!!", "123456");
    }

    @Test
    public void import_TamperedSaltJson_CryptoException() throws CryptoException, CorruptedDataException {
        expectedEx.expect(CryptoException.class);

        testImportBackup("{\n"
                + "  \"pkey\" : \"GAQJH2KSSWOTX3LCEESPIJPY73QRCH55OBCCM3SYZ3EEWSTMJ4NYNT6S\",\n"
                + "  \"seed\" : \"f5b162bf9bfa93922b709b00a89e5bf7f61eef38717b35dabbabc73a68be77e2b498c5697f99c3f70882a8a11cc5e34f88b6f069f47443dbfa031fadd12e8b6af1cc142c902cfef9\",\n"
                // change last bit in seed (first ones are salt)
                + "  \"salt\" : \"d00564d4887b4ccade9f2b63211c37c3\"\n"
                + "}",
            "123456",
            "GAQJH2KSSWOTX3LCEESPIJPY73QRCH55OBCCM3SYZ3EEWSTMJ4NYNT6S");
    }

    @Test
    public void import_TamperedSeedJson_CryptoException() throws CryptoException, CorruptedDataException {
        expectedEx.expect(CryptoException.class);

        testImportBackup("{\n"
                + "  \"pkey\" : \"GAQJH2KSSWOTX3LCEESPIJPY73QRCH55OBCCM3SYZ3EEWSTMJ4NYNT6S\",\n"
                // change last bit in seed (first ones are nonce)
                + "  \"seed\" : \"f5b162bf9bfa93922b709b00a89e5bf7f61eef38717b35dabbabc73a68be77e2b498c5697f99c3f70882a8a11cc5e34f88b6f069f47443dbfa031fadd12e8b6af1cc142c902cfef8\",\n"
                + "  \"salt\" : \"d00564d4887b4ccade9f2b63211c37c4\"\n"
                + "}",
            "123456",
            "GAQJH2KSSWOTX3LCEESPIJPY73QRCH55OBCCM3SYZ3EEWSTMJ4NYNT6S");
    }

    @Test
    public void importFromIOS() throws CryptoException, CorruptedDataException {
        testImportBackup("{\n"
                + "  \"pkey\" : \"GAQJH2KSSWOTX3LCEESPIJPY73QRCH55OBCCM3SYZ3EEWSTMJ4NYNT6S\",\n"
                + "  \"seed\" : \"f5b162bf9bfa93922b709b00a89e5bf7f61eef38717b35dabbabc73a68be77e2b498c5697f99c3f70882a8a11cc5e34f88b6f069f47443dbfa031fadd12e8b6af1cc142c902cfef9\",\n"
                + "  \"salt\" : \"d00564d4887b4ccade9f2b63211c37c4\"\n"
                + "}",
            "123456",
            "GAQJH2KSSWOTX3LCEESPIJPY73QRCH55OBCCM3SYZ3EEWSTMJ4NYNT6S");
        testImportBackup("{\n"
                + "  \"pkey\" : \"GDNLGOSLTZMZX5GNZ41538663709111FY26DPXLGXEIFJJ7VWDJVN3L5DOG7TFLZXHNCU\",\n"
                + "  \"seed\" : \"0d2a1bab487f5591277692987bd66c6f07199120f3e8fab6d013ac81bc3bd648fa357d90b39178d105da130705ee6e8beaea4a5cf900b53978d8c8fecd72fee7bd5aa7295a619b9d\",\n"
                + "  \"salt\" : \"7fb38499e44f084958e954b73f1c2cf0\"\n"
                + "}",
            "123456",
            "GDNLGOSLTZMZX5GNZ4FY26DPXLGXEIFJJ7VWDJVN3L5DOG7TFLZXHNCU");
    }

    private void testImportBackup(String exportedJson, String passphrase, String publicKey)
        throws CryptoException, CorruptedDataException {
        KeyPair importKeyPair = backupRestore.importWallet(exportedJson, passphrase);
        assertThat(importKeyPair.getAccountId(), equalTo(publicKey));
    }


    @Test
    public void importAccount_AddOnlyIfNotExists() throws Exception {
        kinClient1.addAccount();
        kinClient1.addAccount();
        kinClient1.addAccount();
        KinAccount kinAccount = kinClient1.addAccount();
        String passphrase = UUID.randomUUID().toString();
        String exported = kinAccount.export(passphrase);
        kinClient1.importAccount(exported, passphrase);
        assertEquals(4, kinClient1.getAccountCount());
    }

    @Test
    public void backupRestore() throws Exception {
        for (int i = 0; i < 50; i++) {
            KinAccount kinAccount = kinClient1.addAccount();
            String uuid = UUID.randomUUID().toString();
            String exported = kinAccount.export(uuid);
            KinAccount kinAccount2 = kinClient2.importAccount(exported, uuid);
            assertThat(kinAccount2.getPublicAddress(), CoreMatchers.equalTo(kinAccount.getPublicAddress()));
        }
    }

    @Test
    public void importAccount_AddNewAccount() throws Exception {
        KinAccount kinAccount = kinClient1.addAccount();
        String passphrase = UUID.randomUUID().toString();
        String exported = kinAccount.export(passphrase);
        kinClient1.importAccount(exported, passphrase);
        assertEquals(1, kinClient1.getAccountCount());

        kinClient1.clearAllAccounts();
        assertEquals(0, kinClient1.getAccountCount());

        kinClient1.importAccount(exported, passphrase);
        assertEquals(1, kinClient1.getAccountCount());
    }

    @Test
    public void importAccount_CreateKinClientAgain_AddOnlyIfNotExists() throws Exception {
        kinClient1.addAccount();
        kinClient1.addAccount();
        kinClient1.addAccount();
        KinAccount kinAccount = kinClient1.addAccount();
        String passphrase = UUID.randomUUID().toString();
        String exported = kinAccount.export(passphrase);
        kinClient1.importAccount(exported, passphrase);
        KinClientInternal anotherKinClient = createNewKinClient(keystore1);
        assertEquals(4, anotherKinClient.getAccountCount());
        assertEquals(4, kinClient1.getAccountCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void initKinClient_NullId_Exception() {
        kinClient1 = createNewKinClient(null);
    }

}
