package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceOutcome;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;

@ExtendWith(MockitoExtension.class)

public class SetLatestBailiffApplicationStatusTest {

    @InjectMocks
    private SetLatestBailiffApplicationStatus setLatestBailiffApplicationStatus;

    @Test
    void shouldSetBailiffApplicationStatusIfMostRecentOutcomeIsBailiffApplication() {
        AlternativeServiceOutcome outcome = AlternativeServiceOutcome.builder()
            .certificateOfServiceDate(getExpectedLocalDate())
            .successfulServedByBailiff(YES)
            .build();

        final CaseData caseData = CaseData.builder()
            .alternativeServiceOutcomes(singletonList(new ListValue<>("1", outcome)))
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setLatestBailiffApplicationStatus.apply(caseDetails);

        assertThat(result.getData().getConditionalOrder().getLastApprovedServiceApplicationIsBailiffApplication()).isEqualTo(YES);
        assertThat(result.getData().getConditionalOrder().getCertificateOfServiceDate()).isEqualTo(getExpectedLocalDate());
        assertThat(result.getData().getConditionalOrder().getSuccessfulServedByBailiff()).isEqualTo(YES);
    }

    @Test
    void shouldSetLastApprovedServiceApplicationIsBailiffApplicationToNoIfMostRecentOutcomeIsNotBailiffApplication() {
        AlternativeServiceOutcome outcome = AlternativeServiceOutcome.builder().build();

        final CaseData caseData = CaseData.builder()
            .alternativeServiceOutcomes(singletonList(new ListValue<>("1", outcome)))
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setLatestBailiffApplicationStatus.apply(caseDetails);

        assertThat(result.getData().getConditionalOrder().getLastApprovedServiceApplicationIsBailiffApplication()).isEqualTo(NO);
    }

    @Test
    void shouldNotSetAnyDataIfAlternativeServiceOutcomesIsEmpty() {
        final CaseData caseData = CaseData.builder()
            .alternativeServiceOutcomes(new ArrayList<>())
            .build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setLatestBailiffApplicationStatus.apply(caseDetails);

        assertThat(result.getData().getConditionalOrder().getLastApprovedServiceApplicationIsBailiffApplication()).isNull();
    }
}
