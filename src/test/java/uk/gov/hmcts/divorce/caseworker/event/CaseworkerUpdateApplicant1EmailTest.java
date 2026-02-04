package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.EmailUpdateService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateApplicant1Email.CASEWORKER_UPDATE_APP1_EMAIL;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
class CaseworkerUpdateApplicant1EmailTest {

    @Mock
    private EmailUpdateService emailUpdateService;
    @InjectMocks
    private CaseworkerUpdateApplicant1Email caseworkerUpdateApplicant1Email;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerUpdateApplicant1Email.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_UPDATE_APP1_EMAIL);
    }

    @Test
    void shouldReturnWarningInMidEventIfApplicable() {
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .offline(YES)
                .email(TEST_USER_EMAIL)
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(emailUpdateService.willApplicantBeMadeOffline(details, details, true)).thenReturn(true);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicant1Email.midEvent(details, details);

        verify(emailUpdateService).willApplicantBeMadeOffline(details, details, true);
        assertThat(response.getWarnings()).hasSize(1);
    }

    @Test
    void shouldNotReturnWarningInMidEventIfNotApplicable() {
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .offline(YES)
                .email(TEST_USER_EMAIL)
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(emailUpdateService.willApplicantBeMadeOffline(details, details, true)).thenReturn(false);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicant1Email.midEvent(details, details);

        verify(emailUpdateService).willApplicantBeMadeOffline(details, details, true);
        assertThat(response.getWarnings()).isEmpty();
    }

    @Test
    void shouldCallUpdateEmailServiceAndReturnCaseData() {
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .offline(YES)
                .email(TEST_USER_EMAIL)
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(emailUpdateService.processEmailUpdate(details, details, true)).thenReturn(details);

        caseworkerUpdateApplicant1Email.aboutToSubmit(details, details);

        verify(emailUpdateService).processEmailUpdate(details, details, true);
    }
}
