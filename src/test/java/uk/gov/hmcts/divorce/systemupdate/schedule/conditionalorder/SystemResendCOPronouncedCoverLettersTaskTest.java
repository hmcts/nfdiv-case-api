package uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import feign.FeignException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.ConfidentialDocumentsReceived;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdConflictException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdManagementException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchCaseException;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.util.List;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PRIVATE;
import static uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType.PUBLIC;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemResendCOPronouncedCoverLetter.SYSTEM_RESEND_CO_PRONOUNCED_COVER_LETTER;
import static uk.gov.hmcts.divorce.systemupdate.schedule.conditionalorder.SystemResendCOPronouncedCoverLettersTask.NOTIFICATION_FLAG;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICANT1_OFFLINE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICANT1_PRIVATE_CONTACT;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICANT2_OFFLINE;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.APPLICANT2_PRIVATE_CONTACT;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.DATA;
import static uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService.STATE;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SYSTEM_UPDATE_AUTH_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.buildCaseDataCOPronounced;

@ExtendWith(MockitoExtension.class)
class SystemResendCOPronouncedCoverLettersTaskTest {

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SystemResendCOPronouncedCoverLettersTask underTest;

    private User user;

    private static final BoolQueryBuilder query =
        boolQuery()
            .must(matchQuery(STATE, ConditionalOrderPronounced))
            .must(
                boolQuery()
                    .should(
                        boolQuery()
                            .must(matchQuery(String.format(DATA, APPLICANT1_OFFLINE), YES))
                            .must(matchQuery(String.format(DATA, APPLICANT1_PRIVATE_CONTACT), PRIVATE.getType()))
                    )
                    .should(
                        boolQuery()
                            .must(matchQuery(String.format(DATA, APPLICANT2_OFFLINE), YES))
                            .must(matchQuery(String.format(DATA, APPLICANT2_PRIVATE_CONTACT), PRIVATE.getType()))
                    )
                    .minimumShouldMatch(1)
            )
            .mustNot(matchQuery(String.format(DATA, NOTIFICATION_FLAG), YES));

    @BeforeEach
    void setUp() {
        user = new User(SYSTEM_UPDATE_AUTH_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveSystemUpdateUserDetails()).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldSubmitEventResendCOPronouncedCoverLetterWhenOfflineApplicant1ContactIsPrivateAndCoverLetterIsNotInConfidentialList() {
        final CaseDetails caseDetails1 = CaseDetails.builder().id(TEST_CASE_ID)
            .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPronounced))
            .thenReturn(caseDetailsList);

        when(objectMapper.convertValue(caseDetails1.getData(), CaseData.class))
            .thenReturn(buildCaseDataCOPronounced(YES, PRIVATE, PUBLIC));

        underTest.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_RESEND_CO_PRONOUNCED_COVER_LETTER, user, SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdUpdateService);
    }

    @Test
    void shouldSubmitEventResendCOPronouncedCoverLetterWhenOfflineApplicant2ContactIsPrivateAndCoverLetterIsNotInConfidentialList() {
        final CaseDetails caseDetails1 = CaseDetails.builder().id(TEST_CASE_ID)
            .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPronounced))
            .thenReturn(caseDetailsList);

        when(objectMapper.convertValue(caseDetails1.getData(), CaseData.class))
            .thenReturn(buildCaseDataCOPronounced(YES, PUBLIC, PRIVATE));

        underTest.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_RESEND_CO_PRONOUNCED_COVER_LETTER, user, SERVICE_AUTHORIZATION);
        verifyNoMoreInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventResendCOPronouncedCoverLetterWhenOfflineApplicantsContactIsPublic() {
        final CaseDetails caseDetails1 = CaseDetails.builder().id(TEST_CASE_ID)
            .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPronounced))
            .thenReturn(caseDetailsList);

        when(objectMapper.convertValue(caseDetails1.getData(), CaseData.class))
            .thenReturn(buildCaseDataCOPronounced(YES, PUBLIC, PUBLIC));

        underTest.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventResendCOPronouncedCoverLetterWhenOfflineApplicantsContactIsPrivateButCoverLettersAreAlreadyInConfidentialList(
    ) {
        final CaseDetails caseDetails1 = CaseDetails.builder().id(TEST_CASE_ID)
            .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPronounced))
            .thenReturn(caseDetailsList);

        ListValue<ConfidentialDivorceDocument> coCoverLetterApp1 = ListValue.<ConfidentialDivorceDocument>builder()
            .value(ConfidentialDivorceDocument.builder()
                .confidentialDocumentsReceived(ConfidentialDocumentsReceived.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1)
                .build())
            .build();

        ListValue<ConfidentialDivorceDocument> coCoverLetterApp2 = ListValue.<ConfidentialDivorceDocument>builder()
            .value(ConfidentialDivorceDocument.builder()
                .confidentialDocumentsReceived(ConfidentialDocumentsReceived.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2)
                .build())
            .build();

        CaseData caseData = buildCaseDataCOPronounced(YES, PRIVATE, PRIVATE);
        caseData.getDocuments().setConfidentialDocumentsGenerated(Lists.newArrayList(coCoverLetterApp1, coCoverLetterApp2));
        caseData.getDocuments().getDocumentsGenerated().removeIf(doc ->
            doc.getValue().getDocumentType().equals(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1)
                || doc.getValue().getDocumentType().equals(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2));

        when(objectMapper.convertValue(caseDetails1.getData(), CaseData.class))
            .thenReturn(caseData);

        underTest.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldNotSubmitEventIfSearchFails() {
        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPronounced))
            .thenThrow(new CcdSearchCaseException("Failed to search cases", mock(FeignException.class)));

        underTest.run();

        verifyNoInteractions(ccdUpdateService);
    }

    @Test
    void shouldStopProcessingIfThereIsConflictDuringSubmission() {
        final CaseDetails caseDetails1 = CaseDetails.builder().id(TEST_CASE_ID)
            .data(Map.of("applicant1Offline", "Yes"))
            .build();
        final CaseDetails caseDetails2 = CaseDetails.builder().id(2L)
            .data(Map.of("applicant1Offline", "No"))
            .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPronounced))
            .thenReturn(caseDetailsList);

        when(objectMapper.convertValue(caseDetails1.getData(), CaseData.class))
            .thenReturn(buildCaseDataCOPronounced(YES, PRIVATE, PUBLIC));

        doThrow(new CcdConflictException("Case is modified by another transaction", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_RESEND_CO_PRONOUNCED_COVER_LETTER, user, SERVICE_AUTHORIZATION);

        underTest.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_RESEND_CO_PRONOUNCED_COVER_LETTER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService, never())
            .submitEvent(2L, SYSTEM_RESEND_CO_PRONOUNCED_COVER_LETTER, user, SERVICE_AUTHORIZATION);
    }

    @Test
    void shouldContinueToNextCaseIfExceptionIsThrownWhileProcessingPreviousCase() {
        final CaseDetails caseDetails1 = CaseDetails.builder().id(TEST_CASE_ID)
            .data(Map.of("applicant1Offline", "Yes"))
            .build();
        final CaseDetails caseDetails2 = CaseDetails.builder().id(2L)
            .data(Map.of("applicant1Offline", "No"))
            .build();

        final List<CaseDetails> caseDetailsList = List.of(caseDetails1, caseDetails2);

        when(ccdSearchService.searchForAllCasesWithQuery(query, user, SERVICE_AUTHORIZATION, ConditionalOrderPronounced))
            .thenReturn(caseDetailsList);

        when(objectMapper.convertValue(caseDetails1.getData(), CaseData.class))
            .thenReturn(buildCaseDataCOPronounced(YES, PRIVATE, PUBLIC));
        when(objectMapper.convertValue(caseDetails2.getData(), CaseData.class))
            .thenReturn(buildCaseDataCOPronounced(YES, PUBLIC, PRIVATE));

        doThrow(new CcdManagementException(GATEWAY_TIMEOUT.value(), "Failed processing of case", mock(FeignException.class)))
            .when(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_RESEND_CO_PRONOUNCED_COVER_LETTER, user, SERVICE_AUTHORIZATION);

        underTest.run();

        verify(ccdUpdateService).submitEvent(TEST_CASE_ID, SYSTEM_RESEND_CO_PRONOUNCED_COVER_LETTER, user, SERVICE_AUTHORIZATION);
        verify(ccdUpdateService).submitEvent(2L, SYSTEM_RESEND_CO_PRONOUNCED_COVER_LETTER, user, SERVICE_AUTHORIZATION);

    }
}
