package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorCode;

public interface ErrorLogEventDescriptor {
    ErrorCode getType();

    String getDetail();

    default String getErrorCode() {
        return getType().name();
    };    // machine code (or derive from getType())

    default int getStatusCode() {
        return getType().getStatusCode();
    }

    default String getTitle() {
        return getType().getMessageKey();
    }
}