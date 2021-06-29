package uk.gov.hmcts.divorce.caseworker.event;

import org.assertj.core.api.Assertions;
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
import uk.gov.hmcts.divorce.common.model.Applicant;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.ConfidentialAddress;
import uk.gov.hmcts.divorce.common.model.Gender;
import uk.gov.hmcts.divorce.common.model.LegalConnections;
import uk.gov.hmcts.divorce.common.model.State;
import uk.gov.hmcts.divorce.common.model.UserRole;

import java.time.LocalDate;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueApplication.CASEWORKER_ISSUE_APPLICATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.invalidCaseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueApplicationTest {

    @Mock
    private IssueApplicationService issueApplicationService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private CaseworkerIssueApplication caseworkerIssueApplication;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final Set<State> stateSet = Set.of(State.class.getEnumConstants());
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = new ConfigBuilderImpl<>(CaseData.class, stateSet);

        caseworkerIssueApplication.configure(configBuilder);

        assertThat(configBuilder.getEvents())
            .extracting(Event::getId)
            .contains(CASEWORKER_ISSUE_APPLICATION);
    }

    @Test
    void shouldCallIssueApplicationServiceAndReturnCaseData() {

        final var auth = "authorization";
        final var caseData = caseDataWithAllMandatoryFields();
        final var expectedCaseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);
        details.setCreatedDate(LOCAL_DATE_TIME);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(auth);
        when(issueApplicationService
            .aboutToSubmit(
                caseData,
                details.getId(),
                details.getCreatedDate().toLocalDate(),
                auth))
            .thenReturn(expectedCaseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerIssueApplication.aboutToSubmit(details, null);

        assertThat(response.getData()).isEqualTo(expectedCaseData);

        verify(issueApplicationService).aboutToSubmit(
            caseData,
            details.getId(),
            details.getCreatedDate().toLocalDate(),
            auth);

        verifyNoMoreInteractions(httpServletRequest, issueApplicationService);
    }

    @Test
    void shouldFailCaseDataValidationWhenMandatoryFieldsAreNotPopulatedForIssueApplication() {
        final var caseData = invalidCaseData();

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);
        details.setId(1L);
        details.setCreatedDate(LOCAL_DATE_TIME);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerIssueApplication.aboutToSubmit(details, null);

        Assertions.assertThat(response.getErrors())
            .containsExactlyInAnyOrder(
                "Applicant2FirstName cannot be empty or null",
                "Applicant2LastName cannot be empty or null",
                "Applicant1FinancialOrder cannot be empty or null",
                "Applicant2Gender cannot be empty or null",
                "MarriageApplicant1Name cannot be empty or null",
                "Applicant1ContactDetailsConfidential cannot be empty or null",
                "PrayerHasBeenGiven cannot be empty or null",
                "StatementOfTruth cannot be empty or null",
                "MarriageDate cannot be empty or null",
                "JurisdictionConnections cannot be empty or null",
                "MarriageApplicant2Name cannot be empty or null",
                "PlaceOfMarriage cannot be empty or null",
                "Applicant1Gender cannot be empty or null"
            );

        verifyNoMoreInteractions(httpServletRequest, issueApplicationService);
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
            .contactDetailsConfidential(ConfidentialAddress.KEEP)
            .build()
        );

        caseData.getApplicant1().setFinancialOrder(NO);
        caseData.getApplication().setPrayerHasBeenGiven(YES);
        caseData.getApplication().setStatementOfTruth(YES);
        caseData.getApplication().getJurisdiction().setLegalConnections(Set.of(LegalConnections.APPLICANT_RESPONDENT_RESIDENT));
        caseData.getApplication().getMarriageDetails().setApplicant1Name("app1Name");
        caseData.getApplication().getMarriageDetails().setDate(LocalDate.of(2009, 1, 1));
        caseData.getApplication().getMarriageDetails().setApplicant2Name("app2Name");
        caseData.getApplication().getMarriageDetails().setPlaceOfMarriage("London");
        return caseData;
    }
}
