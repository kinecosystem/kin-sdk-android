package kin.base;

import static kin.base.Util.CHARSET_UTF8;
import static kin.base.Util.checkNotNull;

import java.io.UnsupportedEncodingException;
import kin.base.xdr.MemoType;

/**
 * Represents MEMO_TEXT.
 */
public class MemoText extends Memo {
  private String text;

  public MemoText(String text) {
    this.text = checkNotNull(text, "text cannot be null");

    int length = 0;
    try {
      length = text.getBytes(CHARSET_UTF8).length;
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    if (length > 28) {
      throw new MemoTooLongException("text must be <= 28 bytes. length=" + String.valueOf(length));
    }
  }

  public String getText() {
    return text;
  }

  @Override
  kin.base.xdr.Memo toXdr() {
    kin.base.xdr.Memo memo = new kin.base.xdr.Memo();
    memo.setDiscriminant(MemoType.MEMO_TEXT);
    memo.setText(text);
    return memo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MemoText memoText = (MemoText) o;
    if (text == null && memoText.text == null) {
      return true;
    }
    if (text != null && memoText!= null) {
      return text.equals(memoText.text);
    }
    return false;
  }

}
