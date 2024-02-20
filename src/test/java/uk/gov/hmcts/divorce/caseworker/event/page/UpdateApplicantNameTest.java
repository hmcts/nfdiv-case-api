package uk.gov.hmcts.divorce.caseworker.event.page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UpdateApplicantNameTest {
    @InjectMocks
    private UpdateApplicantName updateApplicantName;
    private final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
    @BeforeEach
    void setUp() {
        caseDetails.setData(CaseData.builder()
            .applicant1(Applicant.builder()
                .firstName("Abc")
                .lastName("x")
                .build())
            .build());
    }

    @Test
    void midEventShouldReturnErrorsOnInvalidApplicantName() {
        AboutToStartOrSubmitResponse<CaseData, State> response = updateApplicantName.midEvent(caseDetails, caseDetails);
        assertThat(response.getErrors().size()).isEqualTo(1);
        assertEquals("Firstname and Lastname should have atleast 3 characters each.", response.getErrors().get(0));
    }

    @Test
    void midEventShouldNotReturnErrorsOnValidApplicantName() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(CaseData.builder()
            .applicant1(Applicant.builder()
                .firstName("Abcd")
                .lastName("xxxxx")
                .build())
            .build());
        AboutToStartOrSubmitResponse<CaseData, State> response = updateApplicantName.midEvent(caseDetails, caseDetails);
        assertNull(response.getErrors());
    }

    @Test
    void validApplicantNameTest(){
        boolean isValidName = updateApplicantName.validApplicantName(caseDetails.getData());
        assertThat(isValidName).isEqualTo(Boolean.FALSE);
    }
}
