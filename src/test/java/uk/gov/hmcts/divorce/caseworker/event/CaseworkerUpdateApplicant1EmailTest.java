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
import uk.gov.hmcts.divorce.divorcecase.model.CaseInviteApp1;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerUpdateApplicant1Email.CASEWORKER_UPDATE_APP1_EMAIL;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;

@ExtendWith(MockitoExtension.class)
public class CaseworkerUpdateApplicant1EmailTest {

    @Mock
    private EmailUpdateService emailUpdateService;
    @InjectMocks
    private CaseworkerUpdateApplicant1Email caseworkerUpdateApplicant1Email;

    @Test
    void shouldAddConfigurationToConfigBuilder() throws Exception {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerUpdateApplicant1Email.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_UPDATE_APP1_EMAIL);
    }

    @Test
    void shouldReturnErrorsIfApplicant1EmailHasBeenRemovedInOnlineCase() {
        final CaseData caseDataBefore = CaseData.builder()
            .applicant1(Applicant.builder()
                .email(TEST_USER_EMAIL)
                .build())
            .build();

        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        detailsBefore.setId(TEST_CASE_ID);
        detailsBefore.setData(caseDataBefore);

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder().build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicant1Email.midEvent(details, detailsBefore);

        assertThat(response.getErrors())
            .isEqualTo(singletonList("Please use the 'Update offline status' event before removing the email address."));
    }

    @Test
    void shouldAllowApplicant1EmailRemovalInOfflineCase() {
        final CaseData caseDataBefore = CaseData.builder()
            .applicant1(Applicant.builder()
                .email(TEST_USER_EMAIL)
                .offline(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        detailsBefore.setId(TEST_CASE_ID);
        detailsBefore.setData(caseDataBefore);

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .offline(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicant1Email.midEvent(details, detailsBefore);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldAllowApplicant1EmailRemovalIfRepresentedCase() {
        final CaseData caseDataBefore = CaseData.builder()
            .applicant1(Applicant.builder()
                .email(TEST_USER_EMAIL)
                .solicitorRepresented(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        detailsBefore.setId(TEST_CASE_ID);
        detailsBefore.setData(caseDataBefore);

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .solicitorRepresented(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicant1Email.midEvent(details, detailsBefore);

        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldCallUpdateEmailServiceAndReturnCaseData() {
        final CaseData caseDataBefore = CaseData.builder()
            .applicant1(Applicant.builder()
                .offline(YES)
                .email("test@test.com")
                .build())
            .build();

        final CaseDetails<CaseData, State> detailsBefore = new CaseDetails<>();
        detailsBefore.setId(TEST_CASE_ID);
        detailsBefore.setData(caseDataBefore);

        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .offline(YES)
                .email(TEST_USER_EMAIL)
                .build())
            .build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        final CaseData expectedCaseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .offline(YES)
                .email(TEST_USER_EMAIL)
                .build())
            .caseInviteApp1(CaseInviteApp1.builder()
                .accessCodeApplicant1("ABCD1234")
                .applicant1InviteEmailAddress(TEST_USER_EMAIL)
                .build())
            .build();

        final CaseDetails<CaseData, State> expectedDetails = new CaseDetails<>();
        expectedDetails.setData(expectedCaseData);
        expectedDetails.setId(TEST_CASE_ID);

        when(emailUpdateService.processUpdateForApplicant1(details)).thenReturn(expectedDetails);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicant1Email.aboutToSubmit(details, detailsBefore);

        verify(emailUpdateService).processUpdateForApplicant1(details);
        verify(emailUpdateService).sendNotificationToOldEmail(detailsBefore, TEST_USER_EMAIL, true);
        assertThat(response.getData()).isEqualTo(expectedCaseData);
    }

    @Test
    void shouldNotCallUpdateEmailServiceWhenRepresented() {
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .offline(YES)
                .solicitorRepresented(YES)
                .build())
            .build();

        caseData.getApplicant1().setEmail(TEST_USER_EMAIL);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicant1Email.aboutToSubmit(details, details);

        verifyNoInteractions(emailUpdateService);
    }

    @Test
    void shouldNotCallUpdateEmailServiceWhenNotRepresentedAndEmailNotPresent() {
        final CaseData caseData = CaseData.builder()
            .applicant1(Applicant.builder()
                .offline(YES)
                .solicitorRepresented(NO)
                .build())
            .build();


        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setId(TEST_CASE_ID);
        details.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerUpdateApplicant1Email.aboutToSubmit(details, details);

        verifyNoInteractions(emailUpdateService);
    }
}
