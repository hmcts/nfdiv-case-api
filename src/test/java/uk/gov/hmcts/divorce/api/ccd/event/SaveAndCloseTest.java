package uk.gov.hmcts.divorce.api.ccd.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.api.ccd.event.SaveAndClose;
import uk.gov.hmcts.divorce.api.ccd.model.CaseData;
import uk.gov.hmcts.divorce.api.ccd.model.State;
import uk.gov.hmcts.divorce.api.notification.handler.SaveAndSignOutNotificationHandler;

import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.api.util.TestDataHelper.caseData;


@ExtendWith(SpringExtension.class)
public class SaveAndCloseTest {
    @Mock
    private SaveAndSignOutNotificationHandler saveAndSignOutNotificationHandler;

    @InjectMocks
    private SaveAndClose saveAndClose;

    @Test
    public void givenValidCaseDataWhenCallbackIsInvokedThenSendEmail() throws Exception {
        final CaseData caseData = caseData();
        caseData.setPetitionerEmail("test@test.com");

        final CaseDetails<CaseData, State> details = new CaseDetails<>();
        details.setData(caseData);

        saveAndClose.submitted(details, details);

        verify(saveAndSignOutNotificationHandler).notifyApplicant(caseData);
    }
}
