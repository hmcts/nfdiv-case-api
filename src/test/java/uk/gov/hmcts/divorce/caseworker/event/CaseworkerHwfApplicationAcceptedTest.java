package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.caseworker.service.CaseFlagsService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.testutil.TestConstants;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerHwfApplicationAccepted.CASEWORKER_HWF_APPLICATION_ACCEPTED;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerHwfApplicationAcceptedTest {

    @Mock
    private CaseworkerHwfApplicationAndPaymentHelper caseworkerHwfApplicationAndPaymentHelper;

    @Mock
    private CaseFlagsService caseFlagsService;

    @InjectMocks
    private CaseworkerHwfApplicationAccepted caseworkerHwfApplicationAccepted;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerHwfApplicationAccepted.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_HWF_APPLICATION_ACCEPTED);
    }

    @Test
    void shouldSetStateAndDates() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        when(caseworkerHwfApplicationAndPaymentHelper.getState(caseData)).thenReturn(Submitted);
        when(caseworkerHwfApplicationAndPaymentHelper.setDateSubmittedAndDueDate(caseData)).thenReturn(caseData);

        caseworkerHwfApplicationAccepted.aboutToSubmit(caseDetails, null);

        verify(caseworkerHwfApplicationAndPaymentHelper).getState(caseData);
        verify(caseworkerHwfApplicationAndPaymentHelper).setDateSubmittedAndDueDate(caseData);
    }

    @Test
    void shouldSetDefaultCaseDataRequiredForPostSubmissionCases() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        caseworkerHwfApplicationAccepted.aboutToSubmit(caseDetails, null);

        verify(caseworkerHwfApplicationAndPaymentHelper).setRequiredCaseFieldsForPostSubmissionCase(caseDetails);
    }

    @Test
    void shouldCallCaseFlagsServiceToSetHmctsServiceId() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TestConstants.TEST_CASE_ID);

        caseworkerHwfApplicationAccepted.submitted(caseDetails, null);

        verify(caseFlagsService).setSupplementaryDataForCaseFlags(TestConstants.TEST_CASE_ID);
    }
}
