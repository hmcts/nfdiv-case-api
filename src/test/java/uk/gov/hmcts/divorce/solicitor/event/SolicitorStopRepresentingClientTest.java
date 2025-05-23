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
import uk.gov.hmcts.ccd.sdk.type.Organisation;
import uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy;
import uk.gov.hmcts.divorce.caseworker.event.NoticeType;
import uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService;
import uk.gov.hmcts.divorce.caseworker.service.NoticeOfChangeService;
import uk.gov.hmcts.divorce.citizen.notification.NocCitizenToSolsNotifications;
import uk.gov.hmcts.divorce.citizen.notification.NocSolRemovedCitizenNotification;
import uk.gov.hmcts.divorce.citizen.notification.NocSolRemovedSelfAsRepresentativeNotification;
import uk.gov.hmcts.divorce.citizen.notification.NocSolsToCitizenNotifications;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange;
import uk.gov.hmcts.divorce.divorcecase.model.NoticeOfChange.WhichApplicant;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.noticeofchange.model.ChangeOfRepresentationAuthor;
import uk.gov.hmcts.divorce.noticeofchange.service.ChangeOfRepresentativeService;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.solicitor.service.SolicitorValidationService;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_1_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.APPLICANT_2_SOLICITOR;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorStopRepresentingClient.REPRESENTATIVE_REMOVED_CONFIRMATION_HEADER;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorStopRepresentingClient.REPRESENTATIVE_REMOVED_CONFIRMATION_LABEL;
import static uk.gov.hmcts.divorce.solicitor.event.SolicitorStopRepresentingClient.SOLICITOR_STOP_REPRESENTING_CLIENT;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_FIRST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_LAST_NAME;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_ORG_ID;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.applicantRepresentedBySolicitor;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SolicitorStopRepresentingClientTest {

    @Mock
    private NoticeOfChangeService noticeOfChangeService;

    @Mock
    private SolicitorValidationService solicitorValidationService;

    @Mock
    private ChangeOfRepresentativeService changeOfRepresentativeService;

    @Mock
    private NocCitizenToSolsNotifications nocCitizenToSolsNotifications;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private NocSolRemovedSelfAsRepresentativeNotification nocSolRemovedSelfNotifications;

    @Mock
    private NocSolsToCitizenNotifications nocSolsToCitizenNotifications;

    @Mock
    private CaseFlagsService caseFlagsService;

    @Mock
    private NocSolRemovedCitizenNotification nocSolRemovedCitizenNotification;

    @InjectMocks
    private SolicitorStopRepresentingClient noticeOfChange;

    @Test
    void configure() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        noticeOfChange.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SOLICITOR_STOP_REPRESENTING_CLIENT);
    }

    @Test
    void shouldApplyNoticeOfChangeDecisionForApplicant1() {
        final var beforeDetails = getCaseDetails();
        final var details = getCaseDetails();
        details.setId(TEST_CASE_ID);
        details.getData().getApplicant1().getSolicitor().getOrganisationPolicy().setOrganisation(
            Organisation.builder()
                .organisationId(TEST_ORG_ID)
                .build()
        );
        details.getData().getApplicant2().getSolicitor().getOrganisationPolicy().setOrganisation(
            Organisation.builder()
                .organisationId(TEST_ORG_ID)
                .build()
        );

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(true);

        List<String> roles = List.of(CREATOR.getRole(), APPLICANT_1_SOLICITOR.getRole());

        var result = noticeOfChange.aboutToSubmit(details, beforeDetails);

        verify(changeOfRepresentativeService).buildChangeOfRepresentative(
            details.getData(),
            beforeDetails.getData(),
            ChangeOfRepresentationAuthor.SOLICITOR_STOP_REPRESENTING_CLIENT.getValue(),
            true
        );
        verify(noticeOfChangeService).revokeCaseAccess(TEST_CASE_ID, beforeDetails.getData().getApplicant1(), roles);
        assertSolicitorRemoved(result.getData().getApplicant1(), APPLICANT_1_SOLICITOR);
        assertSolicitorNotRemoved(result.getData().getApplicant2());
    }

    @Test
    void shouldApplyNoticeOfChangeDecisionForApplicant2() {
        final var details = getCaseDetails();
        final var beforeDetails = getCaseDetails();
        details.setId(TEST_CASE_ID);
        details.getData().getApplicant1().getSolicitor().getOrganisationPolicy().setOrganisation(
            Organisation.builder()
                .organisationId(TEST_ORG_ID)
                .build()
        );
        details.getData().getApplicant2().getSolicitor().getOrganisationPolicy().setOrganisation(
            Organisation.builder()
                .organisationId(TEST_ORG_ID)
                .build()
        );

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(false);

        List<String> roles = List.of(APPLICANT_2.getRole(), APPLICANT_2_SOLICITOR.getRole());

        var result = noticeOfChange.aboutToSubmit(details, beforeDetails);

        verify(changeOfRepresentativeService).buildChangeOfRepresentative(
            details.getData(),
            beforeDetails.getData(),
            ChangeOfRepresentationAuthor.SOLICITOR_STOP_REPRESENTING_CLIENT.getValue(),
            false
        );
        verify(noticeOfChangeService).revokeCaseAccess(TEST_CASE_ID, beforeDetails.getData().getApplicant2(), roles);
        assertSolicitorRemoved(result.getData().getApplicant2(), APPLICANT_2_SOLICITOR);
        assertSolicitorNotRemoved(result.getData().getApplicant1());
    }

    @Test
    void shouldSendNotificationAndReturnConfirmationTextContainingApplicant1Details() {
        final var details = getCaseDetails();
        final var beforeDetails = getCaseDetails();
        Applicant applicant = details.getData().getApplicant1();
        details.getData().setNoticeOfChange(
            NoticeOfChange.builder().whichApplicant(WhichApplicant.APPLICANT_1).build()
        );
        applicant.setFirstName(TEST_FIRST_NAME);
        applicant.setLastName(TEST_LAST_NAME);

        var result = noticeOfChange.submitted(details, beforeDetails);

        verify(notificationDispatcher).sendNOC(nocSolRemovedSelfNotifications, details.getData(),
            beforeDetails.getData(), details.getId(), true, NoticeType.ORG_REMOVED);
        assertThat(result.getConfirmationHeader()).isEqualTo(REPRESENTATIVE_REMOVED_CONFIRMATION_HEADER);
        assertThat(result.getConfirmationBody()).isEqualTo(
            String.format(REPRESENTATIVE_REMOVED_CONFIRMATION_LABEL, applicant.getFullName())
        );
    }

    @Test
    void shouldSendNotificationAndReturnConfirmationTextContainingApplicant2Details() {
        final var details = getCaseDetails();
        final var beforeDetails = getCaseDetails();
        Applicant applicant = details.getData().getApplicant2();
        details.getData().setNoticeOfChange(
            NoticeOfChange.builder().whichApplicant(WhichApplicant.APPLICANT_2).build()
        );
        applicant.setFirstName(TEST_FIRST_NAME);
        applicant.setLastName(TEST_LAST_NAME);

        var result = noticeOfChange.submitted(details, beforeDetails);

        verify(notificationDispatcher).sendNOC(nocSolRemovedSelfNotifications, details.getData(),
            beforeDetails.getData(), details.getId(), false, NoticeType.ORG_REMOVED);
        assertThat(result.getConfirmationHeader()).isEqualTo(REPRESENTATIVE_REMOVED_CONFIRMATION_HEADER);
        assertThat(result.getConfirmationBody()).isEqualTo(
            String.format(REPRESENTATIVE_REMOVED_CONFIRMATION_LABEL, applicant.getFullName())
        );
    }

    @Test
    void shouldResetSolicitorCaseFlags() {
        final var beforeDetails = getCaseDetails();
        final var details = getCaseDetails();
        details.setId(TEST_CASE_ID);
        details.getData().getApplicant1().getSolicitor().getOrganisationPolicy().setOrganisation(
            Organisation.builder()
                .organisationId(TEST_ORG_ID)
                .build()
        );
        details.getData().getApplicant2().getSolicitor().getOrganisationPolicy().setOrganisation(
            Organisation.builder()
                .organisationId(TEST_ORG_ID)
                .build()
        );

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(true);

        noticeOfChange.aboutToSubmit(details, beforeDetails);

        verify(caseFlagsService).resetSolicitorCaseFlags(details.getData(), true);
    }

    @Test
    void shouldInviteApplicant1ToCaseForSoleCaseIfEmailExists() {
        final var details = getCaseDetails();
        final var beforeDetails = getCaseDetails();
        Applicant applicant = details.getData().getApplicant1();
        details.getData().setNoticeOfChange(
            NoticeOfChange.builder().whichApplicant(WhichApplicant.APPLICANT_1).build()
        );
        applicant.setFirstName(TEST_FIRST_NAME);
        applicant.setLastName(TEST_LAST_NAME);
        applicant.setEmail(TEST_USER_EMAIL);

        noticeOfChange.submitted(details, beforeDetails);

        verify(notificationDispatcher).sendNOCCaseInvite(nocSolsToCitizenNotifications,
            details.getData(), details.getId(), true);
    }

    @Test
    void shouldNotInviteApplicant1ToCaseForSoleCaseIfEmailIsNull() {
        final var details = getCaseDetails();
        final var beforeDetails = getCaseDetails();
        Applicant applicant = details.getData().getApplicant1();
        details.getData().setNoticeOfChange(
            NoticeOfChange.builder().whichApplicant(WhichApplicant.APPLICANT_1).build()
        );
        applicant.setFirstName(TEST_FIRST_NAME);
        applicant.setLastName(TEST_LAST_NAME);
        applicant.setEmail(null);

        noticeOfChange.submitted(details, beforeDetails);

        verify(notificationDispatcher, never()).sendNOCCaseInvite(nocSolsToCitizenNotifications,
            details.getData(), details.getId(), true);
        verify(nocSolRemovedCitizenNotification).send(details.getData(), true, TEST_CASE_ID);
    }

    @Test
    void shouldNotInviteApplicant1ToCaseForSoleCaseIfEmailIsBlank() {
        final var details = getCaseDetails();
        final var beforeDetails = getCaseDetails();
        Applicant applicant = details.getData().getApplicant1();
        details.getData().setNoticeOfChange(
            NoticeOfChange.builder().whichApplicant(WhichApplicant.APPLICANT_1).build()
        );
        applicant.setFirstName(TEST_FIRST_NAME);
        applicant.setLastName(TEST_LAST_NAME);
        applicant.setEmail("");

        noticeOfChange.submitted(details, beforeDetails);

        verify(notificationDispatcher, never()).sendNOCCaseInvite(nocSolsToCitizenNotifications,
            details.getData(), details.getId(), true);
        verify(nocSolRemovedCitizenNotification).send(details.getData(), true, TEST_CASE_ID);
    }

    @Test
    void shouldInviteRespondentToCaseForSoleCaseIfCaseIssuedAndEmailExists() {
        final var details = getCaseDetails();
        final var beforeDetails = getCaseDetails();
        Applicant applicant = details.getData().getApplicant2();
        details.getData().setNoticeOfChange(
            NoticeOfChange.builder().whichApplicant(WhichApplicant.APPLICANT_2).build()
        );
        applicant.setFirstName(TEST_FIRST_NAME);
        applicant.setLastName(TEST_LAST_NAME);
        applicant.setEmail(TEST_USER_EMAIL);

        noticeOfChange.submitted(details, beforeDetails);

        verify(notificationDispatcher).sendNOCCaseInvite(nocSolsToCitizenNotifications,
            details.getData(), details.getId(), false);
    }

    @Test
    void shouldNotInviteRespondentToCaseForSoleCaseIfCaseNotIssuedAndEmailExists() {
        final var details = getCaseDetails();
        final var beforeDetails = getCaseDetails();
        Applicant applicant = details.getData().getApplicant2();
        details.getData().setNoticeOfChange(
            NoticeOfChange.builder().whichApplicant(WhichApplicant.APPLICANT_2).build()
        );
        applicant.setFirstName(TEST_FIRST_NAME);
        applicant.setLastName(TEST_LAST_NAME);
        applicant.setEmail(TEST_USER_EMAIL);
        details.getData().getApplication().setIssueDate(null);

        noticeOfChange.submitted(details, beforeDetails);

        verify(notificationDispatcher, never()).sendNOCCaseInvite(nocSolsToCitizenNotifications,
            details.getData(), details.getId(), false);
        verify(nocSolRemovedCitizenNotification).send(details.getData(), false, TEST_CASE_ID);
    }

    @Test
    void shouldNotInviteRespondentToCaseForSoleCaseIfCaseIssuedAndEmailNull() {
        final var details = getCaseDetails();
        final var beforeDetails = getCaseDetails();
        Applicant applicant = details.getData().getApplicant2();
        details.getData().setNoticeOfChange(
            NoticeOfChange.builder().whichApplicant(WhichApplicant.APPLICANT_2).build()
        );
        applicant.setFirstName(TEST_FIRST_NAME);
        applicant.setLastName(TEST_LAST_NAME);
        applicant.setEmail(null);

        noticeOfChange.submitted(details, beforeDetails);

        verify(notificationDispatcher, never()).sendNOCCaseInvite(nocSolsToCitizenNotifications,
            details.getData(), details.getId(), false);
        verify(nocSolRemovedCitizenNotification).send(details.getData(), false, TEST_CASE_ID);
    }

    @Test
    void shouldNotInviteRespondentToCaseForSoleCaseIfCaseIssuedAndEmailBlank() {
        final var details = getCaseDetails();
        final var beforeDetails = getCaseDetails();
        Applicant applicant = details.getData().getApplicant2();
        details.getData().setNoticeOfChange(
            NoticeOfChange.builder().whichApplicant(WhichApplicant.APPLICANT_2).build()
        );
        applicant.setFirstName(TEST_FIRST_NAME);
        applicant.setLastName(TEST_LAST_NAME);
        applicant.setEmail("");

        noticeOfChange.submitted(details, beforeDetails);

        verify(notificationDispatcher, never()).sendNOCCaseInvite(nocSolsToCitizenNotifications,
            details.getData(), details.getId(), false);
        verify(nocSolRemovedCitizenNotification).send(details.getData(), false, TEST_CASE_ID);
    }

    @Test
    void shouldNotInviteApplicant1ToCaseForJointCase() {
        final var details = getCaseDetails();
        details.getData().setApplicationType(JOINT_APPLICATION);
        final var beforeDetails = getCaseDetails();
        Applicant applicant = details.getData().getApplicant1();
        details.getData().setNoticeOfChange(
            NoticeOfChange.builder().whichApplicant(WhichApplicant.APPLICANT_1).build()
        );
        applicant.setFirstName(TEST_FIRST_NAME);
        applicant.setLastName(TEST_LAST_NAME);

        noticeOfChange.submitted(details, beforeDetails);

        verify(notificationDispatcher).sendNOC(nocSolRemovedSelfNotifications, details.getData(),
            beforeDetails.getData(), details.getId(), true, NoticeType.ORG_REMOVED);
        verifyNoMoreInteractions(notificationDispatcher);
        verify(nocSolRemovedCitizenNotification).send(details.getData(), true, TEST_CASE_ID);
    }

    @Test
    void shouldSetApplicant2ToOfflineInJointCaseWhenApp1SolicitorStopsRepresentation() {
        final var beforeDetails = getCaseDetails();
        final var details = getCaseDetails();
        details.setId(TEST_CASE_ID);
        details.getData().getApplicant1().getSolicitor().getOrganisationPolicy().setOrganisation(
            Organisation.builder()
                .organisationId(TEST_ORG_ID)
                .build()
        );
        details.getData().getApplicant2().getSolicitor().getOrganisationPolicy().setOrganisation(
            Organisation.builder()
                .organisationId(TEST_ORG_ID)
                .build()
        );
        details.getData().setApplicationType(JOINT_APPLICATION);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(true);

        var result = noticeOfChange.aboutToSubmit(details, beforeDetails);

        assertThat(result.getData().getApplicant2().getOffline()).isEqualTo(YES);
    }

    @Test
    void shouldSetApplicant1ToOfflineInJointCaseWhenApp2SolicitorStopsRepresentation() {
        final var beforeDetails = getCaseDetails();
        final var details = getCaseDetails();
        details.setId(TEST_CASE_ID);
        details.getData().getApplicant1().getSolicitor().getOrganisationPolicy().setOrganisation(
            Organisation.builder()
                .organisationId(TEST_ORG_ID)
                .build()
        );
        details.getData().getApplicant2().getSolicitor().getOrganisationPolicy().setOrganisation(
            Organisation.builder()
                .organisationId(TEST_ORG_ID)
                .build()
        );
        details.getData().setApplicationType(JOINT_APPLICATION);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(TEST_AUTHORIZATION_TOKEN);
        when(ccdAccessService.isApplicant1(TEST_AUTHORIZATION_TOKEN, TEST_CASE_ID)).thenReturn(false);

        var result = noticeOfChange.aboutToSubmit(details, beforeDetails);

        assertThat(result.getData().getApplicant1().getOffline()).isEqualTo(YES);
    }

    private void assertSolicitorRemoved(Applicant applicant, UserRole solicitorRole) {
        final OrganisationPolicy organisationPolicy = applicant.getSolicitor().getOrganisationPolicy();

        assertThat(applicant.isApplicantOffline()).isTrue();
        assertThat(applicant.getSolicitorRepresented()).isEqualTo(NO);
        assertThat(applicant.getSolicitorRepresented());
        assertThat(organisationPolicy.getOrganisation().getOrganisationId()).isNull();
        assertThat(organisationPolicy.getOrgPolicyCaseAssignedRole()).isEqualTo(solicitorRole);
    }

    private void assertSolicitorNotRemoved(Applicant applicant) {
        final OrganisationPolicy organisationPolicy = applicant.getSolicitor().getOrganisationPolicy();

        assertThat(applicant.getSolicitorRepresented()).isEqualTo(YES);
        assertThat(organisationPolicy.getOrganisation().getOrganisationId()).isNotNull();
    }

    private CaseDetails<CaseData, State> getCaseDetails() {
        final var details = new CaseDetails<CaseData, State>();
        final var data = caseData();
        data.setApplicationType(SOLE_APPLICATION);
        data.setApplicant1(applicantRepresentedBySolicitor());
        data.setApplicant2(applicantRepresentedBySolicitor());
        data.getApplicant1().setOffline(NO);
        data.getApplicant2().setOffline(NO);
        data.getApplicant1().getSolicitor().setOrganisationPolicy(OrganisationPolicy.<UserRole>builder()
                .organisation(new Organisation(null, null))
            .orgPolicyCaseAssignedRole(APPLICANT_1_SOLICITOR)
            .build());
        data.getApplicant2().getSolicitor().setOrganisationPolicy(OrganisationPolicy.<UserRole>builder()
                .organisation(new Organisation(null, null))
            .orgPolicyCaseAssignedRole(APPLICANT_2_SOLICITOR)
            .build());
        data.getApplication().setIssueDate(LocalDate.of(2021, 6, 18));
        details.setData(data);
        details.setId(TEST_CASE_ID);

        return details;
    }
}
