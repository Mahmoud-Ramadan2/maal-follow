package com.mahmoud.maalflow.exception;

public class BusinessException extends RuntimeException{
    private final String messageKey;
    public BusinessException(String messageKey) {
        super();
        this.messageKey = messageKey;
    }
    public String getMessageKey() {
        return messageKey;
    }
}
