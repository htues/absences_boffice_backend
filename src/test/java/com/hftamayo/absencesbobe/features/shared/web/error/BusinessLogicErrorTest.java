package com.hftamayo.absencesbobe.features.shared.web.error;

import com.hftamayo.absencesbobe.shared.web.constants.ErrorApiResponse;
import com.hftamayo.absencesbobe.shared.web.error.BusinessLogicError;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessLogicErrorTest {

	@Test
	void constructor_setsFieldsAndDescriptorMethods() {
		BusinessLogicError error = new BusinessLogicError(
				"BUSINESS_RULE_BROKEN",
				"UPDATE",
				"Company",
				"Workflow step required",
				"Cannot update company before approval"
		);

		assertEquals("BUSINESS_RULE_BROKEN", error.getErrorCode());
		assertEquals("UPDATE", error.getOperation());
		assertEquals("Company", error.getResource());
		assertEquals("Workflow step required", error.getReason());
		assertEquals("Cannot update company before approval", error.getMessage());

		assertEquals(ErrorApiResponse.BUSINESS_LOGIC_ERROR, error.getType());
		assertEquals("Cannot update company before approval", error.getDetail());
	}

	@Test
	void invalidState_buildsExpectedBusinessLogicError() {
		BusinessLogicError error = BusinessLogicError.invalidState(
				"DELETE",
				"Company",
				"Company is already archived"
		);

		assertEquals("INVALID_STATE", error.getErrorCode());
		assertEquals("DELETE", error.getOperation());
		assertEquals("Company", error.getResource());
		assertEquals("Entity is in an invalid state for the requested operation", error.getReason());
		assertEquals("Company is already archived", error.getMessage());
	}

	@Test
	void workflowViolation_buildsExpectedBusinessLogicError() {
		BusinessLogicError error = BusinessLogicError.workflowViolation(
				"CREATE",
				"Company",
				"Manager approval is required before creation"
		);

		assertEquals("WORKFLOW_VIOLATION", error.getErrorCode());
		assertEquals("CREATE", error.getOperation());
		assertEquals("Company", error.getResource());
		assertEquals("Operation violates business workflow rules", error.getReason());
		assertEquals("Manager approval is required before creation", error.getMessage());
	}

	@Test
	void constraintViolation_buildsExpectedBusinessLogicError() {
		BusinessLogicError error = BusinessLogicError.constraintViolation(
				"CREATE",
				"Company",
				"Maximum active companies limit reached"
		);

		assertEquals("CONSTRAINT_VIOLATION", error.getErrorCode());
		assertEquals("CREATE", error.getOperation());
		assertEquals("Company", error.getResource());
		assertEquals("Operation violates business constraints", error.getReason());
		assertEquals("Maximum active companies limit reached", error.getMessage());
	}

	@Test
	void toString_returnsFormattedSummary() {
		BusinessLogicError error = new BusinessLogicError(
				"INVALID_STATE",
				"UPDATE",
				"Company",
				"Invalid transition",
				"Cannot move from ACTIVE to DRAFT"
		);

		assertEquals(
				"Error Code: INVALID_STATE, Operation: UPDATE, Resource: Company, Reason: Invalid transition, Message: Cannot move from ACTIVE to DRAFT",
				error.toString()
		);
	}
}
