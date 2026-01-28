package com.hftamayo.absencesbobe.shared.web.error;

public interface ErrorLogEventDescriptor {
    int getStatusCode();
    String getTitle();
    String getDetail();
    String getErrorCode(); //machine code
}