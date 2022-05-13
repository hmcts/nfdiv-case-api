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
import uk.gov.hmcts.divorce.citizen.notification.conditionalorder.PostInformationToCourtNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.common.event.SubmitClarification.SUBMIT_CLARIFICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ClarificationSubmitted;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_REFUSAL;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
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
        CaseData caseData = validApplicant1CaseData();
        caseData.getConditionalOrder().setCannotUploadClarificationDocuments(YES);

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(1L).build();

        submitClarification.aboutToSubmit(caseDetails, null);

        verify(notificationDispatcher).send(postInformationToCourtNotification, caseData, 1L);
        verifyNoMoreInteractions(notificationDispatcher);
    }

    @Test
    void shouldNotSendNotificationIfCannotUploadDocumentsSetAsNo() {
        CaseData caseData = validApplicant1CaseData();
        caseData.getConditionalOrder().setCannotUploadClarificationDocuments(NO);

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(1L).build();

        submitClarification.aboutToSubmit(caseDetails, null);

        verifyNoInteractions(notificationDispatcher);
    }


    @Test
    void shouldNotSendNotificationIfNoConditionalOrder() {
        CaseData caseData = validApplicant1CaseData();

        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder().data(caseData).id(1L).build();

        submitClarification.aboutToSubmit(caseDetails, null);

        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldSetStateOnAboutToSubmit() {
        CaseData caseData = validApplicant1CaseData();
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(AwaitingClarification).id(1L).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitClarification.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(ClarificationSubmitted);
    }

    @Test
    void shouldAddClarificationDocumentsToUploadedDocumentsList() {
        final List<ListValue<DivorceDocument>> clarificationDocuments =
            List.of(documentWithType(CONDITIONAL_ORDER_REFUSAL), documentWithType(CONDITIONAL_ORDER_REFUSAL));
        CaseData caseData = validApplicant1CaseData();
        caseData.getConditionalOrder().setClarificationUploadDocuments(clarificationDocuments);
        final CaseDetails<CaseData, State> caseDetails = CaseDetails.<CaseData, State>builder()
            .data(caseData).state(AwaitingClarification).id(1L).build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = submitClarification.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getDocuments().getDocumentsUploaded()).hasSize(2);
    }
}
