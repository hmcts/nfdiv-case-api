package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.SearchGovRecordsJourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class GeneralApplicationDraftJourneyServiceTest {

    @InjectMocks
    private GeneralApplicationDraftJourneyService generalApplicationDraftJourneyService;

    @Mock
    private GeneralApplicationDraftSubmissionService generalApplicationDraftSubmissionService;

    @Test
    void shouldSetEmptyGeneralApplicationAndBuildSolTypeOptionsForApplicantOnAboutToStart() {
        CaseData caseData = CaseData.builder().build();
        CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(State.AwaitingService)
            .data(caseData)
            .build();

        generalApplicationDraftJourneyService.prepareAboutToStart(details);
    }

    @Test
    void shouldPassDetailsToSubmissionServiceOnAboutToSubmit() {
        InterimApplicationOptions interimOptions = InterimApplicationOptions.builder()
            .generalApplicationD11JourneyOptions(GeneralApplicationD11JourneyOptions.builder().build())
            .build();
        Applicant applicant = Applicant.builder().interimApplicationOptions(interimOptions).build();
        CaseData caseData = CaseData.builder().build();
        CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        generalApplicationDraftJourneyService.prepareAboutToSubmit(details, applicant);

        verify(generalApplicationDraftSubmissionService).buildGeneralApplication(details, caseData, applicant);
    }

    @Test
    void shouldPassDetailsToSubmissionServiceForSearchGovRecordsOnAboutToSubmit() {
        InterimApplicationOptions interimOptions = InterimApplicationOptions.builder()
            .searchGovRecordsJourneyOptions(SearchGovRecordsJourneyOptions.builder().build())
            .build();
        Applicant applicant = Applicant.builder().interimApplicationOptions(interimOptions).build();
        CaseData caseData = CaseData.builder().applicant1(applicant).build();
        CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        generalApplicationDraftJourneyService.prepareAboutToSubmitForSearchGovRecords(details);

        verify(generalApplicationDraftSubmissionService).buildGeneralApplication(details, caseData, applicant);
    }
}
