package uk.gov.hmcts.divorce.caseworker.event;

import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.CaseLink;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.idam.IdamService;
import uk.gov.hmcts.divorce.idam.User;
import uk.gov.hmcts.divorce.systemupdate.service.CcdSearchService;
import uk.gov.hmcts.divorce.systemupdate.service.CcdUpdateService;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.bulkaction.ccd.event.CaseworkerRemoveCasesFromBulkList.CASEWORKER_REMOVE_CASES_BULK_LIST;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRescindConditionalOrder.RESCIND_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.divorcecase.model.State.GeneralConsiderationComplete;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.SERVICE_AUTHORIZATION;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_AUTHORIZATION_TOKEN;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
public class CaseworkerRescindConditionalOrderTest {

    private static final String BULK_CASE_REFERENCE = "1234123412341234";

    @Mock
    private Clock clock;

    @Mock
    private CcdUpdateService ccdUpdateService;

    @Mock
    private CcdSearchService ccdSearchService;

    @Mock
    private IdamService idamService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private CaseworkerRescindConditionalOrder caseworkerRescindConditionalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() throws Exception {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRescindConditionalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(RESCIND_CONDITIONAL_ORDER);
    }

    @Test
    void shouldUnlinkFromBulkCaseIfStateIsAwaitingPronouncement() {

        setMockClock(clock);

        final var caseData = caseData();

        final List<ListValue<DivorceDocument>> documentsGenerated = new ArrayList<>();
        final ListValue<DivorceDocument> coGrantedDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED)
                .build())
            .build();
        documentsGenerated.add(coGrantedDoc);

        caseData.setBulkListCaseReferenceLink(CaseLink.builder().caseReference(BULK_CASE_REFERENCE).build());
        caseData.setConditionalOrder(
            ConditionalOrder.builder()
                .conditionalOrderGrantedDocument(coGrantedDoc.getValue())
                .build()
        );
        caseData.setDocuments(
            CaseDocuments.builder()
                .documentsGenerated(documentsGenerated)
                .build()
        );

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingPronouncement);

        final User user = new User(TEST_AUTHORIZATION_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = buildBulkCaseData();
        when(ccdSearchService.searchForBulkCaseById(
            caseData.getBulkListCaseReferenceLink().getCaseReference(), user, SERVICE_AUTHORIZATION))
            .thenReturn(bulkCaseDetails);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRescindConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getDocuments().getDocumentsGenerated().stream()
            .anyMatch(doc -> CONDITIONAL_ORDER_GRANTED.equals(doc.getValue().getDocumentType())))
            .isFalse();
        assertThat(response.getData().getBulkListCaseReferenceLink()).isNull();
        verify(ccdUpdateService)
            .submitBulkActionEvent(
                Long.parseLong(BULK_CASE_REFERENCE),
                CASEWORKER_REMOVE_CASES_BULK_LIST,
                user,
                SERVICE_AUTHORIZATION
            );
    }

    @Test
    void shouldUnlinkFromBulkCaseIfStateIsAwaitingPronouncementForOfflineCase() {

        setMockClock(clock);

        final var caseData = caseData();
        caseData.getApplicant1().setOffline(YES);
        caseData.getApplicant2().setOffline(YES);

        final ListValue<DivorceDocument> coGrantedDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED)
                .build())
            .build();
        final ListValue<DivorceDocument> coGrantedCoverLetterApp1 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1)
                .build())
            .build();
        final ListValue<DivorceDocument> coGrantedCoverLetterApp2 = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2)
                .build())
            .build();

        caseData.setBulkListCaseReferenceLink(CaseLink.builder().caseReference(BULK_CASE_REFERENCE).build());
        caseData.setConditionalOrder(
            ConditionalOrder.builder()
                .conditionalOrderGrantedDocument(coGrantedDoc.getValue())
                .build()
        );
        caseData.setDocuments(
            CaseDocuments.builder()
                .documentsGenerated(Lists.newArrayList(coGrantedDoc, coGrantedCoverLetterApp1, coGrantedCoverLetterApp2))
                .build()
        );

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);
        caseDetails.setState(AwaitingPronouncement);

        final User user = new User(TEST_AUTHORIZATION_TOKEN, UserInfo.builder().build());
        when(idamService.retrieveUser(any())).thenReturn(user);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTHORIZATION);

        final CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = buildBulkCaseData();
        when(ccdSearchService.searchForBulkCaseById(BULK_CASE_REFERENCE, user, SERVICE_AUTHORIZATION))
            .thenReturn(bulkCaseDetails);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRescindConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getDocuments().getDocumentsGenerated().stream()
            .anyMatch(doc ->
                CONDITIONAL_ORDER_GRANTED.equals(doc.getValue().getDocumentType())
                || CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_1.equals(doc.getValue().getDocumentType())
                || CONDITIONAL_ORDER_GRANTED_COVERSHEET_APP_2.equals(doc.getValue().getDocumentType())
            ))
            .isFalse();

        assertThat(response.getData().getBulkListCaseReferenceLink()).isNull();
        verify(ccdUpdateService)
            .submitBulkActionEvent(
                Long.parseLong(BULK_CASE_REFERENCE),
                CASEWORKER_REMOVE_CASES_BULK_LIST,
                user,
                SERVICE_AUTHORIZATION
            );
    }

    @Test
    void shouldRemoveConditionalOrderDocumentsFromCaseDataAndSetRescindedDateAndTime() {

        setMockClock(clock);

        final var caseData = caseData();

        final List<ListValue<DivorceDocument>> documentsGenerated = new ArrayList<>();
        final ListValue<DivorceDocument> coGrantedDoc = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED)
                .build())
            .build();
        documentsGenerated.add(coGrantedDoc);

        caseData.setConditionalOrder(
            ConditionalOrder.builder()
                .conditionalOrderGrantedDocument(coGrantedDoc.getValue())
                .build()
        );
        caseData.setDocuments(
            CaseDocuments.builder()
                .documentsGenerated(documentsGenerated)
                .build()
        );

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setState(GeneralConsiderationComplete);

        AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRescindConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getRescindedDate()).isEqualTo(getExpectedLocalDate());
        assertThat(response.getData().getConditionalOrder().getConditionalOrderGrantedDocument()).isNull();
        assertThat(response.getData().getDocuments().getDocumentsGenerated()).isEmpty();

        verifyNoInteractions(ccdUpdateService);
    }

    private CaseDetails<BulkActionCaseData, BulkActionState> buildBulkCaseData() {

        BulkActionCaseData caseData = BulkActionCaseData.builder()
            .casesAcceptedToListForHearing(Lists.newArrayList(ListValue.<CaseLink>builder()
                    .value(CaseLink.builder()
                        .caseReference(TEST_CASE_ID.toString())
                        .build())
                .build()))
            .build();

        CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails = new CaseDetails<>();
        bulkCaseDetails.setId(Long.parseLong(BULK_CASE_REFERENCE));
        bulkCaseDetails.setData(caseData);
        bulkCaseDetails.setState(BulkActionState.Listed);

        return bulkCaseDetails;
    }
}
