package com.hftamayo.absencesbobe.shared.web.factory;

import com.hftamayo.absencesbobe.shared.web.constants.ApiResponseDescriptor;
import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.constants.SuccessApiResponse;
import com.hftamayo.absencesbobe.shared.web.error.ErrorLogEventDescriptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorResponseMapperTest {

    @Test
    void fromDescriptor_whenDescriptorIsErrorApiResponse_returnsSameError() {
        ErrorApiResponse result = ErrorResponseMapper.fromDescriptor(ErrorApiResponse.VALIDATION_ERROR);

        assertEquals(ErrorApiResponse.VALIDATION_ERROR, result);
    }

    @Test
    void fromDescriptor_whenDescriptorIsNotErrorApiResponse_returnsUnknownError() {
        ApiResponseDescriptor descriptor = SuccessApiResponse.READ;

        ErrorApiResponse result = ErrorResponseMapper.fromDescriptor(descriptor);

        assertEquals(ErrorApiResponse.UNKNOWN_ERROR, result);
    }

    @Test
    void fromDescriptor_whenDescriptorIsNull_returnsUnknownError() {
        ErrorApiResponse result = ErrorResponseMapper.fromDescriptor(null);

        assertEquals(ErrorApiResponse.UNKNOWN_ERROR, result);
    }

    @Test
    void fromLogEventDescriptor_whenDescriptorIsNull_returnsUnknownError() {
        ErrorApiResponse result = ErrorResponseMapper.fromLogEventDescriptor(null);

        assertEquals(ErrorApiResponse.UNKNOWN_ERROR, result);
    }

    @Test
    void fromLogEventDescriptor_whenTypeIsNull_returnsUnknownError() {
        ErrorLogEventDescriptor descriptor = new ErrorLogEventDescriptor() {
            @Override
            public ErrorApiResponse getType() {
                return null;
            }

            @Override
            public String getDetail() {
                return "Null type test";
            }
        };

        ErrorApiResponse result = ErrorResponseMapper.fromLogEventDescriptor(descriptor);

        assertEquals(ErrorApiResponse.UNKNOWN_ERROR, result);
    }

    @Test
    void fromLogEventDescriptor_whenTypeIsPresent_returnsType() {
        ErrorLogEventDescriptor descriptor = new ErrorLogEventDescriptor() {
            @Override
            public ErrorApiResponse getType() {
                return ErrorApiResponse.VALIDATION_ERROR;
            }

            @Override
            public String getDetail() {
                return "Validation error test";
            }
        };

        ErrorApiResponse result = ErrorResponseMapper.fromLogEventDescriptor(descriptor);

        assertEquals(ErrorApiResponse.VALIDATION_ERROR, result);
    }
}