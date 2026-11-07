package com.zbob.exception;

public class PaymentsNotConfiguredException extends RuntimeException {

    public PaymentsNotConfiguredException() {
        super("Online payment is not available yet");
    }
}
