package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.respondentWithDigitalSolicitor;

@ExtendWith(MockitoExtension.class)
class SetFinalOrderFieldsAsApplicant2SolTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private SetFinalOrderFieldsAsApplicant2Sol setFinalOrderFieldsAsApplicant2Sol;

    @Test
    void shouldUpdateFinalOrderFieldsIfNotSetAndAwaitingFinalOrder() {
        setMockClock(clock);

        final CaseData caseData = CaseData.builder().build();
        caseData.setApplicant2(respondentWithDigitalSolicitor());
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(AwaitingFinalOrder).id(TEST_CASE_ID).data(caseData)
            .build();

        CaseData result = setFinalOrderFieldsAsApplicant2Sol.apply(details).getData();
        FinalOrder resultFo = result.getFinalOrder();
        assertThat(resultFo.getApplicant2SolAppliedForFinalOrder()).isEqualTo(YES);
        assertThat(resultFo.getDateApplicant2SolAppliedForFinalOrder()).isEqualTo(LocalDateTime.now(clock));
        assertThat(resultFo.getApplicant2SolResponsibleForFinalOrder()).isEqualTo(result.getApplicant2().getSolicitor().getName());
        assertThat(resultFo.getApplicant2FinalOrderStatementOfTruth()).isEqualTo(YES);
        assertThat(resultFo.getDateFinalOrderSubmitted()).isEqualTo(LocalDateTime.now(clock));
    }
}
