package com.mahmoud.nagieb.exception;

public class DuplicateVendorNameException extends RuntimeException {

    private final String messageKey;
    private final Object[] parameters;

    public DuplicateVendorNameException(String messageKey, Object... parameters) {
        super();
        this.messageKey = messageKey;
        this.parameters = parameters;
    }

    public String getMessageKey() {
        return messageKey;
    }
    public Object[] getParameters() {
        return parameters;
    }
}
