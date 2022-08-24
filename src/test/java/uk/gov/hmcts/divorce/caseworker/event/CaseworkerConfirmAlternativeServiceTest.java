package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.divorce.caseworker.service.task.SetHoldingDueDate;
import uk.gov.hmcts.divorce.common.service.task.SetServiceConfirmed;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerConfirmAlternativeService.CASEWORKER_CONFIRM_ALTERNATIVE_SERVICE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class CaseworkerConfirmAlternativeServiceTest {

    @Mock
    private SetHoldingDueDate setHoldingDueDate;

    @Mock
    private SetServiceConfirmed setServiceConfirmed;

    @InjectMocks
    private CaseworkerConfirmAlternativeService caseworkerConfirmAlternativeService;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerConfirmAlternativeService.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_CONFIRM_ALTERNATIVE_SERVICE);
    }

    @Test
    void shouldCallSetHoldingDueDateAndSetServiceConfirmedTaskOnAboutToSubmit() {

        final CaseData caseData = caseData();
        final CaseData updatedCaseData = caseData();
        updatedCaseData.setDueDate(getExpectedLocalDate());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(updatedCaseData);

        when(setHoldingDueDate.apply(caseDetails)).thenReturn(caseDetails);
        when(setServiceConfirmed.apply(caseDetails)).thenReturn(updatedCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = caseworkerConfirmAlternativeService.aboutToSubmit(
            caseDetails,
            new CaseDetails<>());

        assertThat(response.getData()).isEqualTo(updatedCaseData);
        verify(setHoldingDueDate).apply(caseDetails);
        verify(setServiceConfirmed).apply(caseDetails);
    }
}
