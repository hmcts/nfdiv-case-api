package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.DynamicList;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralApplicationD11JourneyOptions;
import uk.gov.hmcts.divorce.divorcecase.model.InterimApplicationOptions;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class GeneralApplicationDraftJourneyServiceTest {

    @InjectMocks
    private GeneralApplicationDraftJourneyService generalApplicationDraftJourneyService;

    @Mock
    private GeneralApplicationTypeOptionsService generalApplicationTypeOptionsService;

    @Mock
    private GeneralApplicationDraftSubmissionService generalApplicationDraftSubmissionService;

    @Test
    void shouldSetEmptyGeneralApplicationAndBuildSolTypeOptionsForApplicantOnAboutToStart() {
        GeneralApplicationD11JourneyOptions journeyOptions = GeneralApplicationD11JourneyOptions.builder()
            .solType(DynamicList.builder().build())
            .build();

        InterimApplicationOptions interimOptions = InterimApplicationOptions.builder()
            .generalApplicationD11JourneyOptions(journeyOptions)
            .build();

        Applicant applicant = Applicant.builder()
            .interimApplicationOptions(interimOptions)
            .build();

        CaseData caseData = CaseData.builder().build();
        CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(State.AwaitingService)
            .data(caseData)
            .build();

        DynamicList builtOptions = DynamicList.builder().build();
        when(generalApplicationTypeOptionsService.buildOptions(State.AwaitingService, caseData, journeyOptions.getSolType()))
            .thenReturn(builtOptions);

        generalApplicationDraftJourneyService.prepareAboutToStart(details, applicant);

        assertThat(caseData.getGeneralApplication()).isNotNull();
        assertThat(journeyOptions.getSolType()).isEqualTo(builtOptions);
        verify(generalApplicationTypeOptionsService).buildOptions(State.AwaitingService, caseData, journeyOptions.getSolType());
    }

    @Test
    void shouldPassDetailsToSubmissionServiceOnAboutToSubmit() {
        Applicant applicant = Applicant.builder().build();
        CaseData caseData = CaseData.builder().build();
        CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .id(TEST_CASE_ID)
            .data(caseData)
            .build();

        generalApplicationDraftJourneyService.prepareAboutToSubmit(details, applicant);

        verify(generalApplicationDraftSubmissionService).buildGeneralApplication(details, caseData, applicant);
    }
}
