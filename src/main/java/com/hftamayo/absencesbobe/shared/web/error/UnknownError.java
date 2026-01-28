package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorCode;

public class UnknownError extends Exception implements ErrorLogEventDescriptor {

    public UnknownError(String message) {
        super(message);
    }

    public UnknownError(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public ErrorCode getType() {
        return ErrorCode.UNKNOWN_ERROR;
    }

    @Override
    public String getDetail() {
        return getMessage();
    }
}