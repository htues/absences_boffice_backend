package com.hftamayo.absencesbobe.features.shared.application.result;

import com.hftamayo.absencesbobe.shared.application.result.Result;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void constructor_allowsValueOnly() {
        Result<String, String> r = new Result<>("v", null);
        assertTrue(r.isSuccess());
        assertFalse(r.isError());
    }

    @Test
    void constructor_allowsErrorOnly() {
        Result<String, String> r = new Result<>(null, "e");
        assertFalse(r.isSuccess());
        assertTrue(r.isError());
    }

    @Test
    void constructor_rejectsBothNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Result<>(null, null)
        );
        assertEquals("Result must have exactly one of value or error", ex.getMessage());
    }

    @Test
    void constructor_rejectsBothNonNull() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Result<>("v", "e")
        );
        assertEquals("Result must have exactly one of value or error", ex.getMessage());
    }

    @Test
    void ok_factoryRejectsNullValue() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> Result.ok(null)
        );
        assertEquals("value must not be null", ex.getMessage());
    }

    @Test
    void error_factoryRejectsNullError() {
        NullPointerException ex = assertThrows(
                NullPointerException.class,
                () -> Result.error(null)
        );
        assertEquals("error must not be null", ex.getMessage());
    }

    @Test
    void getValueOrThrow_behavesCorrectly() {
        Result<String, String> ok = Result.ok("v");
        assertEquals("v", ok.getValueOrThrow());

        Result<String, String> err = Result.error("e");
        IllegalStateException ex = assertThrows(IllegalStateException.class, err::getValueOrThrow);
        assertEquals("Result is error", ex.getMessage());
    }

    @Test
    void getErrorOrThrow_behavesCorrectly() {
        Result<String, String> err = Result.error("e");
        assertEquals("e", err.getErrorOrThrow());

        Result<String, String> ok = Result.ok("v");
        IllegalStateException ex = assertThrows(IllegalStateException.class, ok::getErrorOrThrow);
        assertEquals("Result is success", ex.getMessage());
    }

    @Test
    void optionals_areConsistent() {
        Result<String, String> ok = Result.ok("v");
        assertEquals(Optional.of("v"), ok.valueOpt());
        assertEquals(Optional.empty(), ok.errorOpt());

        Result<String, String> err = Result.error("e");
        assertEquals(Optional.empty(), err.valueOpt());
        assertEquals(Optional.of("e"), err.errorOpt());
    }
}