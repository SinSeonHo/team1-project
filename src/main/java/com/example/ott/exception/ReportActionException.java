package com.example.ott.exception;

/**
 * 신고/조치 관련 로직에서 사용할 커스텀 예외
 */
public class ReportActionException extends RuntimeException {

    public ReportActionException() {
        super();
    }

    public ReportActionException(String message) {
        super(message);
    }

    public ReportActionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReportActionException(Throwable cause) {
        super(cause);
    }

    protected ReportActionException(String message, Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}