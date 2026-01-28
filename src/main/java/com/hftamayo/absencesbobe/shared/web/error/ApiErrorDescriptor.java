package com.hftamayo.absencesbobe.shared.web.error;

public interface ApiErrorDescriptor {
    int getStatusCode();
    String getTitle();
    String getDetail();
    String getErrorCode(); //machine code
}