package uk.gov.hmcts.divorce.caseworker.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.divorce.common.service.WithdrawCaseService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerWithdrawn.CASEWORKER_WITHDRAWN;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;

@ExtendWith(MockitoExtension.class)
class CaseworkerWithdrawnTest {

    @Mock
    private WithdrawCaseService withdrawCaseService;

    @InjectMocks
    private CaseworkerWithdrawn caseworkerWithdrawn;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerWithdrawn.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_WITHDRAWN);
    }

    @Test
    void shouldWithdrawCaseByDelegatingToWithdrawCaseService() {
        final var caseDetails = new CaseDetails<CaseData, State>();

        caseworkerWithdrawn.aboutToSubmit(caseDetails, caseDetails);

        verify(withdrawCaseService).withdraw(caseDetails);
    }

    @Test
    void shouldReturnErrorFromMidEventCallbackWhenWithdrawalDetailsIsNotProvidedWhenReasonSelectedIsOther() {
        final var caseData = new CaseData();
        caseData.getApplication().setCwWithdrawApplicationReason(
            uk.gov.hmcts.divorce.divorcecase.model.WithdrawApplicationReasonType.OTHER
        );
        caseData.getApplication().setCwWithdrawApplicationDetails(null);

        final var caseDetails = new CaseDetails<CaseData, State>();
        caseDetails.setData(caseData);

        final var response = caseworkerWithdrawn.midEvent(caseDetails, caseDetails);

        assertThat(response.getErrors()).containsExactly(
            CaseworkerWithdrawn.DETAILS_NOT_PROVIDED
        );
    }
}
