package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.error.exception.BusinessError;
import com.hftamayo.java.boabsenses.utilities.constants.ApiResponseMessages;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a business logic violation - a flaw in the design and implementation
 * that allows unintended behavior when users interact with the application in unexpected ways.
 *
 * <p>Business logic errors occur when:
 * <ul>
 *   <li>Users interact with the application in ways developers didn't anticipate</li>
 *   <li>Flawed assumptions about user behavior lead to rule violations</li>
 *   <li>Domain-specific workflows or constraints are violated</li>
 *   <li>State transitions or business rules are bypassed</li>
 * </ul>
 *
 * <p><strong>When to use BusinessLogicError:</strong>
 * <ul>
 *   <li>Domain rule violations (e.g., "Cannot delete company with active employees")</li>
 *   <li>Workflow state violations (e.g., "Cannot activate deleted company without restoration")</li>
 *   <li>Business constraint violations (e.g., "Operation not allowed in current state")</li>
 *   <li>Unexpected interaction patterns that violate business assumptions</li>
 * </ul>
 *
 * <p><strong>When NOT to use BusinessLogicError:</strong>
 * <ul>
 *   <li>Input format/type validation → use {@link ValidationError}</li>
 *   <li>Runtime request parameter issues (e.g., page out of bounds) → use {@link ValidationError}</li>
 *   <li>Resource not found → use {@link ResourceNotFoundError}</li>
 *   <li>Duplicate resources → use {@link DuplicateResourceError}</li>
 * </ul>
 *
 * <p>Reference: <a href="https://portswigger.net/web-security/logic-flaws">PortSwigger - Business Logic Vulnerabilities</a>
 *
 * @param errorCode Machine-readable error code (e.g., "INVALID_STATE", "WORKFLOW_VIOLATION")
 * @param operation The operation being performed (e.g., "CREATE", "UPDATE", "DELETE")
 * @param resource The resource type affected (e.g., "Company", "User")
 * @param reason Short description of why the business rule was violated
 * @param message Detailed human-readable message explaining the violation
 */
@AllArgsConstructor
@Getter
public class BusinessLogicError implements BusinessError {
    private final String errorCode;
    private final String operation;
    private final String resource;
    private final String reason;
    private final String message;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getTitle() {
        return ApiResponseMessages.BUSINESS_LOGIC_ERROR.getMessageKey();
    }

    @Override
    public int getStatus() {
        return ApiResponseMessages.BUSINESS_LOGIC_ERROR.getStatusCode();
    }

    @Override
    public String getDetail() {
        return message;
    }

    @Override
    public String getCode() {
        return errorCode != null ? errorCode : ApiResponseMessages.BUSINESS_LOGIC_ERROR.getMessageKey();
    }

    /**
     * Factory method for creating a business logic error for invalid state violations.
     * Use when an entity is in a state that violates business rules.
     *
     * @param operation The operation being attempted
     * @param resource The resource type
     * @param stateDescription Description of the invalid state
     * @return BusinessLogicError instance
     */
    public static BusinessLogicError invalidState(String operation, String resource, String stateDescription) {
        return new BusinessLogicError(
                "INVALID_STATE",
                operation,
                resource,
                "Entity is in an invalid state for the requested operation",
                stateDescription
        );
    }

    /**
     * Factory method for creating a business logic error for workflow violations.
     * Use when an operation violates a business workflow or process.
     *
     * @param operation The operation being attempted
     * @param resource The resource type
     * @param workflowDescription Description of the workflow violation
     * @return BusinessLogicError instance
     */
    public static BusinessLogicError workflowViolation(String operation, String resource, String workflowDescription) {
        return new BusinessLogicError(
                "WORKFLOW_VIOLATION",
                operation,
                resource,
                "Operation violates business workflow rules",
                workflowDescription
        );
    }

    /**
     * Factory method for creating a business logic error for constraint violations.
     * Use when a business constraint or limit is exceeded.
     *
     * @param operation The operation being attempted
     * @param resource The resource type
     * @param constraintDescription Description of the constraint violation
     * @return BusinessLogicError instance
     */
    public static BusinessLogicError constraintViolation(String operation, String resource, String constraintDescription) {
        return new BusinessLogicError(
                "CONSTRAINT_VIOLATION",
                operation,
                resource,
                "Operation violates business constraints",
                constraintDescription
        );
    }

    @Override
    public String toString() {
        return String.format("Error Code: %s, Operation: %s, Resource: %s, Reason: %s, Message: %s",
                errorCode, operation, resource, reason, message);
    }
}
