package uk.gov.hmcts.divorce.systemupdate.schedule.bulkaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionCaseTypeConfig;
import uk.gov.hmcts.divorce.bulkaction.data.BulkListCaseDetails;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdCreateService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
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
    private ObjectMapper mapper;

    @InjectMocks
    private SystemCreateBulkCaseListTask systemCreateBulkCaseListTask;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserDetails.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldCreateBulkCaseWithTwoCasesWhenCasesAreInAwaitingPronouncementStateAndMinimumMaximumBatchLimitIsSatisfied() {
        ReflectionTestUtils.setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final CaseData caseData1 =
            caseDataWithApplicant("app1fname1", "app1lname1", "app2fname1", "app2lname1");

        final CaseData caseData2 =
            caseDataWithApplicant("app1fname2", "app1lname2", "app2fname2", "app2lname2");

        final Map<String, Object> caseData1Map = new HashMap<>();
        caseData1Map.put("applicant1FirstName", "app1fname1");
        caseData1Map.put("applicant1LastName", "app1lname1");
        caseData1Map.put("applicant2FirstName", "app2fname1");
        caseData1Map.put("applicant2LastName", "app2lname1");

        when(caseDetails1.getData()).thenReturn(caseData1Map);
        when(caseDetails1.getId()).thenReturn(1L);

        final Map<String, Object> caseData2Map = new HashMap<>();
        caseData2Map.put("applicant1FirstName", "app1fname2");
        caseData2Map.put("applicant1LastName", "app1lname2");
        caseData2Map.put("applicant2FirstName", "app2fname2");
        caseData2Map.put("applicant2LastName", "app2lname2");

        when(caseDetails2.getData()).thenReturn(caseData2Map);
        when(caseDetails2.getId()).thenReturn(2L);

        when(mapper.convertValue(caseData1Map, CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(caseData2Map, CaseData.class)).thenReturn(caseData2);

        when(ccdSearchService.searchAwaitingPronouncementCases(user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(caseDetails1, caseDetails2));

        var bulkListCaseDetails1 =
            bulkListCaseDetailsListValue("app1fname1 app1lname1 vs app2fname1 app2lname1", 1L);
        var bulkListCaseDetails2 =
            bulkListCaseDetailsListValue("app1fname2 app1lname2 vs app2fname2 app2lname2", 2L);

        var bulkActionCaseDetails =
            CaseDetails
                .builder()
                .caseTypeId(BulkActionCaseTypeConfig.CASE_TYPE)
                .data(Map.of("bulkListCaseDetails", List.of(bulkListCaseDetails1, bulkListCaseDetails2)))
                .build();

        CaseDetails caseDetailsBulkCase = CaseDetails.builder().id(3L).build();

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsBulkCase);

        doNothing().when(ccdUpdateService)
            .submitEventWithRetry(caseDetails1, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        doNothing().when(ccdUpdateService)
            .submitEventWithRetry(caseDetails2, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCases(user, SERVICE_AUTHORIZATION);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService)
            .submitEventWithRetry(caseDetails1, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService)
            .submitEventWithRetry(caseDetails2, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        verifyNoMoreInteractions(ccdSearchService, ccdUpdateService);
    }

    @Test
    void shouldNotCreateBulkCaseWithWhenCasesInAwaitingPronouncementStateIsLessThanMinimumBatchSize() {
        ReflectionTestUtils.setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails caseDetails1 = mock(CaseDetails.class);

        when(ccdSearchService.searchAwaitingPronouncementCases(user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(caseDetails1));

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCases(user, SERVICE_AUTHORIZATION);

        verifyNoMoreInteractions(ccdSearchService, ccdUpdateService);
    }

    @Test
    void shouldCreateBulkCaseWithOneCaseIfUpdatingBulkListCaseReferenceFailsForOtherCase() {
        ReflectionTestUtils.setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final CaseData caseData1 =
            caseDataWithApplicant("app1fname1", "app1lname1", "app2fname1", "app2lname1");

        final CaseData caseData2 =
            caseDataWithApplicant("app1fname2", "app1lname2", "app2fname2", "app2lname2");

        final Map<String, Object> caseData1Map = new HashMap<>();
        caseData1Map.put("applicant1FirstName", "app1fname1");
        caseData1Map.put("applicant1LastName", "app1lname1");
        caseData1Map.put("applicant2FirstName", "app2fname1");
        caseData1Map.put("applicant2LastName", "app2lname1");

        when(caseDetails1.getData()).thenReturn(caseData1Map);
        when(caseDetails1.getId()).thenReturn(1L);

        final Map<String, Object> caseData2Map = new HashMap<>();
        caseData2Map.put("applicant1FirstName", "app1fname2");
        caseData2Map.put("applicant1LastName", "app1lname2");
        caseData2Map.put("applicant2FirstName", "app2fname2");
        caseData2Map.put("applicant2LastName", "app2lname2");

        when(caseDetails2.getData()).thenReturn(caseData2Map);
        when(caseDetails2.getId()).thenReturn(2L);

        when(mapper.convertValue(caseData1Map, CaseData.class)).thenReturn(caseData1);
        when(mapper.convertValue(caseData2Map, CaseData.class)).thenReturn(caseData2);

        when(ccdSearchService.searchAwaitingPronouncementCases(user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(caseDetails1, caseDetails2));

        var bulkListCaseDetails1 =
            bulkListCaseDetailsListValue("app1fname1 app1lname1 vs app2fname1 app2lname1", 1L);
        var bulkListCaseDetails2 =
            bulkListCaseDetailsListValue("app1fname2 app1lname2 vs app2fname2 app2lname2", 2L);

        var bulkActionCaseDetails =
            CaseDetails
                .builder()
                .caseTypeId(BulkActionCaseTypeConfig.CASE_TYPE)
                .data(Map.of("bulkListCaseDetails", List.of(bulkListCaseDetails1, bulkListCaseDetails2)))
                .build();

        CaseDetails caseDetailsBulkCase = CaseDetails.builder().id(3L).build();

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsBulkCase);

        doThrow(new CcdManagementException("some exception", mock(FeignException.class))).when(ccdUpdateService)
            .submitEventWithRetry(caseDetails1, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        doNothing().when(ccdUpdateService)
            .submitEventWithRetry(caseDetails2, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCases(user, SERVICE_AUTHORIZATION);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService)
            .submitEventWithRetry(caseDetails2, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        verifyNoMoreInteractions(ccdSearchService, ccdUpdateService);
    }

    @Test
    void shouldCreateBulkCaseWithOneCaseIfDeserializingFailsForOtherCase() {
        ReflectionTestUtils.setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 2);

        final CaseDetails caseDetails1 = mock(CaseDetails.class);
        final CaseDetails caseDetails2 = mock(CaseDetails.class);

        final CaseData caseData2 =
            caseDataWithApplicant("app1fname2", "app1lname2", "app2fname2", "app2lname2");

        final Map<String, Object> caseData1Map = new HashMap<>();
        caseData1Map.put("applicant1FirstName", "app1fname1");
        caseData1Map.put("applicant1LastName", "app1lname1");
        caseData1Map.put("applicant2FirstName", "app2fname1");
        caseData1Map.put("applicant2LastName", "app2lname1");

        when(caseDetails1.getData()).thenReturn(caseData1Map);
        when(caseDetails1.getId()).thenReturn(1L);

        final Map<String, Object> caseData2Map = new HashMap<>();
        caseData2Map.put("applicant1FirstName", "app1fname2");
        caseData2Map.put("applicant1LastName", "app1lname2");
        caseData2Map.put("applicant2FirstName", "app2fname2");
        caseData2Map.put("applicant2LastName", "app2lname2");

        when(caseDetails2.getData()).thenReturn(caseData2Map);
        when(caseDetails2.getId()).thenReturn(2L);

        doThrow(new IllegalArgumentException("some exception"))
            .when(mapper).convertValue(caseData1Map, CaseData.class);

        when(mapper.convertValue(caseData2Map, CaseData.class)).thenReturn(caseData2);

        when(ccdSearchService.searchAwaitingPronouncementCases(user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(caseDetails1, caseDetails2));

        var bulkListCaseDetails2 =
            bulkListCaseDetailsListValue("app1fname2 app1lname2 vs app2fname2 app2lname2", 2L);

        var bulkActionCaseDetails =
            CaseDetails
                .builder()
                .caseTypeId(BulkActionCaseTypeConfig.CASE_TYPE)
                .data(Map.of("bulkListCaseDetails", List.of(bulkListCaseDetails2)))
                .build();

        CaseDetails caseDetailsBulkCase = CaseDetails.builder().id(3L).build();

        when(ccdCreateService.createBulkCase(bulkActionCaseDetails, user, SERVICE_AUTHORIZATION))
            .thenReturn(caseDetailsBulkCase);

        doNothing().when(ccdUpdateService)
            .submitEventWithRetry(caseDetails2, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCases(user, SERVICE_AUTHORIZATION);
        verify(ccdCreateService).createBulkCase(bulkActionCaseDetails, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService)
            .submitEventWithRetry(caseDetails2, SYSTEM_LINK_WITH_BULK_CASE, user, SERVICE_AUTHORIZATION);

        verifyNoMoreInteractions(ccdSearchService, ccdUpdateService);
    }


    @Test
    void shouldNotCreateBulkCaseWhenSearchCaseThrowsException() {
        when(ccdSearchService.searchAwaitingPronouncementCases(user, SERVICE_AUTHORIZATION))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        systemCreateBulkCaseListTask.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfBulkCaseCreationThrowsException() {
        ReflectionTestUtils.setField(systemCreateBulkCaseListTask, "minimumCasesToProcess", 1);

        final CaseDetails caseDetails1 = mock(CaseDetails.class);

        final CaseData caseData1 =
            caseDataWithApplicant("app1fname1", "app1lname1", "app2fname1", "app2lname1");

        final Map<String, Object> caseData1Map = new HashMap<>();
        caseData1Map.put("applicant1FirstName", "app1fname1");
        caseData1Map.put("applicant1LastName", "app1lname1");
        caseData1Map.put("applicant2FirstName", "app2fname1");
        caseData1Map.put("applicant2LastName", "app2lname1");

        when(caseDetails1.getData()).thenReturn(caseData1Map);
        when(caseDetails1.getId()).thenReturn(1L);

        when(mapper.convertValue(caseData1Map, CaseData.class)).thenReturn(caseData1);

        when(ccdSearchService.searchAwaitingPronouncementCases(user, SERVICE_AUTHORIZATION))
            .thenReturn(List.of(caseDetails1));

        var bulkListCaseDetails1 =
            bulkListCaseDetailsListValue("app1fname1 app1lname1 vs app2fname1 app2lname1", 1L);

        var bulkActionCaseDetails =
            CaseDetails
                .builder()
                .caseTypeId(BulkActionCaseTypeConfig.CASE_TYPE)
                .data(Map.of("bulkListCaseDetails", List.of(bulkListCaseDetails1)))
                .build();

        CaseDetails caseDetailsBulkCase = CaseDetails.builder().id(3L).build();

        doThrow(new CcdManagementException("some exception", mock(FeignException.class)))
            .when(ccdCreateService).createBulkCase(bulkActionCaseDetails, user, SERVICE_AUTHORIZATION);

        systemCreateBulkCaseListTask.run();

        verify(ccdSearchService).searchAwaitingPronouncementCases(user, SERVICE_AUTHORIZATION);
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
