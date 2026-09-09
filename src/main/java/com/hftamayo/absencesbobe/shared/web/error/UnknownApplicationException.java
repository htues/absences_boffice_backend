package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;

public class UnknownApplicationException extends Exception implements ErrorLogEventDescriptor {

    public UnknownApplicationException(String message) {
        super(message);
    }

    public UnknownApplicationException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public ErrorApiResponse getType() {
        return ErrorApiResponse.UNKNOWN_ERROR;
    }

    @Override
    public String getDetail() {
        return getMessage();
    }
}