package com.clover.sdk.exception;

public class CloverException extends Exception {

    private final int httpStatus;
    private final String cloverDetail;

    public CloverException(String message) {
        super(message);
        this.httpStatus = -1;
        this.cloverDetail = null;
    }

    public CloverException(String message, int httpStatus, String cloverDetail) {
        super(message);
        this.httpStatus = httpStatus;
        this.cloverDetail = cloverDetail;
    }

    public CloverException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = -1;
        this.cloverDetail = null;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getCloverDetail() {
        return cloverDetail;
    }

    @Override
    public String toString() {
        if (httpStatus > 0) {
            return "CloverException[HTTP " + httpStatus + "]: " + getMessage()
                    + (cloverDetail != null ? " | detail: " + cloverDetail : "");
        }
        return "CloverException: " + getMessage();
    }
}
