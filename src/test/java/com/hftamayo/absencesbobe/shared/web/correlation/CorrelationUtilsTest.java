package com.hftamayo.absencesbobe.features.shared.web.correlation;

import com.hftamayo.absencesbobe.shared.web.constants.CorrelationConstants;
import com.hftamayo.absencesbobe.shared.web.correlation.CorrelationUtils;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationUtilsTest {

    @Test
    void getCorrelationId_whenAttributePresentAndNotBlank_returnsIt_andDoesNotOverwrite() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "abc-123");

        String result = CorrelationUtils.getCorrelationId(request);

        assertEquals("abc-123", result);
        assertEquals("abc-123", request.getAttribute(CorrelationConstants.ATTRIBUTE));
    }

    @Test
    void getCorrelationId_whenAttributeBlank_usesHeaderTrimmed_andStoresAsAttribute() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(CorrelationConstants.ATTRIBUTE, "   "); // blank -> should be ignored
        request.addHeader(CorrelationConstants.HEADER, "  hdr-456  ");

        String result = CorrelationUtils.getCorrelationId(request);

        assertEquals("hdr-456", result);
        assertEquals("hdr-456", request.getAttribute(CorrelationConstants.ATTRIBUTE));
    }

    @Test
    void getCorrelationId_whenNoAttributeAndHeaderPresent_usesHeaderTrimmed_andStoresAsAttribute() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationConstants.HEADER, "\tcor-789\n");

        String result = CorrelationUtils.getCorrelationId(request);

        assertEquals("cor-789", result);
        assertEquals("cor-789", request.getAttribute(CorrelationConstants.ATTRIBUTE));
    }

    @Test
    void getCorrelationId_whenNoAttributeAndNoHeader_generatesUuid_andStoresAsAttribute() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = CorrelationUtils.getCorrelationId(request);

        assertNotNull(result);
        assertEquals(result, request.getAttribute(CorrelationConstants.ATTRIBUTE));

        // Validates it's a UUID without hardcoding the random value
        assertDoesNotThrow(() -> UUID.fromString(result));
    }

    @Test
    void getInstance_whenControllerProvided_returnsControllerSimpleNameAndUri() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/absences");

        String result = CorrelationUtils.getInstance(CorrelationUtilsTest.class, request);

        assertEquals("controller:CorrelationUtilsTest /api/absences", result);
    }

    @Test
    void getInstance_whenControllerNull_usesUnknownController() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/health");

        String result = CorrelationUtils.getInstance(null, request);

        assertEquals("controller:UnknownController /health", result);
    }
}