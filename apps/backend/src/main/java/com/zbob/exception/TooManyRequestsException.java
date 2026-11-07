package com.zbob.exception;

public class TooManyRequestsException extends RuntimeException {

    private final long retryAfterSeconds;

    public TooManyRequestsException(long retryAfterSeconds) {
        super(message(retryAfterSeconds));
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    private static String message(long seconds) {
        long minutes = Math.max(1, (seconds + 59) / 60);
        return "Too many attempts. Please try again in " + minutes + (minutes == 1 ? " minute." : " minutes.");
    }
}
