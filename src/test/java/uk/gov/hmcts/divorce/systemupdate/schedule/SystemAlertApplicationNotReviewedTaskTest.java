package uk.gov.hmcts.divorce.systemupdate.schedule;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.divorce.citizen.notification.JointApplicationOverdueNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingApplicant2Response;

@ExtendWith(MockitoExtension.class)
public class SystemAlertApplicationNotReviewedTaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private JointApplicationOverdueNotification jointApplicationOverdueNotification;

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private SystemAlertApplicationNotReviewedTask systemAlertApplicationNotReviewedTask;

    @Test
    void shouldSendEmailForOverdueJointApplication() {
        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final CaseData caseData1 = CaseData.builder().dueDate(LocalDate.now()).build();
        final CaseData caseData2 = CaseData.builder().dueDate(LocalDate.now().plusDays(5)).build();

        when(caseDetails1.getData()).thenReturn(Map.of("dueDate", LocalDate.now()));
        when(caseDetails1.getId()).thenReturn(1L);
        when(caseDetails2.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(5)));
        when(caseDetails2.getId()).thenReturn(2L);

        when(mapper.convertValue(Map.of("dueDate", LocalDate.now()), CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(Map.of("dueDate", LocalDate.now().plusDays(5)), CaseData.class)).thenReturn(caseData2);

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response)).thenReturn(caseDetailsList);

        systemAlertApplicationNotReviewedTask.execute();

        verify(jointApplicationOverdueNotification).send(caseData1, caseDetails1.getId());
        verify(jointApplicationOverdueNotification, times(0)).send(caseData2, caseDetails2.getId());
    }

    @Test
    void shouldIgnoreCaseWhenDueDateIsNull() {
        final CaseDetails caseDetails = mock(CaseDetails.class);
        final CaseData caseData = CaseData.builder().dueDate(null).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("dueDate", null);

        when(caseDetails.getId()).thenReturn(1L);
        when(caseDetails.getData()).thenReturn(caseDataMap);
        when(mapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response)).thenReturn(List.of(caseDetails));

        systemAlertApplicationNotReviewedTask.execute();

        verify(jointApplicationOverdueNotification, never()).send(caseData, caseDetails.getId());
    }

    @Test
    void shouldNotTriggerAosOverdueTaskOnEachCaseWhenCaseDueDateIsAfterCurrentDate() {
        final CaseDetails caseDetails = mock(CaseDetails.class);
        final CaseData caseData = CaseData.builder().dueDate(LocalDate.now().plusDays(5)).build();

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("dueDate", LocalDate.now().plusDays(5));

        when(caseDetails.getData()).thenReturn(Map.of("dueDate", LocalDate.now().plusDays(5)));
        when(mapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response)).thenReturn(singletonList(caseDetails));

        systemAlertApplicationNotReviewedTask.execute();

        verifyNoInteractions(jointApplicationOverdueNotification);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithStateOf(AwaitingApplicant2Response))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemAlertApplicationNotReviewedTask.execute();

        verifyNoInteractions(jointApplicationOverdueNotification);
    }
}
