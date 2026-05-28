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
import uk.gov.hmcts.divorce.common.service.CaseTerminationService;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorWithdrawn.CASE_WITHDRAWN_CONFIRMATION_HEADER;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorWithdrawn.CASE_WITHDRAWN_CONFIRMATION_LABEL;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorWithdrawn.SOLICITOR_WITHDRAWN;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SolicitorWithdrawnTest {
    @Mock
    private CaseTerminationService caseTerminationService;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private SolicitorWithdrawn solicitorWithdrawn;

    @Test
    void configure() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        solicitorWithdrawn.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_WITHDRAWN);
    }

    @Test
    void shouldWithdrawCaseByDelegatingToWithdrawCaseService() {
        final var beforeDetails = getCaseDetails(false);
        final var details = getCaseDetails(false);

        solicitorWithdrawn.aboutToSubmit(details, beforeDetails);

        verify(caseTerminationService).withdraw(details);
    }

    @Test
    void shouldReturnConfirmationTextOnSubmission() {
        final var details = getCaseDetails(false);
        final var beforeDetails = getCaseDetails(false);

        var result = solicitorWithdrawn.submitted(details, beforeDetails);

        assertThat(result.getConfirmationHeader()).isEqualTo(CASE_WITHDRAWN_CONFIRMATION_HEADER);
        assertThat(result.getConfirmationBody()).isEqualTo(
            String.format(CASE_WITHDRAWN_CONFIRMATION_LABEL)
        );
    }

    @Test
    void shouldSetCaseStateToWithdrawnWhenNotSubmitted() {
        final var details = getCaseDetails(false);
        final var beforeDetails = getCaseDetails(false);

        var response = solicitorWithdrawn.aboutToSubmit(details, beforeDetails);

        assertThat(response.getState()).isEqualTo(State.Withdrawn);
    }

    @Test
    void shouldSetCaseStateToPendingRefundWhenCaseIsSubmitted() {
        final var details = getCaseDetails(true);
        final var beforeDetails = getCaseDetails(true);

        var response = solicitorWithdrawn.aboutToSubmit(details, beforeDetails);

        assertThat(response.getState()).isEqualTo(State.PendingRefund);
    }

    @Test
    void shouldAllowApplicantSolicitorToWithdrawCaseInAllowedState() {
        final var details = getCaseDetails(false);
        details.setState(State.Draft);
        details.getData().setApplicationType(ApplicationType.SOLE_APPLICATION);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant2(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(false);

        var response = solicitorWithdrawn.aboutToStart(details);

        assertThat(response.getErrors()).isNullOrEmpty();
    }

    @Test
    void shouldAllowApplicant1SolicitorToWithdrawCaseInAllowedState() {
        final var details = getCaseDetails(false);
        details.setState(State.AwaitingApplicant2Response);
        details.getData().setApplicationType(ApplicationType.JOINT_APPLICATION);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant2(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(false);

        var response = solicitorWithdrawn.aboutToStart(details);

        assertThat(response.getErrors()).isNullOrEmpty();
    }

    @Test
    void shouldAllowApplicant2SolicitorToWithdrawCaseInAllowedState() {
        final var details = getCaseDetails(false);
        details.setState(State.Applicant2Approved);
        details.getData().setApplicationType(ApplicationType.JOINT_APPLICATION);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant2(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(true);

        var response = solicitorWithdrawn.aboutToStart(details);

        assertThat(response.getErrors()).isNullOrEmpty();
    }

    @Test
    void shouldNotAllowRespondentSolicitorToWithdrawCase() {
        final var details = getCaseDetails(false);
        details.setState(State.Submitted);
        details.getData().setApplicationType(ApplicationType.SOLE_APPLICATION);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant2(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(true);

        var response = solicitorWithdrawn.aboutToStart(details);

        assertThat(response.getErrors()).contains(SolicitorWithdrawn.RESPONDENT_SOLICITOR_ERROR);
    }

    @Test
    void shouldAllowSolicitorToWithdrawCaseIfCaseNotIssuedYet() {
        final var details = getCaseDetails(true);
        details.setState(State.Submitted);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant2(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(false);

        var response = solicitorWithdrawn.aboutToStart(details);

        assertThat(response.getErrors()).isNullOrEmpty();
    }

    @Test
    void shouldNotAllowSolicitorToWithdrawWhenCaseIsIssued() {
        final var details = getCaseDetails(true);
        details.getData().getApplication().setIssueDate(LocalDate.now());
        details.setState(State.Holding);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant2(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(false);

        var response = solicitorWithdrawn.aboutToStart(details);

        assertThat(response.getErrors()).contains(SolicitorWithdrawn.CANNOT_WITHDRAW_CASE);
    }

    private CaseDetails<CaseData, State> getCaseDetails(boolean caseSubmitted) {
        final var details = new CaseDetails<CaseData, State>();
        final var data = caseData();
        if (caseSubmitted) {
            data.getApplication().setDateSubmitted(LocalDateTime.now());
        }
        details.setData(data);
        details.setId(TEST_CASE_ID);

        return details;
    }
}
