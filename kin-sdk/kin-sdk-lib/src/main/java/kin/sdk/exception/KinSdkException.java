package kin.sdk.exception;

public abstract class KinSdkException extends Exception {

    public KinSdkException(Throwable e) {
        super(e);
    }

    public KinSdkException(String msg) {
        super(msg);
    }

    public KinSdkException(String msg, Throwable e) {
        super(msg, e);
    }

}
