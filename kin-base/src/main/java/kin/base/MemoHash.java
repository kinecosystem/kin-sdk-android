package kin.base;

import kin.base.codec.DecoderException;
import kin.base.xdr.MemoType;

/**
 * Represents MEMO_HASH.
 */
public class MemoHash extends MemoHashAbstract {
  public MemoHash(byte[] bytes) {
    super(bytes);
  }

  public MemoHash(String hexString) throws DecoderException {
    super(hexString);
  }

  @Override
  kin.base.xdr.Memo toXdr() {
    kin.base.xdr.Memo memo = new kin.base.xdr.Memo();
    memo.setDiscriminant(MemoType.MEMO_HASH);

    kin.base.xdr.Hash hash = new kin.base.xdr.Hash();
    hash.setHash(bytes);

    memo.setHash(hash);
    return memo;
  }
}
