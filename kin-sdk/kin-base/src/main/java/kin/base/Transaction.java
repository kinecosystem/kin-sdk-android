package kin.base;

import static kin.base.Util.checkArgument;
import static kin.base.Util.checkNotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import kin.base.codec.Base64;
import kin.base.xdr.DecoratedSignature;
import kin.base.xdr.EnvelopeType;
import kin.base.xdr.SignatureHint;
import kin.base.xdr.TransactionEnvelope;
import kin.base.xdr.XdrDataOutputStream;


/**
 * Represents <a href="https://www.stellar.org/developers/learn/concepts/transactions.html" target="_blank">Transaction</a> in Stellar network.
 */
public class Transaction {

  private final int mFee;
  private final KeyPair mSourceAccount;
  private final long mSequenceNumber;
  private final Operation[] mOperations;
  private final Memo mMemo;
  private final TimeBounds mTimeBounds;
  private List<DecoratedSignature> mSignatures;

    Transaction(KeyPair sourceAccount, int feePerOperation, long sequenceNumber, Operation[] operations, Memo memo,
        TimeBounds timeBounds) {
    mSourceAccount = checkNotNull(sourceAccount, "sourceAccount cannot be null");
    mSequenceNumber = checkNotNull(sequenceNumber, "sequenceNumber cannot be null");
    mOperations = checkNotNull(operations, "operations cannot be null");
    checkArgument(operations.length > 0, "At least one operation required");

    mFee = operations.length * feePerOperation;
    mSignatures = new ArrayList<DecoratedSignature>();
    mMemo = memo != null ? memo : Memo.none();
    mTimeBounds = timeBounds;
  }

  /**
   * Adds a new signature ed25519PublicKey to this transaction.
   * @param signer {@link KeyPair} object representing a signer
   */
  public void sign(KeyPair signer) {
    checkNotNull(signer, "signer cannot be null");
    byte[] txHash = this.hash();
    mSignatures.add(signer.signDecorated(txHash));
  }

  /**
   * Adds a new sha256Hash signature to this transaction by revealing preimage.
   * @param preimage the sha256 hash of preimage should be equal to signer hash
   */
  public void sign(byte[] preimage) {
    checkNotNull(preimage, "preimage cannot be null");
    kin.base.xdr.Signature signature = new kin.base.xdr.Signature();
    signature.setSignature(preimage);

    byte[] hash = Util.hash(preimage);
    byte[] signatureHintBytes = Arrays.copyOfRange(hash, hash.length - 4, hash.length);
    SignatureHint signatureHint = new SignatureHint();
    signatureHint.setSignatureHint(signatureHintBytes);

    DecoratedSignature decoratedSignature = new DecoratedSignature();
    decoratedSignature.setHint(signatureHint);
    decoratedSignature.setSignature(signature);

    mSignatures.add(decoratedSignature);
  }

  /**
   * Returns transaction hash.
   */
  public byte[] hash() {
    return Util.hash(this.signatureBase());
  }

  /**
   * Returns signature base.
   */
  public byte[] signatureBase() {
    if (Network.current() == null) {
      throw new NoNetworkSelectedException();
    }

    try {
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      // Hashed NetworkID
      outputStream.write(Network.current().getNetworkId());
      // Envelope Type - 4 bytes
      outputStream.write(ByteBuffer.allocate(4).putInt(EnvelopeType.ENVELOPE_TYPE_TX.getValue()).array());
      // Transaction XDR bytes
      ByteArrayOutputStream txOutputStream = new ByteArrayOutputStream();
      XdrDataOutputStream xdrOutputStream = new XdrDataOutputStream(txOutputStream);
      kin.base.xdr.Transaction.encode(xdrOutputStream, this.toXdr());
      outputStream.write(txOutputStream.toByteArray());

      return outputStream.toByteArray();
    } catch (IOException exception) {
      return null;
    }
  }

  public KeyPair getSourceAccount() {
    return mSourceAccount;
  }

  public long getSequenceNumber() {
    return mSequenceNumber;
  }

  public List<DecoratedSignature> getSignatures() {
    return mSignatures;
  }

  public Memo getMemo() {
    return mMemo;
  }

  public Operation[] getOperations() {
    return mOperations;
  }

  /**
   * @return TimeBounds, or null (representing no time restrictions)
   */
  public TimeBounds getTimeBounds() {
    return mTimeBounds;
  }

  /**
   * Returns fee paid for transaction in stroops (1 stroop = 0.0000001 XLM).
   */
  public int getFee() {
    return mFee;
  }

  /**
   * Generates Transaction XDR object.
   */
  public kin.base.xdr.Transaction toXdr() {
    // fee
    kin.base.xdr.Uint32 fee = new kin.base.xdr.Uint32();
    fee.setUint32(mFee);
    // sequenceNumber
    kin.base.xdr.Uint64 sequenceNumberUint = new kin.base.xdr.Uint64();
    sequenceNumberUint.setUint64(mSequenceNumber);
    kin.base.xdr.SequenceNumber sequenceNumber = new kin.base.xdr.SequenceNumber();
    sequenceNumber.setSequenceNumber(sequenceNumberUint);
    // sourceAccount
    kin.base.xdr.AccountID sourceAccount = new kin.base.xdr.AccountID();
    sourceAccount.setAccountID(mSourceAccount.getXdrPublicKey());
    // operations
    kin.base.xdr.Operation[] operations = new kin.base.xdr.Operation[mOperations.length];
    for (int i = 0; i < mOperations.length; i++) {
      operations[i] = mOperations[i].toXdr();
    }
    // ext
    kin.base.xdr.Transaction.TransactionExt ext = new kin.base.xdr.Transaction.TransactionExt();
    ext.setDiscriminant(0);

    kin.base.xdr.Transaction transaction = new kin.base.xdr.Transaction();
    transaction.setFee(fee);
    transaction.setSeqNum(sequenceNumber);
    transaction.setSourceAccount(sourceAccount);
    transaction.setOperations(operations);
    transaction.setMemo(mMemo.toXdr());
    transaction.setTimeBounds(mTimeBounds == null ? null : mTimeBounds.toXdr());
    transaction.setExt(ext);
    return transaction;
  }

  /**
   * Generates TransactionEnvelope XDR object. Transaction need to have at least one signature.
   */
  public kin.base.xdr.TransactionEnvelope toEnvelopeXdr() {
    if (mSignatures.size() == 0) {
      throw new NotEnoughSignaturesException("Transaction must be signed by at least one signer. Use transaction.sign().");
    }

    kin.base.xdr.TransactionEnvelope xdr = new kin.base.xdr.TransactionEnvelope();
    kin.base.xdr.Transaction transaction = this.toXdr();
    xdr.setTx(transaction);

    DecoratedSignature[] signatures = new DecoratedSignature[mSignatures.size()];
    signatures = mSignatures.toArray(signatures);
    xdr.setSignatures(signatures);
    return xdr;
  }

  /**
   * Returns base64-encoded TransactionEnvelope XDR object. Transaction need to have at least one signature.
   */
  public String toEnvelopeXdrBase64() {
    try {
      kin.base.xdr.TransactionEnvelope envelope = this.toEnvelopeXdr();
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      XdrDataOutputStream xdrOutputStream = new XdrDataOutputStream(outputStream);
      kin.base.xdr.TransactionEnvelope.encode(xdrOutputStream, envelope);
      Base64 base64Codec = new Base64();
      return base64Codec.encodeAsString(outputStream.toByteArray());
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Creates a <code>Transaction</code> instance from previously build <code>TransactionEnvelope</code>
   * @param envelope Base-64 encoded <code>TransactionEnvelope</code>
   * @return
   * @throws IOException
   */
  public static Transaction fromEnvelopeXdr(String envelope) throws IOException {
    TransactionEnvelope transactionEnvelope = TransactionEnvelope.decode(Util.createXdrDataInputStream(envelope));
    return fromEnvelopeXdr(transactionEnvelope);
  }

  /**
   * Creates a <code>Transaction</code> instance from previously build <code>TransactionEnvelope</code>
   * @param envelope Base-64 encoded <code>TransactionEnvelope</code>
   * @return
   */
  public static Transaction fromEnvelopeXdr(TransactionEnvelope envelope) {
    kin.base.xdr.Transaction tx = envelope.getTx();
    // Although currently there couldn't be a transaction with no operations we still check it.
      int feePerOperation = tx.getFee().getUint32();
    if (tx.getOperations().length > 1) {
      // Because the fee was already multiplied by number of operation then divide by it because when reCreate this
      // transaction then we will multiple it again.
        feePerOperation = feePerOperation / tx.getOperations().length;
    }

    KeyPair mSourceAccount = KeyPair.fromXdrPublicKey(tx.getSourceAccount().getAccountID());
    Long mSequenceNumber = tx.getSeqNum().getSequenceNumber().getUint64();
    Memo mMemo = Memo.fromXdr(tx.getMemo());
    TimeBounds mTimeBounds = TimeBounds.fromXdr(tx.getTimeBounds());

    Operation[] mOperations = new Operation[tx.getOperations().length];
    for (int i = 0; i < tx.getOperations().length; i++) {
      mOperations[i] = Operation.fromXdr(tx.getOperations()[i]);
    }

      Transaction transaction = new Transaction(mSourceAccount, feePerOperation, mSequenceNumber, mOperations, mMemo,
          mTimeBounds);

    for (DecoratedSignature signature : envelope.getSignatures()) {
      transaction.mSignatures.add(signature);
    }

    return transaction;
  }

  /**
   * Builds a new Transaction object.
   */
  public static class Builder {
    private final TransactionBuilderAccount mSourceAccount;
    private int fee ;
    private Memo mMemo;
    private TimeBounds mTimeBounds;
    List<Operation> mOperations;

    /**
     * Construct a new transaction builder.
     * @param sourceAccount The source account for this transaction. This account is the account
     * who will use a sequence number. When build() is called, the account object's sequence number
     * will be incremented.
     */
    public Builder(TransactionBuilderAccount sourceAccount) {
      checkNotNull(sourceAccount, "sourceAccount cannot be null");
      mSourceAccount = sourceAccount;
      mOperations = Collections.synchronizedList(new ArrayList<Operation>());
    }

    public int getOperationsCount() {
      return mOperations.size();
    }

    /**
     * Adds a new <a href="https://www.stellar.org/developers/learn/concepts/list-of-operations.html" target="_blank">operation</a> to this transaction.
     * @param operation
     * @return Builder object so you can chain methods.
     * @see Operation
     */
    public Builder addOperation(Operation operation) {
      checkNotNull(operation, "operation cannot be null");
      mOperations.add(operation);
      return this;
    }

    /**
     * @param fee this transaction fee
     * @return Builder object so you can chain methods.
     */
    public Builder addFee(int fee) {
      this.fee = fee;
      return this;
    }

    /**
     * Adds a <a href="https://www.stellar.org/developers/learn/concepts/transactions.html" target="_blank">memo</a> to this transaction.
     * @param memo
     * @return Builder object so you can chain methods.
     * @see Memo
     */
    public Builder addMemo(Memo memo) {
      if (mMemo != null) {
        throw new RuntimeException("Memo has been already added.");
      }
      checkNotNull(memo, "memo cannot be null");
      mMemo = memo;
      return this;
    }
    
    /**
     * Adds a <a href="https://www.stellar.org/developers/learn/concepts/transactions.html" target="_blank">time-bounds</a> to this transaction.
     * @param timeBounds
     * @return Builder object so you can chain methods.
     * @see TimeBounds
     */
    public Builder addTimeBounds(TimeBounds timeBounds) {
      if (mTimeBounds != null) {
        throw new RuntimeException("TimeBounds has been already added.");
      }
      checkNotNull(timeBounds, "timeBounds cannot be null");
      mTimeBounds = timeBounds;
      return this;
    }

    /**
     * Builds a transaction. It will increment sequence number of the source account.
     */
    public Transaction build() {
      Operation[] operations = new Operation[mOperations.size()];
      operations = mOperations.toArray(operations);
      Transaction transaction = new Transaction(mSourceAccount.getKeypair(), fee, mSourceAccount.getIncrementedSequenceNumber(), operations, mMemo, mTimeBounds);
      // Increment sequence number when there were no exceptions when creating a transaction
      mSourceAccount.incrementSequenceNumber();
      return transaction;
    }
  }
}
