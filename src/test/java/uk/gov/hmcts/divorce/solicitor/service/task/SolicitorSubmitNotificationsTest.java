package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.solicitor.service.notification.SolicitorSubmittedNotification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;

@ExtendWith(MockitoExtension.class)
class SolicitorSubmitNotificationsTest {

    @Mock
    private SolicitorSubmittedNotification solicitorSubmittedNotification;

    @InjectMocks
    private SolicitorSubmitNotification solicitorSubmitNotification;

    @Test
    void shouldSendApplicantAndSolicitorNotification() {

        final var caseData = CaseData.builder().build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final var result = solicitorSubmitNotification.apply(caseDetails);

        assertThat(result).isEqualTo(caseDetails);

        verify(solicitorSubmittedNotification).send(caseData, TEST_CASE_ID);
    }
}
