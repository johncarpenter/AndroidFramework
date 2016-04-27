package com.twolinessoftware.network;

/**
 *
 */
public class CommException extends Throwable {

    public static final CommException TIMEOUT = new CommException(-1);
    public static final CommException NO_CONNECTION = new CommException(-2);
    public static final CommException UNKNOWN = new CommException(999);


    private int code;

    private CommException(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return "ApiException:" + code;
    }
}
