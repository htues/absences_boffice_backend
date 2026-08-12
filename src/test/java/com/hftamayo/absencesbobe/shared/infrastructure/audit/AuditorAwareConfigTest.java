package com.hftamayo.absencesbobe.shared.infrastructure.audit;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AuditorAware;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuditorAwareConfigTest {

    @Test
    void auditorAware_returnsSystemUserId() {
        AuditorAwareConfig config = new AuditorAwareConfig();

        AuditorAware<Long> auditorAware = config.auditorAware();

        assertEquals(0L, auditorAware.getCurrentAuditor().orElseThrow());
    }
}
