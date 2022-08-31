package uk.gov.hmcts.divorce.caseworker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateFinalOrder;
import uk.gov.hmcts.divorce.caseworker.service.task.SendFinalOrderGrantedNotifications;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.GeneralParties.APPLICANT;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataWithGeneralLetter;

@ExtendWith(MockitoExtension.class)
public class GrantFinalOrderServiceTest {
    @Mock
    private GenerateFinalOrder grantFinalOrder;

    @Mock
    private SendFinalOrderGrantedNotifications notifications;

    @InjectMocks
    private GrantFinalOrderService service;

    @Test
    public void shouldProcessGenerateFinalOrder() {

        var caseData = buildCaseDataWithGeneralLetter(APPLICANT);
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(1L);

        when(grantFinalOrder.apply(caseDetails)).thenReturn(caseDetails);
        when(notifications.apply(caseDetails)).thenReturn(caseDetails);

        service.process(caseDetails);

        verify(grantFinalOrder).apply(caseDetails);
        verify(notifications).apply(caseDetails);
    }
}
