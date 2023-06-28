package uk.gov.hmcts.divorce.common.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.service.task.SetLatestBailiffApplicationStatus;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderQuestions;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.solicitor.service.CcdAccessService;
import uk.gov.hmcts.divorce.solicitor.service.task.AddLastAlternativeServiceDocumentLink;
import uk.gov.hmcts.divorce.solicitor.service.task.AddMiniApplicationLink;
import uk.gov.hmcts.divorce.solicitor.service.task.AddOfflineRespondentAnswersLink;
import uk.gov.hmcts.divorce.solicitor.service.task.ProgressDraftConditionalOrderState;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.DraftConditionalOrder.DRAFT_CONDITIONAL_ORDER;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_SERVICE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.LOCAL_DATE_TIME;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;

@ExtendWith(MockitoExtension.class)
class DraftConditionalOrderTest {

    private static final String USER_TOKEN = "user";

    @Mock
    private AddMiniApplicationLink addMiniApplicationLink;

    @Mock
    private AddLastAlternativeServiceDocumentLink addLastAlternativeServiceDocumentLink;

    @Mock
    private ProgressDraftConditionalOrderState progressDraftConditionalOrderState;

    @Mock
    private SetLatestBailiffApplicationStatus setLatestBailiffApplicationStatus;

    @Mock
    private AddOfflineRespondentAnswersLink addOfflineRespondentAnswersLink;

    @Mock
    private CcdAccessService ccdAccessService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private DraftConditionalOrder draftConditionalOrder;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        draftConditionalOrder.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(DRAFT_CONDITIONAL_ORDER);
    }

    @Test
    void shouldSetIsDraftedForApplicant1OnAboutToSubmit() {

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YES)
                    .build())
                .build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).id(TEST_CASE_ID).build();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(USER_TOKEN);
        when(ccdAccessService.isApplicant2(USER_TOKEN, TEST_CASE_ID)).thenReturn(false);
        when(progressDraftConditionalOrderState.apply(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted())
            .isEqualTo(YES);
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsDrafted())
            .isNull();
    }

    @Test
    void shouldSetIsDraftedForApplicant2OnAboutToSubmit() {

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YES)
                    .build())
                .build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).id(TEST_CASE_ID).build();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(USER_TOKEN);
        when(ccdAccessService.isApplicant2(USER_TOKEN, TEST_CASE_ID)).thenReturn(true);
        when(progressDraftConditionalOrderState.apply(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getIsDrafted())
            .isNull();
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getIsDrafted())
            .isEqualTo(YES);
    }

    @Test
    void shouldSetApplyForConditionalOrderOnJointIfNoSelectedByApplicant1() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YES)
                    .applyForConditionalOrder(NO)
                    .applyForConditionalOrderIfNo(YES)
                    .build())
                .build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).id(TEST_CASE_ID).build();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(USER_TOKEN);
        when(ccdAccessService.isApplicant2(USER_TOKEN, TEST_CASE_ID)).thenReturn(false);
        when(progressDraftConditionalOrderState.apply(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getApplyForConditionalOrder())
            .isEqualTo(YES);
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant1Questions().getApplyForConditionalOrderIfNo())
            .isNull();
    }

    @Test
    void shouldSetApplyForConditionalOrderOnJointIfNoSelectedByApplicant2() {
        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant2Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YES)
                    .applyForConditionalOrder(NO)
                    .applyForConditionalOrderIfNo(YES)
                    .build())
                .build())
            .build();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).id(TEST_CASE_ID).build();

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(USER_TOKEN);
        when(ccdAccessService.isApplicant2(USER_TOKEN, TEST_CASE_ID)).thenReturn(true);
        when(progressDraftConditionalOrderState.apply(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getApplyForConditionalOrder())
            .isEqualTo(YES);
        assertThat(response.getData().getConditionalOrder().getConditionalOrderApplicant2Questions().getApplyForConditionalOrderIfNo())
            .isNull();
    }

    @Test
    void shouldCallProgressDraftConditionalOrderStateOnAboutToSubmit() {

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .conditionalOrder(ConditionalOrder.builder()
                .conditionalOrderApplicant1Questions(ConditionalOrderQuestions.builder()
                    .statementOfTruth(YES)
                    .build())
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(USER_TOKEN);
        when(ccdAccessService.isApplicant2(USER_TOKEN, TEST_CASE_ID)).thenReturn(false);
        when(progressDraftConditionalOrderState.apply(caseDetails)).thenReturn(caseDetails);

        draftConditionalOrder.aboutToSubmit(caseDetails, caseDetails);

        verify(progressDraftConditionalOrderState).apply(caseDetails);
    }

    @Test
    void shouldCallAddMiniApplicationAndReturnCaseDataOnAboutToStart() {

        final CaseData expectedCaseData = CaseData.builder().build();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        final CaseDetails<CaseData, State> updateCaseDetails = new CaseDetails<>();
        updateCaseDetails.setData(expectedCaseData);

        when(addMiniApplicationLink.apply(caseDetails)).thenReturn(caseDetails);
        when(addLastAlternativeServiceDocumentLink.apply(caseDetails)).thenReturn(caseDetails);
        when(setLatestBailiffApplicationStatus.apply(caseDetails)).thenReturn(caseDetails);
        when(addOfflineRespondentAnswersLink.apply(caseDetails)).thenReturn(updateCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response = draftConditionalOrder.aboutToStart(caseDetails);

        assertThat(response.getData()).isSameAs(expectedCaseData);

        verify(addMiniApplicationLink).apply(caseDetails);
        verify(addLastAlternativeServiceDocumentLink).apply(caseDetails);
        verify(setLatestBailiffApplicationStatus).apply(caseDetails);
        verify(addOfflineRespondentAnswersLink).apply(caseDetails);
    }

    @Test
    void shouldReturnProofOfServiceUploadDocumentsInDescendingOrderWhenNewDocumentsAreAdded() {

        final ListValue<DivorceDocument> doc1 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
            "certificateOfCService.pdf",
            CERTIFICATE_OF_SERVICE);

        final ListValue<DivorceDocument> doc2 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130004",
            "certificateOfCService2.pdf",
            CERTIFICATE_OF_SERVICE);

        final var previousCaseData = caseData();
        previousCaseData.getConditionalOrder().setProofOfServiceUploadDocuments(singletonList(doc1));

        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        previousCaseDetails.setData(previousCaseData);
        previousCaseDetails.setId(TEST_CASE_ID);
        previousCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final var newCaseData = caseData();
        newCaseData.setApplicationType(SOLE_APPLICATION);
        newCaseData.getConditionalOrder().setProofOfServiceUploadDocuments(List.of(doc1, doc2));

        final CaseDetails<CaseData, State> newCaseDetails = new CaseDetails<>();
        newCaseDetails.setData(newCaseData);
        newCaseDetails.setId(TEST_CASE_ID);
        newCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(progressDraftConditionalOrderState.apply(newCaseDetails)).thenReturn(newCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            draftConditionalOrder.aboutToSubmit(newCaseDetails, previousCaseDetails);

        final List<ListValue<DivorceDocument>> proofOfServiceUploadDocuments = response
            .getData()
            .getConditionalOrder()
            .getProofOfServiceUploadDocuments();

        assertThat(proofOfServiceUploadDocuments).hasSize(2);
        assertThat(proofOfServiceUploadDocuments.get(0).getValue()).isSameAs(doc2.getValue());
        assertThat(proofOfServiceUploadDocuments.get(1).getValue()).isSameAs(doc1.getValue());
    }

    @Test
    void shouldSkipSortingProofOfServiceUploadDocumentsWhenNoNewDocumentsAreAdded() {

        final ListValue<DivorceDocument> doc1 = getDivorceDocumentListValue(
            "http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
            "certificateOfCService.pdf",
            CERTIFICATE_OF_SERVICE);

        final var previousCaseData = caseData();
        previousCaseData.getConditionalOrder().setProofOfServiceUploadDocuments(singletonList(doc1));

        final CaseDetails<CaseData, State> previousCaseDetails = new CaseDetails<>();
        previousCaseDetails.setData(previousCaseData);
        previousCaseDetails.setId(TEST_CASE_ID);
        previousCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        final var newCaseData = caseData();
        newCaseData.setApplicationType(SOLE_APPLICATION);
        newCaseData.getConditionalOrder().setProofOfServiceUploadDocuments(singletonList(doc1));

        final CaseDetails<CaseData, State> newCaseDetails = new CaseDetails<>();
        newCaseDetails.setData(newCaseData);
        newCaseDetails.setId(TEST_CASE_ID);
        newCaseDetails.setCreatedDate(LOCAL_DATE_TIME);

        when(progressDraftConditionalOrderState.apply(newCaseDetails)).thenReturn(newCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            draftConditionalOrder.aboutToSubmit(newCaseDetails, previousCaseDetails);

        final List<ListValue<DivorceDocument>> proofOfServiceUploadDocuments = response
            .getData()
            .getConditionalOrder()
            .getProofOfServiceUploadDocuments();

        assertThat(proofOfServiceUploadDocuments).hasSize(1);
        assertThat(proofOfServiceUploadDocuments.get(0).getValue()).isSameAs(doc1.getValue());
    }
}
