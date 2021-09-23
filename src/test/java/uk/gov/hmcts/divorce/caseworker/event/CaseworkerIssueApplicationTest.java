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
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.Gender;
import uk.gov.hmcts.divorce.divorcecase.model.JurisdictionConnections;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueApplication.CASEWORKER_ISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemIssueSolicitorServicePack.SYSTEM_ISSUE_SOLICITOR_SERVICE_PACK;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.invalidCaseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueApplicationTest {

    @Mock
    private IssueApplicationService issueApplicationService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @InjectMocks
    private CaseworkerIssueApplication caseworkerIssueApplication;

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
        details.setId(1L);
        details.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> expectedDetails = new CaseDetails<>();
        expectedDetails.setData(expectedCaseData);
        expectedDetails.setId(1L);
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
        caseData.getApplication().setSolSignStatementOfTruth(YES);

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);
        details.setCreatedDate(LOCAL_DATE_TIME);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerIssueApplication.aboutToSubmit(details, null);

        assertThat(response.getErrors())
            .containsExactlyInAnyOrder(
                "Applicant2FirstName cannot be empty or null",
                "Applicant2LastName cannot be empty or null",
                "Applicant1FinancialOrder cannot be empty or null",
                "Applicant2Gender cannot be empty or null",
                "MarriageApplicant1Name cannot be empty or null",
                "Applicant1KeepContactDetailsConfidential cannot be empty or null",
                "Applicant1PrayerHasBeenGiven cannot be empty or null",
                "MarriageDate cannot be empty or null",
                "JurisdictionConnections cannot be empty or null",
                "MarriageApplicant2Name cannot be empty or null",
                "PlaceOfMarriage cannot be empty or null",
                "Applicant1Gender cannot be empty or null"
            );
    }

    @Test
    void shouldSubmitCcdSystemIssueSolicitorServicePackEventOnSubmittedCallbackIfSolicitorService() {

        final var caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setSolServiceMethod(SOLICITOR_SERVICE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        caseworkerIssueApplication.submitted(caseDetails, null);

        verify(ccdUpdateService).submitEvent(caseDetails, SYSTEM_ISSUE_SOLICITOR_SERVICE_PACK);
    }

    @Test
    void shouldNotSubmitCcdSystemIssueSolicitorServicePackEventOnSubmittedCallbackIfCourtService() {

        final var caseData = caseData();
        caseData.getApplication().setSolSignStatementOfTruth(YES);
        caseData.getApplication().setSolServiceMethod(COURT_SERVICE);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        caseworkerIssueApplication.submitted(caseDetails, null);

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitCcdSystemIssueSolicitorServicePackEventOnSubmittedCallbackIfNotSolicitorApplication() {

        final var caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        caseworkerIssueApplication.submitted(caseDetails, null);

        verifyNoInteractions(ccdUpdateService);
    }

    private CaseData caseDataWithAllMandatoryFields() {
        var caseData = caseData();
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
            .keepContactDetailsConfidential(YES)
            .build()
        );

        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplication().setApplicant1PrayerHasBeenGiven(YES);
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
