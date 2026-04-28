
package uk.gov.hmcts.divorce.systemupdate.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.service.CaseTerminationService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.payment.service.PaymentStatusService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemRejectCasesWithPaymentOverdue.ERROR_CASE_HAS_SUCCESSFUL_PAYMENT;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SystemRejectCasesWithPaymentOverdueTest {

    @InjectMocks
    private SystemRejectCasesWithPaymentOverdue systemRejectCasesWithPaymentOverdue;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseTerminationService caseTerminationService;

    @Mock
    private PaymentStatusService paymentStatusService;

    @Test
    void shouldSendNotifications() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();

        final var user = mock(User.class);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        systemRejectCasesWithPaymentOverdue.aboutToSubmit(details, details);

        verify(caseTerminationService).reject(details);
    }

    @Test
    void shouldReturnErrorIfRecentPaymentIsFound() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();

        final var user = mock(User.class);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);

        when(paymentStatusService.hasSuccessfulPayment(details, user.getAuthToken(), SERVICE_AUTHORIZATION))
            .thenReturn(true);

        var response = systemRejectCasesWithPaymentOverdue.aboutToSubmit(details, details);

        assertThat(response.getErrors()).isEqualTo(List.of(ERROR_CASE_HAS_SUCCESSFUL_PAYMENT));

        verifyNoInteractions(caseTerminationService);
    }
}
