package uk.gov.hmcts.divorce.citizen.validation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.ValidationResponse;
import uk.gov.hmcts.divorce.common.model.ValidationStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class PetitionValidationServiceTest {

    @Autowired
    private PetitionValidationService petitionValidationService;

    private CaseData caseData = new CaseData();

    @BeforeEach
    public void setup() {
        assertNotNull(petitionValidationService);

        // Populate with valid data
        caseData.setDivorceCostsClaim(YesOrNo.YES);
    }

    @Test
    public void givenCaseIdWhenValidationIsCalledWithNoDataThenValidationWillFail() {
        assertEquals(ValidationStatus.FAILED.getValue(), petitionValidationService.validateCaseData(new CaseData()).getValidationStatus());
    }

    @Test
    public void givenCaseIdWhenValidationIsCalledWithValidDataThenValidationWillSucceed() {
        assertEquals(ValidationStatus.SUCCESS.getValue(), petitionValidationService.validateCaseData(caseData).getValidationStatus());
    }

    @Test
    public void givenCaseId_whenValidationIsCalledWithInvalidData_thenValidationWillFail() {
        caseData.setDivorceCostsClaim(null);
        ValidationResponse response = petitionValidationService.validateCaseData(caseData);
        assertEquals(ValidationStatus.FAILED.getValue(), response.getValidationStatus());
        assertEquals(1, response.getErrors().size());
    }

    @Test
    public void givenNullWhenValidationIsCalledWithValidDataThenValidationWillFail() {
        assertEquals(ValidationStatus.FAILED.getValue(), petitionValidationService.validateCaseData(null).getValidationStatus());
    }
}
