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
import uk.gov.hmcts.divorce.caseworker.service.IssueApplicationService;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer;
import uk.gov.hmcts.divorce.divorcecase.model.ApplicationType;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueApplication.CASEWORKER_ISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicantPrayer.DissolveDivorce.DISSOLVE_DIVORCE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.PERSONAL_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemIssueSolicitorServicePack.SYSTEM_ISSUE_SOLICITOR_SERVICE_PACK;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseDataWithStatementOfTruth;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.invalidCaseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueApplicationTest {

    @Mock
    private IssueApplicationService issueApplicationService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @InjectMocks
    private CaseworkerIssueApplication caseworkerIssueApplication;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerIssueApplication.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_ISSUE_APPLICATION);
    }

    @Test
    void shouldCallIssueApplicationServiceAndReturnCaseData() {

        final var caseData = caseDataWithAllMandatoryFields();
        final var expectedCaseData = CaseData.builder().build();
        caseData.getApplication().setSolSignStatementOfTruth(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> expectedDetails = new CaseDetails<>();
        expectedDetails.setData(expectedCaseData);
        expectedDetails.setId(TEST_CASE_ID);
        expectedDetails.setCreatedDate(LOCAL_DATE_TIME);
        expectedDetails.setState(AwaitingService);

        when(issueApplicationService.issueApplication(details)).thenReturn(expectedDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerIssueApplication.aboutToSubmit(details, null);

        assertThat(response.getData()).isEqualTo(expectedCaseData);
        assertThat(response.getState()).isEqualTo(AwaitingService);
        verify(issueApplicationService).issueApplication(details);
    }

    @Test
    void shouldFailCaseDataValidationWhenMandatoryFieldsAreNotPopulatedForIssueApplication() {
        final var caseData = invalidCaseData();
        caseData.getApplicant2().setEmail("onlineApplicant2@email.com");
        caseData.getApplication().setSolSignStatementOfTruth(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(TEST_CASE_ID);
        details.setCreatedDate(LOCAL_DATE_TIME);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerIssueApplication.aboutToSubmit(details, null);

        assertThat(response.getErrors())
            .containsExactlyInAnyOrder(
                "ApplicationType cannot be empty or null",
                "Applicant2FirstName cannot be empty or null",
                "Applicant2LastName cannot be empty or null",
                "Applicant1FinancialOrder cannot be empty or null",
                "Applicant2Gender cannot be empty or null",
                "MarriageApplicant1Name cannot be empty or null",
                "Applicant1ContactDetailsType cannot be empty or null",
                "Applicant 1 must confirm prayer to dissolve their marriage (get a divorce)",
                "MarriageDate cannot be empty or null",
                "JurisdictionConnections cannot be empty or null",
                "MarriageApplicant2Name cannot be empty or null",
                "PlaceOfMarriage cannot be empty or null",
                "Applicant1Gender cannot be empty or null"
            );
    }

    @Test
    void shouldSubmitCcdSystemIssueSolicitorServicePackEventOnSubmittedCallbackIfSolicitorService() {
        final User user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final var caseData = caseData();
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);

        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        caseworkerIssueApplication.submitted(caseDetails, null);

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_ISSUE_SOLICITOR_SERVICE_PACK, user, SERVICE_AUTHORIZATION);

        verify(issueApplicationService).sendNotifications(caseDetails);
    }

    @Test
    void shouldNotSubmitCcdSystemIssueSolicitorServicePackEventOnSubmittedCallbackIfStateIsCourtService() {

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final var caseData = caseData();
        caseData.getApplication().setServiceMethod(COURT_SERVICE);

        caseDetails.setData(caseData);

        caseworkerIssueApplication.submitted(caseDetails, null);

        verifyNoInteractions(ccdUpdateService);

        verify(issueApplicationService).sendNotifications(caseDetails);
    }

    @Test
    void shouldNotSubmitCcdSystemIssueSolicitorServicePackEventOnSubmittedCallbackIfPersonalService() {

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final var caseData = caseData();
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);

        caseDetails.setData(caseData);

        caseworkerIssueApplication.submitted(caseDetails, null);

        verifyNoInteractions(ccdUpdateService);

        verify(issueApplicationService).sendNotifications(caseDetails);
    }

    @Test
    void shouldThrowErrorIfPersonalServiceConfidential() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("Some place");
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PRIVATE);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        updatedCaseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerIssueApplication.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).contains("You may not select Solicitor Service "
            + "or Personal Service if the respondent is confidential.");
    }

    @Test
    void shouldThrowErrorIfSolicitorServiceConfidential() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PRIVATE);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        updatedCaseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerIssueApplication.aboutToSubmit(
            updatedCaseDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).contains("You may not select Solicitor Service "
            + "or Personal Service if the respondent is confidential.");
    }

    @Test
    void shouldIssueApplicationPersonalServiceNonConfidential() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.getApplication().setDateSubmitted(LocalDateTime.now());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("Some place");
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseData.getApplication().setServiceMethod(PERSONAL_SERVICE);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PUBLIC);

        final CaseDetails<CaseData, State> expectedDetails = new CaseDetails<>();
        expectedDetails.setData(caseData);
        expectedDetails.setState(Submitted);

        when(issueApplicationService.issueApplication(caseDetails)).thenReturn(expectedDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerIssueApplication.aboutToSubmit(
            expectedDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
    }

    @Test
    void shouldIssueApplicationSolicitorServiceNonConfidential() {
        final CaseData caseData = caseDataWithStatementOfTruth();
        caseData.getApplication().setDateSubmitted(LocalDateTime.now());
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("Some place");
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);
        caseData.getApplication().setServiceMethod(SOLICITOR_SERVICE);
        final Applicant applicant2 = caseData.getApplicant2();
        applicant2.setContactDetailsType(ContactDetailsType.PUBLIC);

        final CaseDetails<CaseData, State> expectedDetails = new CaseDetails<>();
        expectedDetails.setData(caseData);
        expectedDetails.setState(Submitted);

        when(issueApplicationService.issueApplication(caseDetails)).thenReturn(expectedDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerIssueApplication.aboutToSubmit(
            expectedDetails, caseDetails);

        assertThat(response.getWarnings()).isNull();
        assertThat(response.getErrors()).isNull();
    }

    private CaseData caseDataWithAllMandatoryFields() {
        var caseData = caseData();
        caseData.setApplicationType(ApplicationType.JOINT_APPLICATION);
        caseData.getApplication().setDateSubmitted(LocalDateTime.now());
        caseData.setApplicant2(Applicant
            .builder()
            .firstName("app2FirstName")
            .lastName("app2LastName")
            .gender(Gender.FEMALE)
            .build()
        );

        caseData.setApplicant1(Applicant
            .builder()
            .firstName("app1FirstName")
            .lastName("app1LastName")
            .gender(Gender.MALE)
            .applicantPrayer(ApplicantPrayer.builder().prayerDissolveDivorce(Set.of(DISSOLVE_DIVORCE)).build())
            .contactDetailsType(PRIVATE)
            .build()
        );

        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplication().setApplicant1StatementOfTruth(YES);
        caseData.getApplication().getJurisdiction().setConnections(Set.of(JurisdictionConnections.APP_1_APP_2_RESIDENT));
        caseData.getApplication().getJurisdiction().setApplicant1Residence(YES);
        caseData.getApplication().getJurisdiction().setApplicant2Residence(YES);
        caseData.getApplication().getMarriageDetails().setApplicant1Name("app1Name");
        caseData.getApplication().getMarriageDetails().setDate(LocalDate.of(2009, 1, 1));
        caseData.getApplication().getMarriageDetails().setApplicant2Name("app2Name");
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("London");
        return caseData;
    }
}
