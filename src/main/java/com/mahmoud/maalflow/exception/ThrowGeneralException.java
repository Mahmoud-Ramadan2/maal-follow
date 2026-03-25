package com.mahmoud.maalflow.exception;

public class ThrowGeneralException extends Exception {

    String messageKey;

    public ThrowGeneralException(String messageKey) {
        super();
        this.messageKey = this.messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }


}
