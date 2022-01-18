package uk.gov.hmcts.divorce.common.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.citizen.notification.Applicant2ApprovedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.HelpWithFees;
import uk.gov.hmcts.divorce.divorcecase.model.Jurisdiction;
import uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.DivorceApplicationJointTemplateContent;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.divorce.common.event.Applicant2Approve.APPLICANT_2_APPROVE;
import static uk.gov.hmcts.divorce.divorcecase.model.Application.ThePrayer.I_CONFIRM;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.Gender.MALE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getApplicant;

@ExtendWith(MockitoExtension.class)
class Applicant2ApproveTest {

    @Mock
    private Applicant2ApprovedNotification applicant2ApprovedNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private DivorceApplicationJointTemplateContent divorceApplicationJointTemplateContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private Applicant2Approve applicant2Approve;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        applicant2Approve.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(APPLICANT_2_APPROVE);
    }

    @Test
    void givenEventStartedWithEmptyCaseThenGiveValidationErrors() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = applicant2Approve.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors().size()).isEqualTo(5);
        assertThat(response.getErrors().get(0)).isEqualTo("Applicant2FirstName cannot be empty or null");
    }

    @Test
    void givenEventStartedWithInvalidCaseThenGiveValidationErrors() {
        CaseData caseData = caseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = applicant2Approve.aboutToSubmit(details, details);

        assertThat(response.getErrors().size()).isEqualTo(5);
        assertThat(response.getErrors()).containsExactlyInAnyOrder(
            "Applicant2StatementOfTruth cannot be empty or null",
            "Applicant2PrayerHasBeenGiven cannot be empty or null",
            "MarriageApplicant2Name cannot be empty or null",
            "Applicant2FirstName cannot be empty or null",
            "Applicant2LastName cannot be empty or null"
        );
    }

    @Test
    void givenEventStartedWithValidCaseThenChangeStateApplicant2ApprovedAndSendEmailToApplicant1AndApplicant2() {
        setMockClock(clock);
        final long caseId = 2L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        setValidCaseData(caseData);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        caseDetails.setState(State.AwaitingApplicant2Response);

        final AboutToStartOrSubmitResponse<CaseData, State> response = applicant2Approve.aboutToSubmit(caseDetails, caseDetails);

        verify(notificationDispatcher).send(applicant2ApprovedNotification, caseData, caseDetails.getId());
        assertThat(response.getState()).isEqualTo(State.Applicant2Approved);
    }

    @Test
    void givenEventStartedWithValidCaseThenChangeStateApplicant2ApprovedAndSendEmailToApplicant1AndApplicant2WithDeniedHwf() {
        setMockClock(clock);
        final long caseId = 2L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder().build();
        setValidCaseData(caseData);
        caseData.getApplication().getApplicant1HelpWithFees().setNeedHelp(YesOrNo.YES);
        caseData.getApplication().getApplicant2HelpWithFees().setNeedHelp(YesOrNo.NO);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        caseDetails.setState(State.AwaitingApplicant2Response);

        final AboutToStartOrSubmitResponse<CaseData, State> response = applicant2Approve.aboutToSubmit(caseDetails, caseDetails);

        verify(notificationDispatcher).send(applicant2ApprovedNotification, caseData, caseDetails.getId());
        assertThat(response.getState()).isEqualTo(State.Applicant2Approved);
    }

    private CaseData setValidCaseData(CaseData caseData) {
        caseData.setApplicant1(getApplicant());
        caseData.setApplicant2(getApplicant(MALE));
        caseData.getApplicant1().setContactDetailsType(PUBLIC);
        caseData.getApplication().setApplicant1HelpWithFees(
            HelpWithFees.builder()
                .needHelp(NO)
                .build()
        );

        caseData.getApplication().setApplicant2HelpWithFees(
            HelpWithFees.builder()
                .needHelp(NO)
                .build()
        );

        caseData.getApplication().setApplicant2PrayerHasBeenGivenCheckbox(Set.of(I_CONFIRM));
        caseData.getApplication().setApplicant2StatementOfTruth(YesOrNo.YES);
        caseData.getApplication().getMarriageDetails().setApplicant1Name("Full name");
        caseData.getApplication().getMarriageDetails().setApplicant2Name("Full name");

        caseData.getApplication().getMarriageDetails().setDate(LocalDate.now().minus(2, ChronoUnit.YEARS));
        caseData.getApplication().setApplicant2ConfirmApplicant1Information(YesOrNo.YES);

        Jurisdiction jurisdiction = new Jurisdiction();
        jurisdiction.setConnections(Set.of(JurisdictionConnections.APP_1_APP_2_LAST_RESIDENT));
        jurisdiction.setBothLastHabituallyResident(YesOrNo.YES);
        caseData.getApplication().setJurisdiction(jurisdiction);
        return caseData;
    }
}
