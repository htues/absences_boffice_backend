# Log Event Pipeline Blueprint

## Objective

Provide a standardized structured logging pipeline for application events such as:

- `INFO` success events
- `WARN` validation/business events
- `ERROR` unknown/unhandled exceptions

These logs are designed to be consumed later by an independent event digester service.

---

## Main Contracts

### Digester-facing event contract

- `ApplicationLogEventDto`

### Event vocabulary

- `SuccessApiResponse`
- `ErrorApiResponse`

### Correlation contract

- Header: `X-Correlation-ID`
- MDC key: `correlationId`

---

## Event Payload Shape

Each structured event follows this scaffold:
```
{
"timestamp": "2026-09-10T10:15:30Z",
"severity": "INFO | WARN | ERROR",
"eventType": "API_SUCCESS | VALIDATION_ERROR | BUSINESS_ERROR | UNKNOWN_ERROR",
"eventCode": "ENTITY_CREATED | VALIDATION_ERROR | UNKNOWN_ERROR",
"message": "Human-readable or message-key text",
"detail": "Optional technical/detail message",
"statusCode": 200,
"correlationId": "uuid-or-client-provided-id",
"traceId": null,
"path": "/api/companies/123",
"httpMethod": "GET",
"source": "CompanyCommandController",
"context": {
"responseType": "success | error",
"errorCode": "NOT_FOUND",
"exception": "java.lang.RuntimeException"
}
}
```
---

## Pipeline Flow
```
HTTP Request
|
v
CorrelationIdFilter
- Reads X-Correlation-ID header
- Generates UUID if missing
- Stores ID in request attribute
- Stores ID in MDC as correlationId
- Adds X-Correlation-ID to HTTP response
  |
  v
  Controller / Handler / Service Boundary
  |
  v
  ApplicationLogEventFactory
- Builds ApplicationLogEventDto
- Adds timestamp
- Adds severity
- Adds eventType
- Adds eventCode
- Adds statusCode
- Adds correlationId
- Adds request path
- Adds HTTP method
- Adds source class
- Adds optional context
  |
  v
  ApplicationEventLogger
- Receives ApplicationLogEventDto
- Adds correlation/event fields to MDC
- Logs through SLF4J
  |
  v
  Logback
- Development/Test: readable console logs
- Staging/Production: JSON structured logs
  |
  v
  Log Collector / Event Digester Microservice
```
---

## Component Responsibilities

### `CorrelationIdFilter`

Responsible for request-level traceability.
```
Input:
- Incoming HTTP request

Output:
- Request attribute: correlationId
- Response header: X-Correlation-ID
- MDC key: correlationId
```
---

### `ApplicationLogEventFactory`

Responsible for creating structured event payloads.

Factory methods:
```
fromSuccess(...)
fromBusinessException(...)
fromValidationException(...)
fromConstraintViolationException(...)
fromUnreadableBodyException(...)
fromUnknownException(...)
```
Produces:
```
ApplicationLogEventDto
```
---

### `ApplicationEventLogger`

Responsible for emitting events.

Methods:
```
info(event)
warn(event)
error(event)
error(event, throwable)
```
Behavior:
```
INFO  -> successful API events
WARN  -> validation/business expected failures
ERROR -> unknown/unhandled failures
```
---

### `GlobalValidationExceptionHandler`

Responsible for web-layer exception handling.

Handles:
```
MethodArgumentNotValidException      -> WARN / VALIDATION_ERROR
ConstraintViolationException         -> WARN / VALIDATION_ERROR
HttpMessageNotReadableException      -> WARN / VALIDATION_ERROR
Exception                            -> ERROR / UNKNOWN_ERROR
```
Important rule:
```
Internal exception details are logged internally,
but not exposed to frontend responses.
```
---

### `ApiResponseFactory`

Responsible only for frontend/API responses.

It should not build log events.

Responsibilities:
```
fromResult(...)
success(...)
error(...)
unknownError(...)
```
---

## Event Types

| Event Type | Severity | Description |
|---|---:|---|
| `API_SUCCESS` | `INFO` | Successful API request |
| `VALIDATION_ERROR` | `WARN` | Invalid body, params, or malformed JSON |
| `BUSINESS_ERROR` | `WARN` | Expected domain/application failure |
| `UNKNOWN_ERROR` | `ERROR` | Unhandled exception / zero-day catcher |

---

## Severity Rules

| Scenario | Severity |
|---|---|
| Successful create/update/delete/read | `INFO` |
| Validation failure | `WARN` |
| Business rule failure | `WARN` |
| Not found / duplicate resource | `WARN` |
| Rate limit exceeded | `WARN` |
| Unauthorized / forbidden | `WARN` |
| Unexpected runtime exception | `ERROR` |

---

## Correlation ID Rules

Standard header:
```
X-Correlation-ID
```
Standard MDC key:
```
correlationId
```
Rules:
```
If request has X-Correlation-ID:
use it

If request does not have X-Correlation-ID:
generate UUID

Always:
store in request attribute
store in MDC during request
return in response header
include in ApplicationLogEventDto
```
---

## Example Success Event
```
{
"severity": "INFO",
"eventType": "API_SUCCESS",
"eventCode": "ENTITY_CREATED",
"message": "ENTITY_CREATED",
"detail": "API request completed successfully",
"statusCode": 201,
"correlationId": "6f6c8b8d-5e95-4f2c-95aa-8e7f8a0c8a33",
"path": "/api/companies",
"httpMethod": "POST",
"source": "CompanyCommandController",
"context": {
"responseType": "success"
}
}
```
---

## Example Validation Event
```
{
"severity": "WARN",
"eventType": "VALIDATION_ERROR",
"eventCode": "VALIDATION_ERROR",
"message": "Request body validation failed",
"detail": "Validation failed for argument...",
"statusCode": 422,
"correlationId": "corr-123",
"path": "/api/companies",
"httpMethod": "POST",
"source": "GlobalValidationExceptionHandler",
"context": {
"responseType": "error",
"exception": "MethodArgumentNotValidException",
"fieldErrorCount": 2
}
}
```
---

## Example Unknown Error Event
```
{
"severity": "ERROR",
"eventType": "UNKNOWN_ERROR",
"eventCode": "UNKNOWN_ERROR",
"message": "Unexpected application error",
"detail": "boom",
"statusCode": 500,
"correlationId": "corr-500",
"path": "/api/companies/123",
"httpMethod": "PUT",
"source": "GlobalValidationExceptionHandler",
"context": {
"responseType": "error",
"exception": "java.lang.RuntimeException"
}
}
```
---

## Frontend vs Digester Contracts

### Frontend contract
```
ApiResponseDto
```
Used for HTTP responses.

### Digester contract
```
ApplicationLogEventDto
```
Used for structured logs.

Rule:
```
Never use ApiResponseDto as the digester event payload.
Never expose internal exception details through ApiResponseDto.
```
---

## Production Logging

In production-like profiles, Logback emits JSON logs.
```
staging
production
prod
```
These logs are suitable for:
```
Logstash
Fluent Bit
Filebeat
OpenSearch
Elasticsearch
event digester microservice
```
---

## Final Architecture
```
ApiResponseDto
= frontend response contract

ApplicationLogEventDto
= digester event contract

CorrelationIdFilter
= request traceability

ApplicationLogEventFactory
= event creation

ApplicationEventLogger
= event emission

logback-spring.xml
= JSON log formatting

event digester microservice
= downstream consumer
```