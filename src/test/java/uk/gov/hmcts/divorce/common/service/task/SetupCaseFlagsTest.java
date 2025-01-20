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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SetupCaseFlagsTest {

    @Mock
    private CaseFlagsService caseFlagsService;

    @InjectMocks
    private SetupCaseFlags setupCaseFlags;

    @Test
    void shouldSetCaseFlagsSetupStatus() {

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setupCaseFlags.apply(caseDetails);

        assertThat(result.getData().getCaseFlagsSetupComplete()).isEqualTo(YES);
    }

    @Test
    void shouldInitialiseCaseFlags() {

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> result = setupCaseFlags.apply(caseDetails);

        verify(caseFlagsService).initialiseCaseFlags(caseData);
    }
}
