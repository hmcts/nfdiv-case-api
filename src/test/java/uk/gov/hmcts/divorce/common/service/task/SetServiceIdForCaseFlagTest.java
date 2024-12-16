package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class SetServiceIdForCaseFlagTest {
    @InjectMocks
    private SetServiceIdForCaseFlag setServiceIdForCaseFlag;

    @Mock
    private CaseFlagsService caseFlagsService;

    @Test
    void shouldCallCaseFlagServiceToSetSupplementaryData() {
        final CaseData caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(Submitted);

        final CaseDetails<CaseData, State> result = setServiceIdForCaseFlag.apply(caseDetails);

        verify(caseFlagsService).setSupplementaryDataForCaseFlags(TEST_CASE_ID);
    }
}
