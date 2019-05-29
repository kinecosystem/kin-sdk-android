package kin.base;

/**
 * Indicates that there was a problem decoding strkey encoded string.
 * @see KeyPair
 */
public class FormatException extends RuntimeException {
  public FormatException() {
    super();
  }

  public FormatException(String message) {
    super(message);
  }
}
