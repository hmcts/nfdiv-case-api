package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
class SetServiceConfirmedTest {

    @InjectMocks
    private SetServiceConfirmed setServiceConfirmed;

    @Test
    void shouldSetServiceConfirmed() {

        final CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> updatedDetails = setServiceConfirmed.apply(caseDetails);

        assertThat(updatedDetails.getData().getConditionalOrder().getServiceConfirmed()).isEqualTo(YES);
    }
}