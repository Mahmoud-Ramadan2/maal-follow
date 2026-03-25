package com.mahmoud.maalflow.exception;

public class DuplicateNationalIdException extends RuntimeException {

    private final String messageKey;
    private final Object[] parameters;

    public DuplicateNationalIdException(String messageKey,Object... parameters) {
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
