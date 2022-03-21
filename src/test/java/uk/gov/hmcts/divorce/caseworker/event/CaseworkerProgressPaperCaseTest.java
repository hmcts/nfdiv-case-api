package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event. CaseworkerProgressPaperCase.CASEWORKER_PROGRESS_PAPER_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.ProgressPaperCase.AWAITING_DOCUMENTS;
import static uk.gov.hmcts.divorce.divorcecase.model.ProgressPaperCase.AWAITING_PAYMENT;
import static uk.gov.hmcts.divorce.divorcecase.model.ProgressPaperCase.SUBMITTED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
public class CaseworkerProgressPaperCaseTest {

    @InjectMocks
    private CaseworkerProgressPaperCase caseworkerProgressPaperCase;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerProgressPaperCase.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_PROGRESS_PAPER_CASE);
    }

    @Test
    void shouldUpdateCaseStateWhenCaseworkerSelectsAwaitingPayment() {

        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplication().setProgressPaperCase(AWAITING_PAYMENT);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerProgressPaperCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(AwaitingPayment);
    }

    @Test
    void shouldUpdateCaseStateWhenCaseworkerSelectsAwaitingDocuments() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplication().setProgressPaperCase(AWAITING_DOCUMENTS);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerProgressPaperCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(AwaitingDocuments);
    }

    @Test
    void shouldUpdateCaseStateWhenCaseworkerSelectsSubmitted() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = validApplicant1CaseData();
        caseData.getApplication().setProgressPaperCase(SUBMITTED);

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerProgressPaperCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(Submitted);
    }

    @Test
    void shouldReturnErrorsIfCaseDataIsNotReadyForProgression() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();
        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerProgressPaperCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getErrors()).isNotNull();
        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).hasSize(12);
        assertThat(response.getErrors()).containsExactly(
            "Applicant1FirstName cannot be empty or null",
            "Applicant1LastName cannot be empty or null",
            "Applicant2FirstName cannot be empty or null",
            "Applicant2LastName cannot be empty or null",
            "Applicant1FinancialOrder cannot be empty or null",
            "Applicant1Gender cannot be empty or null",
            "MarriageApplicant1Name cannot be empty or null",
            "Applicant1ContactDetailsType cannot be empty or null",
            "Statement of truth must be accepted by the person making the application",
            "applicant1PrayerHasBeenGivenCheckbox cannot be empty or null",
            "MarriageDate cannot be empty or null",
            "JurisdictionConnections cannot be empty or null"
        );
    }
}
