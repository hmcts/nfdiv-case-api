package uk.gov.hmcts.divorce.caseworker.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.COURT_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceMethod.SOLICITOR_SERVICE;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingService;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;

@ExtendWith(MockitoExtension.class)
class SetPostIssueStateTest {

    @InjectMocks
    private SetPostIssueState setPostIssueState;

    @Test
    void shouldSetStateToAwaitingService() {

        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .solSignStatementOfTruth(YES)
                .solServiceMethod(SOLICITOR_SERVICE)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setPostIssueState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingService);
    }

    @Test
    void shouldSetStateToAwaitingAos() {

        final CaseData caseData = CaseData.builder()
            .application(Application.builder()
                .solSignStatementOfTruth(YES)
                .solServiceMethod(COURT_SERVICE)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setPostIssueState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(AwaitingAos);
    }

    @Test
    void shouldSetStateToHolding() {

        final CaseData caseData = CaseData.builder()
            .application(Application.builder().build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setPostIssueState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(Holding);
    }
}