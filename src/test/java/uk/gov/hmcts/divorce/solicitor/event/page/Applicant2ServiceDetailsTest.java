package uk.gov.hmcts.divorce.solicitor.event.page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_SOLICITOR_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class Applicant2ServiceDetailsTest {

    private final Applicant2ServiceDetails page = new Applicant2ServiceDetails();

    @Test
    void shouldReturnErrorIfEmailValidationFailsForNonSolicitorCase() {
        final CaseData caseData = caseData();
        caseData.setApplicant2(Applicant.builder()
                .email("invalidEmail")
                .solicitorRepresented(NO)
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(1, response.getErrors().size());
    }

    @Test
    void shouldReturnErrorIfEmailValidationFailsForSolicitorCase() {
        final CaseData caseData = caseData();
        caseData.setApplicant2(Applicant.builder()
            .solicitorRepresented(YES)
                .solicitor(Solicitor.builder()
                    .email("invalidEmail")
                    .build())
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(1, response.getErrors().size());
    }

    @Test
    void shouldReturnNoErrorsIfEmailValidationPasses() {
        final CaseData caseData = caseData();
        caseData.setApplicant2(Applicant.builder()
            .solicitorRepresented(YES)
            .solicitor(Solicitor.builder()
                .email(TEST_SOLICITOR_EMAIL)
                .build())
            .email(TEST_USER_EMAIL)
            .build());

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);

        AboutToStartOrSubmitResponse<CaseData, State> response = page.midEvent(details, details);

        assertEquals(0, response.getErrors().size());
    }
}
