package com.hftamayo.absencesbobe.shared.application.errors;

import com.hftamayo.absencesbobe.shared.application.result.Result;
import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import org.slf4j.Logger;

public final class UnknownErrorHandler {

    private UnknownErrorHandler() {
    }

    public static <T> Result<T, ? extends ApiResponseDescriptor> catchUnknownError(
            Logger log,
            String method,
            Long id,
            Exception ex
    ) {
        log.error("method={} failed for id={}", method, id, ex);
        return Result.error(ErrorApiResponse.UNKNOWN_ERROR);
    }
}