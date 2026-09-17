package com.hftamayo.absencesbobe.shared.web.factory;

import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.error.ErrorLogEventDescriptor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class ErrorResponseMapper {

    public static ErrorApiResponse fromDescriptor(ApiResponseDescriptor descriptor) {
        return descriptor instanceof ErrorApiResponse errorCode
                ? errorCode
                : ErrorApiResponse.UNKNOWN_ERROR;
    }

    public static ErrorApiResponse fromLogEventDescriptor(ErrorLogEventDescriptor error) {
        return error == null || error.getType() == null
                ? ErrorApiResponse.UNKNOWN_ERROR
                : error.getType();
    }
}