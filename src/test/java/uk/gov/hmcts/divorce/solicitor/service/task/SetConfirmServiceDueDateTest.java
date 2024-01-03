package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.SolicitorService;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;


@ExtendWith(MockitoExtension.class)
public class SetConfirmServiceDueDateTest {

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @InjectMocks
    private SetConfirmServiceDueDate setConfirmServiceDueDate;

    @BeforeEach
    void setPageSize() {
        ReflectionTestUtils.setField(setConfirmServiceDueDate, "dueDateOffsetDays", 16L);
    }

    @Test
    void shouldSetDueDateTo16DaysFromServiceDateWhenServiceNotProcessedByProcessServer() {
        final var caseData = caseData();
        final var serviceDate = LocalDate.of(2021, 10, 12);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setApplication(
            Application.builder()
                .solicitorService(SolicitorService.builder()
                    .dateOfService(serviceDate)
                    .build())
                .build()
        );
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = setConfirmServiceDueDate.apply(caseDetails);

        final LocalDate expectedDueDate = serviceDate.plusDays(16);

        assertThat(result.getData().getDueDate()).isEqualTo(expectedDueDate);

        verifyNoInteractions(holdingPeriodService);
    }

    @Test
    void shouldSetDueDateTo141DaysFromIssueDateWhenServiceProcessedByProcessServer() {
        final var caseData = caseData();
        final var issueDate = LocalDate.of(2021, 10, 12);
        final var expectedDueDate = issueDate.plusDays(141);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setApplication(
            Application.builder()
                .solicitorService(SolicitorService.builder()
                    .dateOfService(issueDate)
                    .serviceProcessedByProcessServer(Set.of(SolicitorService.ServiceProcessedByProcessServer.CONFIRM))
                    .build())
                .issueDate(issueDate)
                .build()
        );
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(holdingPeriodService.getDueDateFor(issueDate)).thenReturn(expectedDueDate);

        final CaseDetails<CaseData, State> result = setConfirmServiceDueDate.apply(caseDetails);

        assertThat(result.getData().getDueDate()).isEqualTo(expectedDueDate);
    }

    @Test
    void shouldNotSetDueDateIfAosPreviouslySubmitted() {
        final var caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.setAcknowledgementOfService(
            AcknowledgementOfService.builder().dateAosSubmitted(LocalDateTime.now()).build()
        );
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final CaseDetails<CaseData, State> result = setConfirmServiceDueDate.apply(caseDetails);
        assertThat(result.getData().getDueDate()).isNull();

        verifyNoInteractions(holdingPeriodService);
    }
}
