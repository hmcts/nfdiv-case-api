package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerProgressPaperCase.CASEWORKER_PROGRESS_PAPER_CASE;
import static uk.gov.hmcts.divorce.divorcecase.model.ProgressPaperCase.AWAITING_DOCUMENTS;
import static uk.gov.hmcts.divorce.divorcecase.model.ProgressPaperCase.AWAITING_PAYMENT;
import static uk.gov.hmcts.divorce.divorcecase.model.ProgressPaperCase.SUBMITTED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingDocuments;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
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
        CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .progressPaperCase(AWAITING_PAYMENT)
                .build())
            .build();

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerProgressPaperCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(AwaitingPayment);
    }

    @Test
    void shouldUpdateCaseStateWhenCaseworkerSelectsAwaitingDocuments() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .progressPaperCase(AWAITING_DOCUMENTS)
                .build())
            .build();

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerProgressPaperCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(AwaitingDocuments);
    }

    @Test
    void shouldUpdateCaseStateWhenCaseworkerSelectsSubmitted() {
        final long caseId = 1L;
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .progressPaperCase(SUBMITTED)
                .build())
            .build();

        caseDetails.setData(caseData);
        caseDetails.setId(caseId);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerProgressPaperCase.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(Submitted);
    }
}
