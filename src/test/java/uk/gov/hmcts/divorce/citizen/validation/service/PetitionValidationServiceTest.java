package uk.gov.hmcts.divorce.citizen.validation.service;
import com.deliveredtechnologies.rulebook.Result;
import com.deliveredtechnologies.rulebook.model.RuleBook;
import org.assertj.core.api.Assertions;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    @BeforeEach
    public void setup() {
        // Populate with some valid data
        caseData.setDivorceCostsClaim(YesOrNo.YES);
    }

    @Test
    public void givenNoRulebookErrorsThenValidationWillSucceed() {
        Result<List<String>> result = new Result<>(Collections.emptyList());
        doNothing().when(submittedCaseDataRuleBook).run(any());
        when(submittedCaseDataRuleBook.getResult()).thenReturn(Optional.of(result));
        ValidationResponse validationResponse = petitionValidationService.validateCaseData(caseData);
        Assertions.assertThat(validationResponse.getValidationStatus()).isEqualTo(ValidationStatus.SUCCESS.getValue());
        Assertions.assertThat(validationResponse.getErrors()).isNull();
    }

    @Test
    public void givenRulebookErrorsThenValidationWillFail() {
        List<String> errors = List.of("Some error during validation");
        Result<List<String>> result = new Result<>(errors);
        doNothing().when(submittedCaseDataRuleBook).run(any());
        when(submittedCaseDataRuleBook.getResult()).thenReturn(Optional.of(result));
        ValidationResponse validationResponse = petitionValidationService.validateCaseData(caseData);
        Assertions.assertThat(validationResponse.getValidationStatus()).isEqualTo(ValidationStatus.FAILED.getValue());
        Assertions.assertThat(validationResponse.getErrors()).contains("Some error during validation");
    }
}
