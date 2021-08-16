package uk.gov.hmcts.divorce.solicitor.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import java.time.Clock;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Disputed;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDateTime;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SolicitorSubmitAosServiceTest {

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @Mock
    private Clock clock;

    @InjectMocks
    private SolicitorSubmitAosService solicitorSubmitAosService;

    @Test
    void shouldSetStateToDisputedIfJurisdictionDisputed() {

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .jurisdictionAgree(NO)
            .build();

        final CaseData caseData = caseData();
        caseData.setAcknowledgementOfService(acknowledgementOfService);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(AosDrafted);
        caseDetails.setData(caseData);

        setMockClock(clock);

        final CaseDetails<CaseData, State> result = solicitorSubmitAosService.submitAos(caseDetails);

        assertThat(result.getState()).isEqualTo(Disputed);
        assertThat(result.getData().getAcknowledgementOfService().getDateAosSubmitted()).isEqualTo(getExpectedLocalDateTime());
        assertThat(result.getData().getDueDate()).isNull();
    }

    @Test
    void shouldSetStateToHoldingIfJurisdictionNotDisputed() {

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .jurisdictionAgree(YES)
            .build();

        setMockClock(clock);
        final LocalDate issueDate = getExpectedLocalDate();

        final CaseData caseData = caseData();
        caseData.getApplication().setIssueDate(issueDate);
        caseData.setAcknowledgementOfService(acknowledgementOfService);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(AosDrafted);
        caseDetails.setData(caseData);

        when(holdingPeriodService.getDueDateAfter(issueDate)).thenReturn(issueDate);

        final CaseDetails<CaseData, State> result = solicitorSubmitAosService.submitAos(caseDetails);

        assertThat(result.getState()).isEqualTo(Holding);
        assertThat(result.getData().getAcknowledgementOfService().getDateAosSubmitted()).isEqualTo(getExpectedLocalDateTime());
        assertThat(result.getData().getDueDate()).isEqualTo(issueDate);
    }
}
