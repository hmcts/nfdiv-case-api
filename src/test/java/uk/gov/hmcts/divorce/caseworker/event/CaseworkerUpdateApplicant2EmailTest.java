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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateApplicant2Email.CASEWORKER_UPDATE_APP2_EMAIL;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
public class CaseworkerUpdateApplicant2EmailTest {

    @Mock
    private EmailUpdateService emailUpdateService;
    @InjectMocks
    private CaseworkerUpdateApplicant2Email caseworkerUpdateApplicant2Email;

    @Test
    void shouldAddConfigurationToConfigBuilder() throws Exception {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerUpdateApplicant2Email.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_UPDATE_APP2_EMAIL);
    }

    @Test
    void shouldReturnErrorsIfApplicant2EmailHasBeenRemoved() {
        final CaseData caseDataBefore = CaseData.builder()
            .applicant2(Applicant.builder()
                .email(TEST_USER_EMAIL)
                .build())
            .build();

        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        detailsBefore.setId(TEST_CASE_ID);
        detailsBefore.setData(caseDataBefore);

        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder().build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicant2Email.midEvent(details, detailsBefore);

        assertThat(response.getErrors())
            .isEqualTo(singletonList("Email address should not be removed or blanked out."));
    }

    @Test
    void shouldNotReturnErrorsIfApplicant2EmailUnchanged() {
        final CaseData caseDataBefore = CaseData.builder()
            .applicant2(Applicant.builder()
                .email(TEST_USER_EMAIL)
                .build())
            .build();

        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        detailsBefore.setId(TEST_CASE_ID);
        detailsBefore.setData(caseDataBefore);

        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder().email(TEST_USER_EMAIL).build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicant2Email.midEvent(details, detailsBefore);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldCallUpdateEmailServiceAndReturnCaseData() {
        final CaseData caseData = CaseData.builder()
            .applicant2(Applicant.builder()
                .offline(YES)
                .email(TEST_USER_EMAIL)
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        when(emailUpdateService.processEmailUpdate(details, details, false)).thenReturn(details);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicant2Email.aboutToSubmit(details, details);

        verify(emailUpdateService).processEmailUpdate(details, details, false);
    }
}
