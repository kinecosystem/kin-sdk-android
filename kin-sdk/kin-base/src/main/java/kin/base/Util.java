package kin.base;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import kin.base.codec.Base64;
import kin.base.xdr.XdrDataInputStream;

public class Util {

  public static final String CHARSET_UTF8 = "UTF-8";
  static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for ( int j = 0; j < bytes.length; j++ ) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }

  static byte[] hexToBytes(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
          + Character.digit(s.charAt(i+1), 16));
    }
    return data;
  }

  /**
   * Returns SHA-256 hash of <code>data</code>.
   * @param data
   */
  static byte[] hash(byte[] data) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(data);
      return md.digest();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("SHA-256 not implemented");
    }
  }

  /**
   * Pads <code>bytes</code> array to <code>length</code> with zeros.
   * @param bytes
   * @param length
   */
  static byte[] paddedByteArray(byte[] bytes, int length) {
    byte[] finalBytes = new byte[length];
    Arrays.fill(finalBytes, (byte) 0);
    System.arraycopy(bytes, 0, finalBytes, 0, bytes.length);
    return finalBytes;
  }

  /**
   * Pads <code>string</code> to <code>length</code> with zeros.
   * @param string
   * @param length
   */
  static byte[] paddedByteArray(String string, int length) {
    return Util.paddedByteArray(string.getBytes(), length);
  }

  /**
   * Remove zeros from the end of <code>bytes</code> array.
   * @param bytes
   */
  static String paddedByteArrayToString(byte[] bytes) {
    return new String(bytes).split("\0")[0];
  }

  public static <T> T checkNotNull(T obj, String msg) {
      if (obj == null) {
          throw new NullPointerException(msg);
      } else {
          return obj;
      }
  }

  public static void checkArgument(boolean expression, final Object errorMessage) {
      if (!expression) {
          throw new IllegalArgumentException(String.valueOf(errorMessage));
      }
  }

  public static XdrDataInputStream createXdrDataInputStream(String envelopeXdr) throws UnsupportedEncodingException {
    Base64 base64 = new Base64();
    byte[] decoded = base64.decode(envelopeXdr.getBytes(CHARSET_UTF8));
    InputStream is = new ByteArrayInputStream(decoded);
    return new XdrDataInputStream(is);
  }
}
