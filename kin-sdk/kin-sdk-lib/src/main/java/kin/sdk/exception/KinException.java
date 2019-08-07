package kin.sdk.exception;

public abstract class KinException extends Exception {

    public KinException(Throwable e) {
        super(e);
    }

    public KinException(String msg) {
        super(msg);
    }

    public KinException(String msg, Throwable e) {
        super(msg, e);
    }

}
