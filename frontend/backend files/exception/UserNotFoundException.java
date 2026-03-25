package com.mahmoud.maalflow.exception;


public class UserNotFoundException extends RuntimeException {

    private final String messageKey;
    private final Object[] parameters;

    public UserNotFoundException(String messageKey, Object... parameters) {
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
