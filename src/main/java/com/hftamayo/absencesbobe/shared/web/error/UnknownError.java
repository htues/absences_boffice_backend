package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;

public class UnknownError extends Exception implements ErrorLogEventDescriptor {

    public UnknownError(String message) {
        super(message);
    }

    public UnknownError(String message, Throwable cause) {
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