package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdCreateService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemLinkWithBulkCase.SYSTEM_LINK_WITH_BULK_CASE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class SystemCreateBulkCaseListTaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdCreateService ccdCreateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private FailedBulkCaseRemover failedBulkCaseRemover;

    @InjectMocks
    private SystemCreateBulkCaseListTask systemCreateBulkCaseListTask;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldCreateBulkCaseWithTwoCasesWhenCasesAreInAwaitingPronouncementStateAndMinimumMaximumBatchLimitIsSatisfied() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final var caseData1 =
            caseDataWithApplicant("app1fname1", "app1lname1", "app2fname1", "app2lname1");

        final var caseData2 =
            caseDataWithApplicant("app1fname2", "app1lname2", "app2fname2", "app2lname2");

        final CaseDetails<CaseData, State> caseDetails1 = new CaseDetails<>();
        caseDetails1.setId(1L);
        caseDetails1.setData(caseData1);

        final CaseDetails<CaseData, State> caseDetails2 = new CaseDetails<>();
        caseDetails2.setId(2L);
        caseDetails2.setData(caseData2);

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1, caseDetails2));
        searchResults.offer(emptyList());

        final var bulkListCaseDetails1 =
            bulkListCaseDetailsListValue("app1fname1 app1lname1 vs app2fname1 app2lname1", 1L);
        final var bulkListCaseDetails2 =
            bulkListCaseDetailsListValue("app1fname2 app1lname2 vs app2fname2 app2lname2", 2L);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails = new CaseDetails<>();
        bulkActionCaseDetails.setCaseTypeId(BulkActionCaseTypeConfig.CASE_TYPE);
        bulkActionCaseDetails.setData(BulkActionCaseData.builder()
            .bulkListCaseDetails(List.of(bulkListCaseDetails1, bulkListCaseDetails2))
            .build());

        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails = new CaseDetails<>();
        createdCaseDetails.setId(3L);

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION))
            .thenReturn(searchResults);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails, user, SERVICE_AUTHORIZATION))
            .thenReturn(createdCaseDetails);

        doNothing().when(ccdUpdateService)
            .submitEventWithRetry(caseDetails1, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        doNothing().when(ccdUpdateService)
            .submitEventWithRetry(caseDetails2, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService)
            .submitEventWithRetry(caseDetails1, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService)
            .submitEventWithRetry(caseDetails2, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        verifyNoMoreInteractions(ccdSearchService, ccdUpdateService);
    }

    @Test
    void shouldCreateTwoBulkCaseWhenThereAreMoreThanMaxCasesAvailable() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final var caseData1 =
            caseDataWithApplicant("app1fname1", "app1lname1", "app2fname1", "app2lname1");

        final var caseData2 =
            caseDataWithApplicant("app1fname2", "app1lname2", "app2fname2", "app2lname2");

        final var caseData3 =
            caseDataWithApplicant("app1fname3", "app1lname3", "app2fname3", "app2lname3");

        final var caseData4 =
            caseDataWithApplicant("app1fname4", "app1lname4", "app2fname4", "app2lname4");

        final CaseDetails<CaseData, State> caseDetails1 = new CaseDetails<>();
        caseDetails1.setId(1L);
        caseDetails1.setData(caseData1);

        final CaseDetails<CaseData, State> caseDetails2 = new CaseDetails<>();
        caseDetails2.setId(2L);
        caseDetails2.setData(caseData2);

        final CaseDetails<CaseData, State> caseDetails3 = new CaseDetails<>();
        caseDetails3.setId(3L);
        caseDetails3.setData(caseData3);

        final CaseDetails<CaseData, State> caseDetails4 = new CaseDetails<>();
        caseDetails4.setId(4L);
        caseDetails4.setData(caseData4);

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1, caseDetails2));
        searchResults.offer(List.of(caseDetails3, caseDetails4));
        searchResults.offer(emptyList());

        final var bulkListCaseDetails1 =
            bulkListCaseDetailsListValue("app1fname1 app1lname1 vs app2fname1 app2lname1", 1L);
        final var bulkListCaseDetails2 =
            bulkListCaseDetailsListValue("app1fname2 app1lname2 vs app2fname2 app2lname2", 2L);
        final var bulkListCaseDetails3 =
            bulkListCaseDetailsListValue("app1fname3 app1lname3 vs app2fname3 app2lname3", 3L);
        final var bulkListCaseDetails4 =
            bulkListCaseDetailsListValue("app1fname4 app1lname4 vs app2fname4 app2lname4", 4L);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails1 = new CaseDetails<>();
        bulkActionCaseDetails1.setCaseTypeId(BulkActionCaseTypeConfig.CASE_TYPE);
        bulkActionCaseDetails1.setData(BulkActionCaseData.builder()
            .bulkListCaseDetails(List.of(bulkListCaseDetails1, bulkListCaseDetails2))
            .build());

        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails1 = new CaseDetails<>();
        createdCaseDetails1.setId(5L);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails2 = new CaseDetails<>();
        bulkActionCaseDetails2.setCaseTypeId(BulkActionCaseTypeConfig.CASE_TYPE);
        bulkActionCaseDetails2.setData(BulkActionCaseData.builder()
            .bulkListCaseDetails(List.of(bulkListCaseDetails3, bulkListCaseDetails4))
            .build());

        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails2 = new CaseDetails<>();
        createdCaseDetails2.setId(6L);

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION))
            .thenReturn(searchResults);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails1, user, SERVICE_AUTHORIZATION))
            .thenReturn(createdCaseDetails1);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails2, user, SERVICE_AUTHORIZATION))
            .thenReturn(createdCaseDetails2);

        doNothing().when(ccdUpdateService)
            .submitEventWithRetry(caseDetails1, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        doNothing().when(ccdUpdateService)
            .submitEventWithRetry(caseDetails2, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        doNothing().when(ccdUpdateService)
            .submitEventWithRetry(caseDetails3, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        doNothing().when(ccdUpdateService)
            .submitEventWithRetry(caseDetails4, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails1, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService)
            .submitEventWithRetry(caseDetails1, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService)
            .submitEventWithRetry(caseDetails2, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService)
            .submitEventWithRetry(caseDetails3, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService)
            .submitEventWithRetry(caseDetails4, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        verifyNoMoreInteractions(ccdSearchService, ccdUpdateService);
    }

    @Test
    void shouldNotCreateBulkCaseWithWhenCasesInAwaitingPronouncementStateIsLessThanMinimumBatchSize() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails));

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION))
            .thenReturn(searchResults);

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION);

        verifyNoMoreInteractions(ccdSearchService, ccdUpdateService);
    }

    @Test
    void shouldRemoveAwaitingPronouncementCaseLinkFromBulkCaseWhenUpdatingBulkCaseReferenceFailsForAwaitingPronouncementCase() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final var caseData1 =
            caseDataWithApplicant("app1fname1", "app1lname1", "app2fname1", "app2lname1");

        final var caseData2 =
            caseDataWithApplicant("app1fname2", "app1lname2", "app2fname2", "app2lname2");

        final CaseDetails<CaseData, State> caseDetails1 = new CaseDetails<>();
        caseDetails1.setId(1L);
        caseDetails1.setData(caseData1);

        final CaseDetails<CaseData, State> caseDetails2 = new CaseDetails<>();
        caseDetails2.setId(2L);
        caseDetails2.setData(caseData2);

        final Deque<List<uk.gov.hmcts.ccd.sdk.api.CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1, caseDetails2));
        searchResults.offer(emptyList());

        final var bulkListCaseDetails1 =
            bulkListCaseDetailsListValue("app1fname1 app1lname1 vs app2fname1 app2lname1", 1L);
        final var bulkListCaseDetails2 =
            bulkListCaseDetailsListValue("app1fname2 app1lname2 vs app2fname2 app2lname2", 2L);

        final var bulkCaseList = new ArrayList<>(asList(bulkListCaseDetails1, bulkListCaseDetails2));

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails = new CaseDetails<>();
        bulkActionCaseDetails.setCaseTypeId(BulkActionCaseTypeConfig.CASE_TYPE);
        bulkActionCaseDetails.setData(BulkActionCaseData.builder()
            .bulkListCaseDetails(bulkCaseList)
            .build());

        final CaseDetails<BulkActionCaseData, BulkActionState> createdCaseDetails = new CaseDetails<>();
        createdCaseDetails.setId(3L);
        bulkActionCaseDetails.setData(BulkActionCaseData.builder()
            .bulkListCaseDetails(bulkCaseList)
            .build());

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION))
            .thenReturn(searchResults);

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails, user, SERVICE_AUTHORIZATION))
            .thenReturn(createdCaseDetails);

        doThrow(new CcdManagementException("some exception", mock(FeignException.class))).when(ccdUpdateService)
            .submitEventWithRetry(caseDetails1, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        doNothing().when(ccdUpdateService)
            .submitEventWithRetry(caseDetails2, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService)
            .submitEventWithRetry(caseDetails2, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);
        verify(failedBulkCaseRemover)
            .removeFailedCasesFromBulkListCaseDetails(
                List.of(1L),
                createdCaseDetails,
                user,
                SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdSearchService, ccdCreateService, ccdUpdateService);
    }

    @Test
    void shouldNotCreateBulkCaseWhenSearchCaseThrowsException() {
        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemCreateBulkCaseListTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfBulkCaseCreationThrowsException() {

        setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 1);

        final var caseData1 =
            caseDataWithApplicant("app1fname1", "app1lname1", "app2fname1", "app2lname1");

        final CaseDetails<CaseData, State> caseDetails1 = new CaseDetails<>();
        caseDetails1.setId(1L);
        caseDetails1.setData(caseData1);

        final Deque<List<CaseDetails<CaseData, State>>> searchResults = new LinkedList<>();
        searchResults.offer(List.of(caseDetails1));

        final var bulkListCaseDetails1 =
            bulkListCaseDetailsListValue("app1fname1 app1lname1 vs app2fname1 app2lname1", 1L);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkActionCaseDetails = new CaseDetails<>();
        bulkActionCaseDetails.setCaseTypeId(BulkActionCaseTypeConfig.CASE_TYPE);
        bulkActionCaseDetails.setData(BulkActionCaseData.builder()
            .bulkListCaseDetails(List.of(bulkListCaseDetails1))
            .build());

        when(ccdSearchService.searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION))
            .thenReturn(searchResults);

        doThrow(new CcdManagementException("some exception", mock(FeignException.class)))
            .when(ccdCreateService).createBulkCase(bulkActionCaseDetails, user, SERVICE_AUTHORIZATION);

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCasesAllPages(user, SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdUpdateService);
    }

    private CaseData caseDataWithApplicant(final String app1FirstName,
                                           final String app1LastName,
                                           final String app2FirstName,
                                           final String app2LastName) {
        return CaseData
            .builder()
            .applicant1(
                Applicant
                    .builder()
                    .firstName(app1FirstName)
                    .lastName(app1LastName)
                    .build()
            )
            .applicant2(
                Applicant
                    .builder()
                    .firstName(app2FirstName)
                    .lastName(app2LastName)
                    .build()
            )
            .build();
    }

    private ListValue<BulkListCaseDetails> bulkListCaseDetailsListValue(final String caseParties, final Long caseId) {
        var bulkCaseDetails = BulkListCaseDetails
            .builder()
            .caseParties(caseParties)
            .caseReference(
                CaseLink
                    .builder()
                    .caseReference(String.valueOf(caseId))
                    .build()
            )
            .build();

        return
            ListValue
                .<BulkListCaseDetails>builder()
                .value(bulkCaseDetails)
                .build();
    }
}
