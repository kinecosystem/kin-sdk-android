package kin.base;

import org.junit.Before;
import org.junit.Test;

import kin.base.codec.Base64;
import kin.base.xdr.XdrDataInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TransactionTest {

    @Before
    public void setupNetwork() {
        Network.useTestNetwork();
    }

    @Test
    public void testBuilderSuccessTestnet() throws FormatException {
        // GBPMKIRA2OQW2XZZQUCQILI5TMVZ6JNRKM423BSAISDM7ZFWQ6KWEBC4
        KeyPair source = KeyPair.fromSecretSeed("SCH27VUZZ6UAKB67BDNF6FA42YMBMQCBKXWGMFD5TZ6S5ZZCZFLRXKHS");
        KeyPair destination = KeyPair.fromAccountId("GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR");

        long sequenceNumber = 2908908335136768L;
        Account account = new Account(source, sequenceNumber);
        Transaction transaction = new Transaction.Builder(account)
                .addOperation(new CreateAccountOperation.Builder(destination, "2000").build())
                .addFee(100)
                .build();

        transaction.sign(source);

        assertEquals(
                "AAAAAF7FIiDToW1fOYUFBC0dmyufJbFTOa2GQESGz+S2h5ViAAAAZAAKVaMAAAABAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAA7eBSYbzcL5UKo7oXO24y1ckX+XuCtkDsyNHOp1n1bxAAAAAAC+vCAAAAAAAAAAABtoeVYgAAAEAB8zwQnX0d+a5B4rKUtV1IQI7SgDDoGLXtwrlXefsH+rFouKuz2otCQN/out9Szo7KT3QvWZgg26aARFsRYJkL",
                transaction.toEnvelopeXdrBase64());

        assertEquals(transaction.getSourceAccount(), source);
        assertEquals(transaction.getSequenceNumber(), sequenceNumber + 1);
        assertEquals(transaction.getFee(), 100);

        Transaction transaction2 = Transaction.fromEnvelopeXdr(transaction.toEnvelopeXdr());

        assertEquals(transaction.getSourceAccount().getAccountId(), transaction2.getSourceAccount().getAccountId());
        assertEquals(transaction.getSequenceNumber(), transaction2.getSequenceNumber());
        assertEquals(transaction.getFee(), transaction2.getFee());
        assertEquals(
                ((CreateAccountOperation)transaction.getOperations()[0]).getStartingBalance(),
                ((CreateAccountOperation)transaction2.getOperations()[0]).getStartingBalance()
        );

        assertEquals(transaction.getSignatures(), transaction2.getSignatures());
    }

    @Test
    public void testBuilderMemoText() throws FormatException {
        // GBPMKIRA2OQW2XZZQUCQILI5TMVZ6JNRKM423BSAISDM7ZFWQ6KWEBC4
        KeyPair source = KeyPair.fromSecretSeed("SCH27VUZZ6UAKB67BDNF6FA42YMBMQCBKXWGMFD5TZ6S5ZZCZFLRXKHS");
        KeyPair destination = KeyPair.fromAccountId("GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR");

        Account account = new Account(source, 2908908335136768L);
        Transaction transaction = new Transaction.Builder(account)
                .addOperation(new CreateAccountOperation.Builder(destination, "2000").build())
                .addMemo(Memo.text("Hello world!"))
                .build();

        transaction.sign(source);

        assertEquals(
                "AAAAAF7FIiDToW1fOYUFBC0dmyufJbFTOa2GQESGz+S2h5ViAAAAAAAKVaMAAAABAAAAAAAAAAEAAAAMSGVsbG8gd29ybGQhAAAAAQAAAAAAAAAAAAAAAO3gUmG83C+VCqO6FztuMtXJF/l7grZA7MjRzqdZ9W8QAAAAAAvrwgAAAAAAAAAAAbaHlWIAAABAzOC+juaAElCvMXnZR0EhirvtXJalGlW0jZPa+V30M82CFNOyXI1R0M0Aue7lEEHPyEI7JQfHwlv6zL8Wvh7TBg==",
                transaction.toEnvelopeXdrBase64());

        Transaction transaction2 = Transaction.fromEnvelopeXdr(transaction.toEnvelopeXdr());

        assertEquals(transaction.getSourceAccount().getAccountId(), transaction2.getSourceAccount().getAccountId());
        assertEquals(transaction.getSequenceNumber(), transaction2.getSequenceNumber());
        assertEquals(transaction.getMemo(), transaction2.getMemo());
        assertEquals(transaction.getFee(), transaction2.getFee());
        assertEquals(
                ((CreateAccountOperation)transaction.getOperations()[0]).getStartingBalance(),
                ((CreateAccountOperation)transaction2.getOperations()[0]).getStartingBalance()
        );
    }

    @Test
    public void testBuilderTimeBounds() throws FormatException, IOException {
        // GBPMKIRA2OQW2XZZQUCQILI5TMVZ6JNRKM423BSAISDM7ZFWQ6KWEBC4
        KeyPair source = KeyPair.fromSecretSeed("SCH27VUZZ6UAKB67BDNF6FA42YMBMQCBKXWGMFD5TZ6S5ZZCZFLRXKHS");
        KeyPair destination = KeyPair.fromAccountId("GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR");

        Account account = new Account(source, 2908908335136768L);
        Transaction transaction = new Transaction.Builder(account)
                .addOperation(new CreateAccountOperation.Builder(destination, "2000").build())
                .addTimeBounds(new TimeBounds(42, 1337))
                .build();

        transaction.sign(source);

        // Convert transaction to binary XDR and back again to make sure timebounds are correctly de/serialized.

        XdrDataInputStream is = new XdrDataInputStream(
                new ByteArrayInputStream(Base64.decodeBase64(transaction.toEnvelopeXdrBase64().getBytes()))
        );
        kin.base.xdr.Transaction decodedTransaction = kin.base.xdr.Transaction.decode(is);

        assertEquals(decodedTransaction.getTimeBounds().getMinTime().getUint64().longValue(), 42);
        assertEquals(decodedTransaction.getTimeBounds().getMaxTime().getUint64().longValue(), 1337);

        Transaction transaction2 = Transaction.fromEnvelopeXdr(transaction.toEnvelopeXdr());

        assertEquals(transaction.getSourceAccount().getAccountId(), transaction2.getSourceAccount().getAccountId());
        assertEquals(transaction.getSequenceNumber(), transaction2.getSequenceNumber());
        assertEquals(transaction.getTimeBounds(), transaction2.getTimeBounds());
        assertEquals(transaction.getFee(), transaction2.getFee());
        assertEquals(
                ((CreateAccountOperation)transaction.getOperations()[0]).getStartingBalance(),
                ((CreateAccountOperation)transaction2.getOperations()[0]).getStartingBalance()
        );
    }

    @Test
    public void testBuilderSuccessPublic() throws FormatException {
        Network.usePublicNetwork();

        // GBPMKIRA2OQW2XZZQUCQILI5TMVZ6JNRKM423BSAISDM7ZFWQ6KWEBC4
        KeyPair source = KeyPair.fromSecretSeed("SCH27VUZZ6UAKB67BDNF6FA42YMBMQCBKXWGMFD5TZ6S5ZZCZFLRXKHS");
        KeyPair destination = KeyPair.fromAccountId("GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR");

        Account account = new Account(source, 2908908335136768L);
        Transaction transaction = new Transaction.Builder(account)
                .addOperation(new CreateAccountOperation.Builder(destination, "2000").build())
                .build();

        transaction.sign(source);

        assertEquals(
                "AAAAAF7FIiDToW1fOYUFBC0dmyufJbFTOa2GQESGz+S2h5ViAAAAAAAKVaMAAAABAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAA7eBSYbzcL5UKo7oXO24y1ckX+XuCtkDsyNHOp1n1bxAAAAAAC+vCAAAAAAAAAAABtoeVYgAAAEAMPojMTqezq9G7T1ZJHNgs2eE3aY5g3YkV9wysO4XNqm4Mx7ZA3RRw3Jvm2y+OJhR97esC558hU2HHK/PLH2sB",
                transaction.toEnvelopeXdrBase64());
    }

    @Test
    public void testSha256HashSigning() throws FormatException {
        Network.usePublicNetwork();

        KeyPair source = KeyPair.fromAccountId("GBBM6BKZPEHWYO3E3YKREDPQXMS4VK35YLNU7NFBRI26RAN7GI5POFBB");
        KeyPair destination = KeyPair.fromAccountId("GDJJRRMBK4IWLEPJGIE6SXD2LP7REGZODU7WDC3I2D6MR37F4XSHBKX2");

        Account account = new Account(source, 0L);
        Transaction transaction = new Transaction.Builder(account)
                .addOperation(new PaymentOperation.Builder(destination, new AssetTypeNative(), "2000").build())
                .build();

        byte[] preimage = new byte[64];
        new SecureRandom().nextBytes(preimage);
        byte[] hash = Util.hash(preimage);

        transaction.sign(preimage);

        assertTrue(Arrays.equals(transaction.getSignatures().get(0).getSignature().getSignature(), preimage));
        assertTrue(Arrays.equals(transaction.getSignatures().get(0).getHint().getSignatureHint(), Arrays.copyOfRange(hash, hash.length - 4, hash.length)));
    }

    @Test
    public void testToBase64EnvelopeXdrBuilderNoSignatures() throws FormatException, IOException {
        // GBPMKIRA2OQW2XZZQUCQILI5TMVZ6JNRKM423BSAISDM7ZFWQ6KWEBC4
        KeyPair source = KeyPair.fromSecretSeed("SCH27VUZZ6UAKB67BDNF6FA42YMBMQCBKXWGMFD5TZ6S5ZZCZFLRXKHS");
        KeyPair destination = KeyPair.fromAccountId("GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR");

        Account account = new Account(source, 2908908335136768L);
        Transaction transaction = new Transaction.Builder(account)
                .addOperation(new CreateAccountOperation.Builder(destination, "2000").build())
                .build();

        try {
            transaction.toEnvelopeXdrBase64();
            fail();
        } catch (RuntimeException exception) {
            assertTrue(exception.getMessage().contains("Transaction must be signed by at least one signer."));
        }
    }

    @Test
    public void testNoOperations() throws FormatException, IOException {
        // GBPMKIRA2OQW2XZZQUCQILI5TMVZ6JNRKM423BSAISDM7ZFWQ6KWEBC4
        KeyPair source = KeyPair.fromSecretSeed("SCH27VUZZ6UAKB67BDNF6FA42YMBMQCBKXWGMFD5TZ6S5ZZCZFLRXKHS");

        Account account = new Account(source, 2908908335136768L);
        try {
            Transaction transaction = new Transaction.Builder(account).build();
            fail();
        } catch (RuntimeException exception) {
            assertTrue(exception.getMessage().contains("At least one operation required"));
            assertEquals(new Long(2908908335136768L), account.getSequenceNumber());
        }
    }

    @Test
    public void testTryingToAddMemoTwice() throws FormatException, IOException {
        // GBPMKIRA2OQW2XZZQUCQILI5TMVZ6JNRKM423BSAISDM7ZFWQ6KWEBC4
        KeyPair source = KeyPair.fromSecretSeed("SCH27VUZZ6UAKB67BDNF6FA42YMBMQCBKXWGMFD5TZ6S5ZZCZFLRXKHS");
        KeyPair destination = KeyPair.fromAccountId("GDW6AUTBXTOC7FIKUO5BOO3OGLK4SF7ZPOBLMQHMZDI45J2Z6VXRB5NR");

        try {
            Account account = new Account(source, 2908908335136768L);
            new Transaction.Builder(account)
                    .addOperation(new CreateAccountOperation.Builder(destination, "2000").build())
                    .addMemo(Memo.none())
                    .addMemo(Memo.none());
            fail();
        } catch (RuntimeException exception) {
            assertTrue(exception.getMessage().contains("Memo has been already added."));
        }
    }
}