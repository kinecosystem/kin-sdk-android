package kin.sdk.exception;

/**
 * Input exported account data is corrupted and cannot be imported.
 */
public class CorruptedDataException extends KinException {

    public CorruptedDataException(Throwable e) {
        super(e);
    }

    public CorruptedDataException(String msg) {
        super(msg);
    }

    public CorruptedDataException(String msg, Throwable e) {
        super(msg, e);
    }
}
