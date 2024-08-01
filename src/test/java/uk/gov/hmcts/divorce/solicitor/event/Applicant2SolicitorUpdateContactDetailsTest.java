package uk.gov.hmcts.divorce.solicitor.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.CaseInfo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Solicitor;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorCreateApplicationService;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant1SolicitorUpdateContactDetails.INVALID_EMAIL_ERROR;
import static uk.gov.hmcts.divorce.solicitor.event.Applicant2SolicitorUpdateContactDetails.APP2_SOLICITOR_UPDATE_CONTACT_DETAILS;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicantWithAddress;

@ExtendWith(MockitoExtension.class)
class Applicant2SolicitorUpdateContactDetailsTest {

    @Mock
    private SolicitorCreateApplicationService solicitorCreateApplicationService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private Applicant2SolicitorUpdateContactDetails applicant2SolicitorUpdateContactDetails;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant2SolicitorUpdateContactDetails.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APP2_SOLICITOR_UPDATE_CONTACT_DETAILS);
    }

    @Test
    void shouldReturnErrorsWhenEmailValidationIsUnsuccessful() {
        Solicitor solicitor = Solicitor.builder().build();
        var caseDetails = caseDetailsWithApplicant2Solicitor(solicitor);

        when(solicitorCreateApplicationService.validateSolicitorOrganisationAndEmail(solicitor, TEST_CASE_ID, null))
            .thenReturn(invalidCaseInfoResult());

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorUpdateContactDetails.midEvent(caseDetails, null);

        assertThat(response.getErrors()).isEqualTo(List.of(INVALID_EMAIL_ERROR));
    }

    @Test
    void shouldNotReturnErrorsWhenEmailValidationIsSuccessful() {
        Solicitor solicitor = Solicitor.builder().build();
        var caseDetails = caseDetailsWithApplicant2Solicitor(solicitor);

        when(solicitorCreateApplicationService.validateSolicitorOrganisationAndEmail(solicitor, TEST_CASE_ID, null))
            .thenReturn(validCaseInfoResult());

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            applicant2SolicitorUpdateContactDetails.midEvent(caseDetails, null);

        assertThat(response.getErrors()).isNull();
    }

    private CaseDetails<CaseData, State> caseDetailsWithApplicant2Solicitor(Solicitor solicitor) {
        var applicant2 = getApplicantWithAddress();
        applicant2.setSolicitor(solicitor);

        CaseData data = CaseData
            .builder()
            .applicant2(applicant2)
            .build();

        return CaseDetails.<CaseData, State>builder()
            .data(data)
            .id(TEST_CASE_ID)
            .build();
    }

    private CaseInfo invalidCaseInfoResult() {
        return CaseInfo.builder()
            .errors(singletonList("Please select an organisation you belong to"))
            .build();
    }

    private CaseInfo validCaseInfoResult() {
        return CaseInfo.builder().build();
    }
}
