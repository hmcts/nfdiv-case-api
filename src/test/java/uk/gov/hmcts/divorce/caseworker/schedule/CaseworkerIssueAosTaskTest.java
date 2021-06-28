package uk.gov.hmcts.divorce.caseworker.schedule;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.caseworker.service.CcdManagementException;
import uk.gov.hmcts.divorce.caseworker.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.caseworker.service.CcdSearchService;
import uk.gov.hmcts.divorce.caseworker.service.CcdUpdateService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerIssueAos.CASEWORKER_ISSUE_AOS;
import static uk.gov.hmcts.divorce.common.model.State.Issued;

@ExtendWith(MockitoExtension.class)
class CaseworkerIssueAosTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @InjectMocks
    private CaseworkerIssueAosTask caseworkerIssueAosTask;

    @Test
    void shouldSearchForAllIssuedCasesAndSubmitIssueAosEventOnEachCase() {

        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithStateOf(Issued)).thenReturn(caseDetailsList);

        caseworkerIssueAosTask.issueAosTask();

        verify(ccdUpdateService).submitEvent(caseDetails1, CASEWORKER_ISSUE_AOS);
        verify(ccdUpdateService).submitEvent(caseDetails2, CASEWORKER_ISSUE_AOS);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {

        when(ccdSearchService.searchForAllCasesWithStateOf(Issued))
            .thenThrow(new CcdSearchCaseException("Message", mock(FeignException.class)));

        caseworkerIssueAosTask.issueAosTask();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownBySubmitEvent() {

        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithStateOf(Issued)).thenReturn(caseDetailsList);

        doThrow(new CcdManagementException("Message", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, CASEWORKER_ISSUE_AOS);

        caseworkerIssueAosTask.issueAosTask();

        verify(ccdUpdateService).submitEvent(caseDetails1, CASEWORKER_ISSUE_AOS);
        verify(ccdUpdateService).submitEvent(caseDetails2, CASEWORKER_ISSUE_AOS);
    }
}
