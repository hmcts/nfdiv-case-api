package uk.gov.hmcts.divorce.systemupdate.schedule.migration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.bulkaction.service.CaseTriggerService;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.systemupdate.convert.CaseDetailsConverter;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.predicate.CaseCourtHearingPredicate;
import uk.gov.hmcts.divorce.systemupdate.schedule.migration.task.CaseCourtHearingTaskProvider;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemMigrateCaseWithCoEGeneration.SYSTEM_MIGRATE_CASE_WITH_COE_GENERATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
class SetCaseCourtHearingBulkActionTest {

    @Mock
    private CaseCourtHearingPredicate caseCourtHearingPredicate;

    @Mock
    private CaseDetailsConverter caseDetailsConverter;

    @Mock
    private CaseTriggerService caseTriggerService;

    @Mock
    private CaseCourtHearingTaskProvider caseCourtHearingTask;

    @InjectMocks
    private SetCaseCourtHearingBulkAction setCaseCourtHearingBulkAction;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
    }

    @Test
    void shouldMigrateAllCasesInBulkCaseThatDoNotHaveHearingSet() {

        final AtomicInteger predicateIndex = new AtomicInteger(0);
        final List<Boolean> predicateValues = List.of(TRUE, FALSE, TRUE);

        final ListValue<BulkListCaseDetails> bulkListCaseDetails1 = ListValue.<BulkListCaseDetails>builder()
            .value(BulkListCaseDetails.builder().caseReference(CaseLink.builder().caseReference("1").build()).build())
            .build();
        final ListValue<BulkListCaseDetails> bulkListCaseDetails2 = ListValue.<BulkListCaseDetails>builder()
            .value(BulkListCaseDetails.builder().caseReference(CaseLink.builder().caseReference("2").build()).build())
            .build();
        final ListValue<BulkListCaseDetails> bulkListCaseDetails3 = ListValue.<BulkListCaseDetails>builder()
            .value(BulkListCaseDetails.builder().caseReference(CaseLink.builder().caseReference("3").build()).build())
            .build();

        final List<ListValue<BulkListCaseDetails>> listValues = List.of(bulkListCaseDetails1, bulkListCaseDetails2, bulkListCaseDetails3);

        final CaseDetails reformCaseDetails = CaseDetails.builder().build();
        final BulkActionCaseData bulkActionCaseData = BulkActionCaseData.builder()
            .bulkListCaseDetails(listValues)
            .build();

        final uk.gov.hmcts.ccd.sdk.api.CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails
            = new uk.gov.hmcts.ccd.sdk.api.CaseDetails<>();
        bulkCaseDetails.setData(bulkActionCaseData);
        final CaseTask caseTask = caseDetails -> caseDetails;

        when(caseDetailsConverter.convertToBulkActionCaseDetailsFromReformModel(reformCaseDetails)).thenReturn(bulkCaseDetails);
        when(caseCourtHearingPredicate.caseHearingIsNotSet(bulkActionCaseData, user, SERVICE_AUTHORIZATION))
            .thenReturn(listValue -> predicateValues.get(predicateIndex.getAndAdd(1)));
        when(caseCourtHearingTask.getCaseTask(bulkCaseDetails)).thenReturn(caseTask);

        setCaseCourtHearingBulkAction.setCaseCourtHearing(reformCaseDetails, user, SERVICE_AUTHORIZATION);

        verify(caseTriggerService).caseTrigger(
            bulkListCaseDetails1,
            SYSTEM_MIGRATE_CASE_WITH_COE_GENERATION,
            caseTask,
            user,
            SERVICE_AUTHORIZATION);
        verify(caseTriggerService).caseTrigger(
            bulkListCaseDetails3,
            SYSTEM_MIGRATE_CASE_WITH_COE_GENERATION,
            caseTask,
            user,
            SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(caseTriggerService);
    }
}