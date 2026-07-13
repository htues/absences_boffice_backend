package com.hftamayo.absencesbobe.shared.infrastructure.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hftamayo.absencesbobe.shared.web.error.RateLimiterError;
import io.github.bucket4j.Bucket;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RateLimiterAspectTest {

    private RateLimiterUtil rateLimiterUtil;
    private RateLimiterConfig rateLimiterConfig;
    private ObjectMapper objectMapper;
    private RateLimiterAspect aspect;

    @BeforeEach
    void setUp() {
        rateLimiterUtil = mock(RateLimiterUtil.class);
        rateLimiterConfig = mock(RateLimiterConfig.class);
        objectMapper = new ObjectMapper();
        aspect = new RateLimiterAspect(rateLimiterUtil, rateLimiterConfig, objectMapper);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("rateLimit: consumes tokens, sets headers and proceeds when allowed")
    void rateLimit_whenAllowed_setsHeadersAndProceeds() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = TestHandlers.class.getDeclaredMethod("createCompany");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenReturn("ok");

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/companies");
        request.addHeader("Authorization", "Bearer admin-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        RateLimiterConfig combinedConfig = new RateLimiterConfig();
        combinedConfig.setCapacity(10L);
        combinedConfig.setRefillRate(2L);
        combinedConfig.setRefillDuration(Duration.ofMinutes(1));

        Bucket bucket = mock(Bucket.class);

        when(rateLimiterConfig.getCombinedConfig("company.create", "ADMIN")).thenReturn(combinedConfig);
        when(rateLimiterUtil.createBucket(combinedConfig)).thenReturn(bucket);
        when(rateLimiterUtil.tryConsume(bucket, 5L)).thenReturn(true);
        when(rateLimiterUtil.getAvailableTokens(bucket)).thenReturn(5L);

        Object result = aspect.rateLimit(joinPoint);

        assertEquals("ok", result);
        assertEquals("10", response.getHeader("X-RateLimit-Limit"));
        assertEquals("5", response.getHeader("X-RateLimit-Remaining"));
        assertNotNull(response.getHeader("X-RateLimit-Reset"));
        assertTrue(Long.parseLong(response.getHeader("X-RateLimit-Reset")) > System.currentTimeMillis());

        verify(joinPoint).getSignature();
        verify(signature).getMethod();
        verify(rateLimiterConfig).getCombinedConfig("company.create", "ADMIN");
        verify(rateLimiterUtil).createBucket(combinedConfig);
        verify(rateLimiterUtil).tryConsume(bucket, 5L);
        verify(rateLimiterUtil).getAvailableTokens(bucket);
        verify(joinPoint).proceed();
        verifyNoMoreInteractions(rateLimiterUtil, rateLimiterConfig, joinPoint, signature);
    }

    @Test
    @DisplayName("rateLimit: returns 429 response when tokens cannot be consumed")
    void rateLimit_whenRejected_returnsTooManyRequests() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = TestHandlers.class.getDeclaredMethod("getActiveCompanies");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/companies");
        request.addHeader("Authorization", "Bearer user-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        RateLimiterConfig combinedConfig = new RateLimiterConfig();
        combinedConfig.setCapacity(3L);
        combinedConfig.setRefillRate(1L);
        combinedConfig.setRefillDuration(Duration.ofMinutes(2));

        Bucket bucket = mock(Bucket.class);

        when(rateLimiterConfig.getCombinedConfig("/api/v1/companies", "USER")).thenReturn(combinedConfig);
        when(rateLimiterUtil.createBucket(combinedConfig)).thenReturn(bucket);
        when(rateLimiterUtil.tryConsume(bucket, 1L)).thenReturn(false);
        when(rateLimiterUtil.getAvailableTokens(bucket)).thenReturn(0L);

        Object result = aspect.rateLimit(joinPoint);

        assertNull(result);
        assertEquals(429, response.getStatus());
        assertEquals("application/json", response.getContentType());
        assertEquals("3", response.getHeader("X-RateLimit-Limit"));
        assertEquals("0", response.getHeader("X-RateLimit-Remaining"));
        assertNotNull(response.getHeader("X-RateLimit-Reset"));
        assertTrue(response.getContentAsString().contains("RATE_LIMITED"));

        verify(joinPoint).getSignature();
        verify(signature).getMethod();
        verify(rateLimiterConfig).getCombinedConfig("/api/v1/companies", "USER");
        verify(rateLimiterUtil).createBucket(combinedConfig);
        verify(rateLimiterUtil).tryConsume(bucket, 1L);
        verify(rateLimiterUtil).getAvailableTokens(bucket);
        verify(joinPoint, never()).proceed();
        verifyNoMoreInteractions(rateLimiterUtil, rateLimiterConfig, joinPoint, signature);
    }

    @Test
    @DisplayName("rateLimit: throws when request context is missing")
    void rateLimit_whenRequestContextMissing_throws() {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        RateLimiterError error = assertThrows(RateLimiterError.class, () -> aspect.rateLimit(joinPoint));

        assertEquals("Request context not available", error.getMessage());
        verifyNoInteractions(rateLimiterUtil, rateLimiterConfig, joinPoint);
    }

    @Test
    @DisplayName("rateLimit: proceeds without rate limiting when method is not annotated")
    void rateLimit_whenMethodNotAnnotated_proceedsWithoutRateLimit() throws Throwable {
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = TestHandlers.class.getDeclaredMethod("notAnnotated");
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenReturn("done");

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/companies");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));

        Object result = aspect.rateLimit(joinPoint);

        assertEquals("done", result);
        verify(joinPoint).getSignature();
        verify(signature).getMethod();
        verify(joinPoint).proceed();
        verifyNoInteractions(rateLimiterUtil, rateLimiterConfig);
        verifyNoMoreInteractions(joinPoint, signature);
    }

    private static final class TestHandlers {
        @RateLimit(tokens = 5, key = "company.create")
        @SuppressWarnings("unused")
        private void createCompany() {
        }

        @RateLimit(tokens = 1)
        @SuppressWarnings("unused")
        private void getActiveCompanies() {
        }

        @SuppressWarnings("unused")
        private void notAnnotated() {
        }
    }
}
