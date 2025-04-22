package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.testutil.TestConstants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerPrepareForCaseFlags.CASEWORKER_PREPARE_FOR_CASEFLAGS;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CaseworkerPrepareForCaseFlagsTest {

    @Mock
    private CaseFlagsService caseFlagsService;

    @InjectMocks
    private CaseworkerPrepareForCaseFlags caseworkerPrepareForCaseFlags;

    @Test
    void shouldAddConfigurationToConfigBuilder() throws Exception {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerPrepareForCaseFlags.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_PREPARE_FOR_CASEFLAGS);
    }

    @Test
    void shouldSetFlagForCaseFlagsSetupComplete() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(CaseData.builder().build());
        caseDetails.setId(TestConstants.TEST_CASE_ID);

        var response = caseworkerPrepareForCaseFlags.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getCaseFlagsSetupComplete()).isEqualTo(YesOrNo.YES);
    }

    @Test
    void shouldInitialiseCaseFlags() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(CaseData.builder().build());
        caseDetails.setId(TestConstants.TEST_CASE_ID);

        var response = caseworkerPrepareForCaseFlags.aboutToSubmit(caseDetails, null);

        verify(caseFlagsService).initialiseCaseFlags(caseDetails.getData());
    }

    @Test
    void shouldSetHmctsServiceIdInSubmittedCallback() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TestConstants.TEST_CASE_ID);

        caseworkerPrepareForCaseFlags.submitted(caseDetails, null);

        verify(caseFlagsService).setSupplementaryDataForCaseFlags(TestConstants.TEST_CASE_ID);
    }
}
