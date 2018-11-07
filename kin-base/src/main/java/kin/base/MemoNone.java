package kin.base;

import kin.base.xdr.MemoType;

/**
 * Represents MEMO_NONE.
 */
public class MemoNone extends Memo {
  @Override
  kin.base.xdr.Memo toXdr() {
    kin.base.xdr.Memo memo = new kin.base.xdr.Memo();
    memo.setDiscriminant(MemoType.MEMO_NONE);
    return memo;
  }
}
