package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.*;
import uk.gov.hmcts.divorce.common.notification.EmailUpdatedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.task.DivorceApplicationRemover;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateD84Form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class EmailUpdateServiceTest {

    @Mock
    private SetCaseInviteApplicant1 setCaseInviteApplicant1;
    @Mock
    private SetCaseInviteApplicant2 setCaseInviteApplicant2;
    @Mock
    private SendCaseInviteToApplicant1 sendCaseInviteToApplicant1;
    @Mock
    private SendCaseInviteToApplicant2 sendCaseInviteToApplicant2;
    @Mock
    private EmailUpdatedNotification emailUpdatedNotification;
    @InjectMocks
    private EmailUpdateService emailUpdateService;

    @Test
    void shouldRunProcessUpdateForApplicant1() {
        //TBD
    }

    @Test
    void shouldRunProcessUpdateForApplicant2() {
        //TBD
    }

    @Test
    void shouldSendNotificationToOldEmail() {
        //TBD
    }
}
