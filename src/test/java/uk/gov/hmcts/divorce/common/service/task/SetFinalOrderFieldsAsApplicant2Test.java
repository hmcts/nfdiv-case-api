package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.FinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrder;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingFinalOrderPayment;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJointFinalOrder;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SetFinalOrderFieldsAsApplicant2Test {

    @Mock
    private Clock clock;

    @InjectMocks
    private SetFinalOrderFieldsAsApplicant2 setFinalOrderFieldsAsApplicant2;

    @Test
    void shouldUpdateFinalOrderFieldsIfNotSetAndAwaitingFinalOrder() {
        setMockClock(clock);

        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(AwaitingFinalOrder).id(TEST_CASE_ID).data(caseData)
            .build();

        CaseDetails<CaseData, State> result = setFinalOrderFieldsAsApplicant2.apply(details);
        assertThat(result.getData().getFinalOrder().getApplicant2AppliedForFinalOrderFirst()).isEqualTo(YesOrNo.YES);
        assertThat(result.getData().getFinalOrder().getApplicant1AppliedForFinalOrderFirst()).isEqualTo(YesOrNo.NO);
        assertThat(result.getData().getFinalOrder().getDateFinalOrderSubmitted()).isEqualTo(LocalDateTime.now(clock));
    }

    @Test
    void shouldUpdateFinalOrderFieldsIfNotSetAndAwaitingFinalOrderPayment() {
        setMockClock(clock);

        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(AwaitingFinalOrderPayment).id(TEST_CASE_ID).data(caseData)
            .build();

        CaseDetails<CaseData, State> result = setFinalOrderFieldsAsApplicant2.apply(details);
        assertThat(result.getData().getFinalOrder().getApplicant2AppliedForFinalOrderFirst()).isEqualTo(YesOrNo.YES);
        assertThat(result.getData().getFinalOrder().getApplicant1AppliedForFinalOrderFirst()).isEqualTo(YesOrNo.NO);
        assertThat(result.getData().getFinalOrder().getDateFinalOrderSubmitted()).isEqualTo(LocalDateTime.now(clock));
    }

    @Test
    void shouldNotUpdateFinalOrderFieldsIfAlreadySetAndAwaitingFinalOrder() {
        setMockClock(clock);

        final FinalOrder finalOrder = FinalOrder.builder()
            .applicant2AppliedForFinalOrderFirst(YesOrNo.NO)
            .applicant1AppliedForFinalOrderFirst(YesOrNo.YES)
            .dateFinalOrderSubmitted(LocalDateTime.now(clock).minusDays(2))
            .build();

        final CaseData caseData = CaseData.builder().finalOrder(finalOrder).build();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(AwaitingFinalOrder).id(TEST_CASE_ID).data(caseData)
            .build();

        CaseDetails<CaseData, State> result = setFinalOrderFieldsAsApplicant2.apply(details);
        assertThat(result.getData().getFinalOrder().getApplicant2AppliedForFinalOrderFirst()).isEqualTo(YesOrNo.NO);
        assertThat(result.getData().getFinalOrder().getApplicant1AppliedForFinalOrderFirst()).isEqualTo(YesOrNo.YES);
        assertThat(result.getData().getFinalOrder().getDateFinalOrderSubmitted()).isEqualTo(LocalDateTime.now(clock).minusDays(2));
    }

    @Test
    void shouldNotUpdateFinalOrderFieldsIfNotAwaitingFinalOrder() {
        final CaseData caseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(AwaitingJointFinalOrder).id(TEST_CASE_ID).data(caseData)
            .build();

        CaseDetails<CaseData, State> result = setFinalOrderFieldsAsApplicant2.apply(details);
        assertThat(result.getData().getFinalOrder().getApplicant2AppliedForFinalOrderFirst()).isNull();
        assertThat(result.getData().getFinalOrder().getApplicant1AppliedForFinalOrderFirst()).isNull();
        assertThat(result.getData().getFinalOrder().getDateFinalOrderSubmitted()).isNull();
    }



    @Test
    void shouldUpdateFinalOrderStatementOfTruthIfCaseIsNotOverdue() {
        setMockClock(clock);

        final CaseData caseData = CaseData.builder().build();
        caseData.getFinalOrder().setIsFinalOrderOverdue(YesOrNo.NO);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(AwaitingFinalOrder).id(TEST_CASE_ID).data(caseData)
            .build();

        CaseDetails<CaseData, State> result = setFinalOrderFieldsAsApplicant2.apply(details);
        assertThat(result.getData().getFinalOrder().getApplicant2FinalOrderStatementOfTruth()).isEqualTo(YesOrNo.YES);
        assertThat(result.getData().getFinalOrder().getApplicant1FinalOrderStatementOfTruth()).isEqualTo(null);
    }

    @Test
    void shouldNotUpdateFinalOrderStatementOfTruthIfCaseIsOverdue() {
        setMockClock(clock);

        final CaseData caseData = CaseData.builder().build();
        caseData.getFinalOrder().setIsFinalOrderOverdue(YesOrNo.YES);
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .state(AwaitingFinalOrder).id(TEST_CASE_ID).data(caseData)
            .build();

        CaseDetails<CaseData, State> result = setFinalOrderFieldsAsApplicant2.apply(details);
        assertThat(result.getData().getFinalOrder().getApplicant2FinalOrderStatementOfTruth()).isEqualTo(null);
        assertThat(result.getData().getFinalOrder().getApplicant1FinalOrderStatementOfTruth()).isEqualTo(null);
    }
}
