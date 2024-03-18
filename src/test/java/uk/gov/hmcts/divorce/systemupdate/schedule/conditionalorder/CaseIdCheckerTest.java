package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaseIdCheckerTest {

    private final CaseIdChecker caseIdChecker = new CaseIdChecker();

    @Test
    void testIsCaseIdValid_ValidCaseId() {
        boolean isValid = caseIdChecker.isCaseIdValid(6277876);
        assertTrue(isValid);
    }

    @Test
    void testIsCaseIdValid_InvalidCaseId() {
        boolean isValid = caseIdChecker.isCaseIdValid(999L); // Assuming 999L is not in the test data
        assertFalse(isValid);
    }
}
