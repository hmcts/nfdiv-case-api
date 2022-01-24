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
import uk.gov.hmcts.divorce.divorcecase.model.ProgressPaperCase;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerProgressPaperCase.CASEWORKER_PROGRESS_PAPER_CASE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

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
        final CaseData caseData = CaseData.builder().build();

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        caseData.setProgressPaperCase(ProgressPaperCase.AWAITING_PAYMENT);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerProgressPaperCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.AwaitingPayment);
    }

    @Test
    void shouldUpdateCaseStateWhenCaseworkerSelectsAwaitingDocuments() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        caseData.setProgressPaperCase(ProgressPaperCase.AWAITING_DOCUMENT);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerProgressPaperCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.AwaitingDocuments);
    }

    @Test
    void shouldUpdateCaseStateWhenCaseworkerSelectsSubmitted() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseData caseData = CaseData.builder().build();

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);
        caseData.setProgressPaperCase(ProgressPaperCase.SUBMITTED);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerProgressPaperCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(State.Submitted);
    }
}
