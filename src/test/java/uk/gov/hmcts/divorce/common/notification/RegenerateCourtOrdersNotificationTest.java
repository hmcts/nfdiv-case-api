package uk.gov.hmcts.divorce.common.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.caseworker.service.print.RegenerateCourtOrdersPrinter;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
public class RegenerateCourtOrdersNotificationTest {

    @Mock
    private RegenerateCourtOrdersPrinter printer;

    @InjectMocks
    private RegenerateCourtOrdersNotification notification;

    @Test
    void shouldPrintRegeneratedCourtOrdersIfApplicant1Offline() {
        final CaseData caseData = new CaseData();
        notification.sendToApplicant1Offline(caseData, TEST_CASE_ID);
        verify(printer).print(caseData, TEST_CASE_ID, true);
    }

    @Test
    void shouldPrintRegeneratedCourtOrdersIfApplicant2Offline() {
        final CaseData caseData = new CaseData();
        notification.sendToApplicant2Offline(caseData, TEST_CASE_ID);
        verify(printer).print(caseData, TEST_CASE_ID, false);
    }
}
