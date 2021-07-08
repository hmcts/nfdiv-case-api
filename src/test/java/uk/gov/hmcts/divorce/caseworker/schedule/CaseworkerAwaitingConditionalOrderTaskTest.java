package uk.gov.hmcts.divorce.caseworker.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerAwaitingConditionalOrder.CASEWORKER_AWAITING_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.common.model.State.Holding;

@ExtendWith(MockitoExtension.class)
class CaseworkerAwaitingConditionalOrderTaskTest {

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CaseworkerAwaitingConditionalOrderTask awaitingConditionalOrderTask;

    @Test
    void shouldTriggerAwaitingConditionalOrderOnEachCaseWhenCaseIsInHoldingForMoreThan20Weeks() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        CaseData caseData1 = CaseData.builder().issueDate(LocalDate.of(2021, 1, 1)).build();
        when(objectMapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData1);

        CaseData caseData2 = CaseData.builder().issueDate(LocalDate.of(2021, 1, 2)).build();
        when(objectMapper.convertValue(caseDetails2.getData(), CaseData.class)).thenReturn(caseData2);

        when(ccdSearchService.searchForAllCasesWithStateOf(Holding)).thenReturn(caseDetailsList);

        awaitingConditionalOrderTask.execute();

        verify(ccdUpdateService).submitEvent(caseDetails1, CASEWORKER_AWAITING_CONDITIONAL_ORDER);
        verify(ccdUpdateService).submitEvent(caseDetails2, CASEWORKER_AWAITING_CONDITIONAL_ORDER);
    }

    @Test
    void shouldNotTriggerAwaitingConditionalOrderWhenCaseIsInHoldingForLessThan20Weeks() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);

        CaseData caseData1 = CaseData.builder().issueDate(LocalDate.now()).build();
        when(objectMapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData1);

        when(ccdSearchService.searchForAllCasesWithStateOf(Holding)).thenReturn(singletonList(caseDetails1));

        awaitingConditionalOrderTask.execute();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithStateOf(Holding))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        awaitingConditionalOrderTask.execute();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);
        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        CaseData caseData1 = CaseData.builder().issueDate(LocalDate.of(2021, 1, 1)).build();
        when(objectMapper.convertValue(caseDetails1.getData(), CaseData.class)).thenReturn(caseData1);

        CaseData caseData2 = CaseData.builder().issueDate(LocalDate.of(2021, 1, 2)).build();
        when(objectMapper.convertValue(caseDetails2.getData(), CaseData.class)).thenReturn(caseData2);

        when(ccdSearchService.searchForAllCasesWithStateOf(Holding)).thenReturn(caseDetailsList);

        doThrow(new CcdManagementException("Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(caseDetails1, CASEWORKER_AWAITING_CONDITIONAL_ORDER);

        awaitingConditionalOrderTask.execute();

        verify(ccdUpdateService).submitEvent(caseDetails1, CASEWORKER_AWAITING_CONDITIONAL_ORDER);
        verify(ccdUpdateService).submitEvent(caseDetails2, CASEWORKER_AWAITING_CONDITIONAL_ORDER);
    }
}
