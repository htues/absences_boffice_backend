package com.hftamayo.absencesbobe.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Represents a business logic violation (domain rule/workflow/constraint violation).
 *
 * <p><strong>Fields:</strong>
 * <ul>
 *   <li><code>errorCode</code>: machine-readable code (e.g., "INVALID_STATE", "WORKFLOW_VIOLATION")</li>
 *   <li><code>operation</code>: operation being performed (e.g., "CREATE", "UPDATE", "DELETE")</li>
 *   <li><code>resource</code>: resource type affected (e.g., "Company")</li>
 *   <li><code>reason</code>: short explanation of the rule being violated</li>
 *   <li><code>message</code>: detailed human-readable description (used as <code>detail</code>)</li>
 * </ul>
 */
@AllArgsConstructor
@Getter
public class BusinessLogicError implements ErrorLogEventDescriptor {
    private final String errorCode;
    private final String operation;
    private final String resource;
    private final String reason;
    private final String message;

    @Override
    public ErrorApiResponse getType() {
        return ErrorApiResponse.BUSINESS_LOGIC_ERROR;
    }

    @Override
    public String getDetail() {
        return message;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Factory method for creating a business logic error for invalid state violations.
     * Use when an entity is in a state that violates business rules.
     *
     * @param operation        The operation being attempted
     * @param resource         The resource type
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
     * @param operation           The operation being attempted
     * @param resource            The resource type
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
     * @param operation             The operation being attempted
     * @param resource              The resource type
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
