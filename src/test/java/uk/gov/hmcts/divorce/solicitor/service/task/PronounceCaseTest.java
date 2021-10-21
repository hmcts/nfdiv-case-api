package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class PronounceCaseTest {

    @InjectMocks
    private PronounceCase pronounceCase;

    @Test
    void shouldPopulatePronouncementCaseFieldsWhenTaskIsExecuted() {
        final var caseData = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .dateAndTimeOfHearing(LocalDateTime.now())
                    .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final var result = pronounceCase.apply(caseDetails);

        assertThat(result.getData().getConditionalOrder().getGrantedDate(), is(LocalDate.now()));
        assertThat(result.getData().getFinalOrder().getDateFinalOrderEligibleFrom(), is(LocalDate.now().plusWeeks(6).plusDays(1)));
        assertThat(result.getData().getConditionalOrder().getOutcomeCase(), is(YES));
    }
}
