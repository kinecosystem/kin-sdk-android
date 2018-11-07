package kin.base;

/**
 * Indicates that value passed to Memo
 * @see Memo
 */
public class MemoTooLongException extends RuntimeException {
    public MemoTooLongException() {
        super();
    }

    public MemoTooLongException(String message) {
        super(message);
    }
}
