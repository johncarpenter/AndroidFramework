package com.twolinessoftware.network;

/**
 *
 */
public class ApiException extends Throwable {

    public static final int BAD_REQUEST = 400;
    public static final int NOT_AUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int SERVICE_ERROR = 500;
    public static final int SERVICE_UNAVAILABLE = 503;
    public static final int CODE_TIMEOUT = -1;
    public static final int CODE_NO_CONNECTION = -2;
    public static final int CODE_UNKNOWN = -3;

    public static final ApiException TIMEOUT = new ApiException(-1);
    public static final ApiException NO_CONNECTION = new ApiException(-2);
    public static final ApiException UNKNOWN = new ApiException(999);


    private int code;
    private Errors errors;

    private ApiException(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Errors getErrors() {
        return errors;
    }

    public void setErrors(Errors errors) {
        this.errors = errors;
    }

    @Override
    public String getMessage() {
        return "ApiException:"+code+" HasErrors:"+errors.hasErrors();
    }
}
