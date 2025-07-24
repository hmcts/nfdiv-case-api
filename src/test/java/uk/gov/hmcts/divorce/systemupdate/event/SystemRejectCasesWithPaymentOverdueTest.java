package uk.gov.hmcts.divorce.systemupdate.event;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.common.notification.ApplicationRejectedFeeNotPaidNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.divorce.divorcecase.model.UserRole.CREATOR;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class SystemRejectCasesWithPaymentOverdueTest {

    @InjectMocks
    private SystemRejectCasesWithPaymentOverdue systemRejectCasesWithPaymentOverdue;

    @Mock
    private ApplicationRejectedFeeNotPaidNotification applicationRejectedFeeNotPaidNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private CcdAccessService caseAccessService;

    @Test
    void shouldSendNotifications() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder().data(caseData).build();

        systemRejectCasesWithPaymentOverdue.aboutToSubmit(details, details);

        verify(notificationDispatcher).send(applicationRejectedFeeNotPaidNotification, caseData, details.getId());
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldUnlinkApplicants() {
        final var caseDetails = new CaseDetails<CaseData, State>();
        caseDetails.setId(TEST_CASE_ID);

        systemRejectCasesWithPaymentOverdue.aboutToSubmit(caseDetails, caseDetails);

        verify(caseAccessService).removeUsersWithRole(anyLong(), eq(List.of(CREATOR.getRole())));
    }
}
