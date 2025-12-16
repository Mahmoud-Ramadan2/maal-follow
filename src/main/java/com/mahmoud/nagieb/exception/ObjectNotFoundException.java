package com.mahmoud.nagieb.exception;

public class ObjectNotFoundException extends RuntimeException{

    private final String messageKey;
    private final Object[] parameters;

    public ObjectNotFoundException(String messageKey, Object... parameters) {
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
