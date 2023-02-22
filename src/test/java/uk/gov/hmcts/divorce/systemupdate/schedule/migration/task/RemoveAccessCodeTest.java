package uk.gov.hmcts.divorce.systemupdate.schedule.migration.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class RemoveAccessCodeTest {

    @InjectMocks
    private RemoveAccessCode removeAccessCode;

    @Test
    void shouldSetConfirmReadPetitionFields() {

        final CaseData caseData = caseData();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = removeAccessCode.apply(caseDetails);

        assertThat(result.getData().getCaseInvite().accessCode()).isEqualTo(null);
    }
}
