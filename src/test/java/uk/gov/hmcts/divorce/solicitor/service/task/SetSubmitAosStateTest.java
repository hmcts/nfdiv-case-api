package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AosDrafted;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Disputed;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SetSubmitAosStateTest {

    @InjectMocks
    private SetSubmitAosState setSubmitAosState;

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

        final CaseDetails<CaseData, State> result = setSubmitAosState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(Disputed);
    }

    @Test
    void shouldSetStateToHoldingIfJurisdictionNotDisputed() {

        final AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService.builder()
            .jurisdictionAgree(YES)
            .build();

        final CaseData caseData = caseData();
        caseData.setAcknowledgementOfService(acknowledgementOfService);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setState(AosDrafted);
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setSubmitAosState.apply(caseDetails);

        assertThat(result.getState()).isEqualTo(Holding);
    }
}