package com.mahmoud.nagieb.exception;

public class AccessDeniedException extends RuntimeException {

    private final String messageKey;
    public AccessDeniedException(String messageKey) {
        super();
        this.messageKey = messageKey;
    }
    public String getMessageKey() {
        return messageKey;
    }

}
