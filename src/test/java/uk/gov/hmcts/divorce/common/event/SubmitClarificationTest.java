package uk.gov.hmcts.divorce.common.event;

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
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.ClarificationSubmittedNotification;
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.PostInformationToCourtNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ClarificationReason;
import uk.gov.hmcts.divorce.divorcecase.model.ClarificationResponse;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SubmitClarification.SUBMIT_CLARIFICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ClarificationSubmitted;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.document.model.DocumentType.MARRIAGE_CERTIFICATE;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.documentWithType;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.validApplicant1CaseData;

@ExtendWith(MockitoExtension.class)
class SubmitClarificationTest {

    @InjectMocks
    private SubmitClarification submitClarification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private PostInformationToCourtNotification postInformationToCourtNotification;

    @Mock
    private ClarificationSubmittedNotification clarificationSubmittedNotification;

    @Mock
    private Clock clock;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        submitClarification.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SUBMIT_CLARIFICATION);
    }

    @Test
    void sendNotificationIfCannotUploadDocuments() {

        setMockClock(clock);

        CaseData caseData = validApplicant1CaseData();
        caseData.getConditionalOrder().setCannotUploadClarificationDocuments(YES);

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(TEST_CASE_ID).build();

        submitClarification.aboutToSubmit(caseDetails, null);

        verify(notificationDispatcher).send(postInformationToCourtNotification, caseData, TEST_CASE_ID);
        verify(notificationDispatcher).send(clarificationSubmittedNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldNotSendNotificationIfCannotUploadDocumentsSetAsNo() {

        setMockClock(clock);

        CaseData caseData = validApplicant1CaseData();
        caseData.getConditionalOrder().setCannotUploadClarificationDocuments(NO);

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(TEST_CASE_ID).build();

        submitClarification.aboutToSubmit(caseDetails, null);

        verify(notificationDispatcher).send(clarificationSubmittedNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }


    @Test
    void shouldNotSendNotificationIfNoConditionalOrder() {

        setMockClock(clock);

        CaseData caseData = validApplicant1CaseData();

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(TEST_CASE_ID).build();

        submitClarification.aboutToSubmit(caseDetails, null);

        verify(notificationDispatcher).send(clarificationSubmittedNotification, caseData, TEST_CASE_ID);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldSetStateOnAboutToSubmit() {

        setMockClock(clock);

        CaseData caseData = validApplicant1CaseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(AwaitingClarification).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitClarification.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(ClarificationSubmitted);
    }

    @Test
    void shouldAddClarificationDocumentsToUploadedDocumentsList() {

        setMockClock(clock);

        final List<ListValue<DivorceDocument>> clarificationDocuments =
            List.of(documentWithType(CONDITIONAL_ORDER_REFUSAL), documentWithType(CONDITIONAL_ORDER_REFUSAL));
        CaseData caseData = validApplicant1CaseData();
        caseData.getConditionalOrder().setClarificationUploadDocuments(clarificationDocuments);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(AwaitingClarification).id(TEST_CASE_ID).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitClarification.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getDocuments().getDocumentsUploaded()).hasSize(2);
    }

    @Test
    void shouldCreateNewClarificationResponsesSubmittedListIfNotExist() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder
                    .builder()
                    .clarificationResponses(emptyList())
                    .cannotUploadClarificationDocuments(NO)
                    .clarificationUploadDocuments(List.of(documentWithType(MARRIAGE_CERTIFICATE)))
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            submitClarification.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getClarificationResponsesSubmitted()).hasSize(1);
    }

    @Test
    void shouldAddClarificationResponseSubmittedToTopOfListIfExistsAlready() {

        setMockClock(clock);

        final ListValue<ClarificationResponse> listValue =
            ListValue.<ClarificationResponse>builder()
                .value(ClarificationResponse.builder().build())
                .build();
        final List<ListValue<ClarificationResponse>> clarificationResponsesSubmitted = new ArrayList<>();
        clarificationResponsesSubmitted.add(listValue);

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder
                    .builder()
                    .clarificationResponses(emptyList())
                    .cannotUploadClarificationDocuments(NO)
                    .clarificationUploadDocuments(List.of(documentWithType(MARRIAGE_CERTIFICATE)))
                    .clarificationResponsesSubmitted(clarificationResponsesSubmitted)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            submitClarification.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getClarificationResponsesSubmitted()).hasSize(2);
    }

    @Test
    void shouldResetRefusalReasonFieldsUponClarificationSubmission() {
        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder
                    .builder()
                    .refusalDecision(MORE_INFO)
                    .refusalClarificationReason(Set.of(ClarificationReason.MARRIAGE_CERTIFICATE, ClarificationReason.OTHER))
                    .refusalClarificationAdditionalInfo("Test refusalClarificationAdditionalInfo")
                    .refusalAdminErrorInfo("Test refusalAdminErrorInfo")
                    .refusalRejectionAdditionalInfo("Rejected comments")
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            submitClarification.aboutToSubmit(caseDetails, null);

        ConditionalOrder actualConditionalOrder = response.getData().getConditionalOrder();
        assertThat(actualConditionalOrder.getRefusalDecision()).isNull();
        assertThat(actualConditionalOrder.getRefusalClarificationReason()).isNull();
        assertThat(actualConditionalOrder.getRefusalClarificationAdditionalInfo()).isNull();
        assertThat(actualConditionalOrder.getRefusalAdminErrorInfo()).isNull();
        assertThat(actualConditionalOrder.getRefusalRejectionAdditionalInfo()).isNull();
    }
}

