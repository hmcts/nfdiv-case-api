package uk.gov.hmcts.divorce.citizen.validation.service;

import com.deliveredtechnologies.rulebook.FactMap;
import com.deliveredtechnologies.rulebook.NameValueReferableMap;
import com.deliveredtechnologies.rulebook.annotation.Result;
import com.deliveredtechnologies.rulebook.model.RuleBook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.ValidationResponse;
import uk.gov.hmcts.divorce.common.model.ValidationStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PetitionValidationServiceTest {

    @InjectMocks
    private PetitionValidationService petitionValidationService;

    @Mock
    private RuleBook<List<String>> submittedCaseDataRuleBook;

    private CaseData caseData = new CaseData();
    NameValueReferableMap<CaseData> facts = new FactMap<>();

    @BeforeEach
    public void setup() {

        // Populate with valid data
        caseData.setDivorceCostsClaim(YesOrNo.YES);
    }

    @Test
    public void givenCaseIdWhenValidationIsCalledWithNoDataThenValidationWillFail() {
        ValidationResponse validationResponse = ValidationResponse.builder()
            .validationStatus(ValidationStatus.FAILED.getValue())
            .build();

        Optional<Result<List<String>>> result = Optional.empty();

        doNothing().when(submittedCaseDataRuleBook).run(any());

        when(submittedCaseDataRuleBook.getResult()).thenReturn(result);

        when(petitionValidationService.validateCaseData(new CaseData())).thenReturn(validationResponse);

        assertNotNull(petitionValidationService);
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
