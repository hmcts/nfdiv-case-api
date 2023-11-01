package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorService;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;


@ExtendWith(MockitoExtension.class)
public class SetConfirmServiceDueDateTest {

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @InjectMocks
    private SetConfirmServiceDueDate setConfirmServiceDueDate;

    @Test
    void shouldSetDueDateTo141DaysFromIssueDate() {
        final var caseData = caseData();
        final var issueDate = LocalDate.of(2021, 10, 12);
        final var expectedDueDate = issueDate.plusDays(141);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setApplication(
            Application.builder()
                .solicitorService(SolicitorService.builder()
                    .dateOfService(issueDate)
                    .build())
                .issueDate(issueDate)
                .build()
        );
        caseDetails.setData(caseData);

        when(holdingPeriodService.getDueDateFor(issueDate)).thenReturn(expectedDueDate);

        final CaseDetails<CaseData, State> result = setConfirmServiceDueDate.apply(caseDetails);

        assertThat(result.getData().getDueDate()).isEqualTo(expectedDueDate);
    }
}
