package com.hftamayo.absencesbobe.shared.web.error;

public interface BusinessError {
    String getTitle();
    int getStatus();
    String getDetail();
    String getErrorCode();
}