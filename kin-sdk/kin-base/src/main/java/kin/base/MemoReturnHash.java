package kin.base;

import kin.base.codec.DecoderException;
import kin.base.xdr.Memo;
import kin.base.xdr.MemoType;

/**
 * Represents MEMO_RETURN.
 */
public class MemoReturnHash extends MemoHashAbstract {
  public MemoReturnHash(byte[] bytes) {
    super(bytes);
  }

  public MemoReturnHash(String hexString) throws DecoderException {
    super(hexString);
  }

  @Override
  Memo toXdr() {
    kin.base.xdr.Memo memo = new kin.base.xdr.Memo();
    memo.setDiscriminant(MemoType.MEMO_RETURN);

    kin.base.xdr.Hash hash = new kin.base.xdr.Hash();
    hash.setHash(bytes);

    memo.setHash(hash);
    return memo;
  }
}
