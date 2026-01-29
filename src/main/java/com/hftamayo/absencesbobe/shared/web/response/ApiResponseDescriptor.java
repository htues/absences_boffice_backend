package com.hftamayo.absencesbobe.shared.web.response;

public interface ApiResponseDescriptor {
    String getResponseType();
    int getStatusCode();
    String getMessageKey();
}