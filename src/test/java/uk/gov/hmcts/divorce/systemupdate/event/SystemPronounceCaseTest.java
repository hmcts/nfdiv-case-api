package uk.gov.hmcts.divorce.systemupdate.event;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.notification.ConditionalOrderPronouncedNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrderCourt;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.notification.exception.NotificationTemplateException;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedCoversheet;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedDocument;
import uk.gov.hmcts.divorce.systemupdate.service.task.RemoveExistingConditionalOrderPronouncedDocument;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ConditionalOrderPronounced;
import static uk.gov.hmcts.divorce.divorcecase.model.State.SeparationOrderGranted;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.systemupdate.event.SystemPronounceCase.SYSTEM_PRONOUNCE_CASE;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(SpringExtension.class)
public class SystemPronounceCaseTest {

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ConditionalOrderPronouncedNotification notification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private GenerateConditionalOrderPronouncedDocument generateConditionalOrderPronouncedDocument;

    @Mock
    private GenerateConditionalOrderPronouncedCoversheet generateCoversheetDocument;

    @Mock
    private RemoveExistingConditionalOrderPronouncedDocument removeExistingConditionalOrderPronouncedDocument;

    @InjectMocks
    private SystemPronounceCase underTest;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        underTest.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(SYSTEM_PRONOUNCE_CASE);
    }

    @Test
    void shouldGenerateConditionalOrderGrantedDocAndSetStateToConditionalOrderPronounced() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .id(1L)
            .data(caseData)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = underTest.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(ConditionalOrderPronounced);

        verify(generateConditionalOrderPronouncedDocument).apply(details);
        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldGenerateConditionalOrderGrantedDocAndSetStateToSeparationOrderGranted() {
        final CaseData caseData = caseData();
        caseData.setIsJudicialSeparation(YES);

        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .id(1L)
            .data(caseData)
            .build();

        final AboutToStartOrSubmitResponse<CaseData, State> response = underTest.aboutToSubmit(details, details);

        assertThat(response.getState()).isEqualTo(SeparationOrderGranted);

        verify(generateConditionalOrderPronouncedDocument).apply(details);
        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldSendNotification() {
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .id(1L)
            .data(caseData)
            .build();

        underTest.submitted(details, details);

        verify(notificationDispatcher).send(notification, caseData, details.getId());
    }

    @Test
    void shouldNotSendNotificationAndLogErrorIfNotificationTemplateExceptionIsThrown() {

        final NotificationTemplateException notificationTemplateException = new NotificationTemplateException("Message");
        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .id(1L)
            .data(caseData)
            .build();
        doThrow(notificationTemplateException)
            .when(notificationDispatcher)
            .send(notification, caseData, details.getId());

        underTest.submitted(details, details);

    }

    @Test
    public void shouldSkipDocGenerationWhenOnlineCoDocumentAlreadyExistsAndNoChangesToConditionalOrder() {
        final CaseData caseData = caseData();

        setConditionalOrder(caseData);

        final CaseDetails<CaseData, State> details = CaseDetails.<CaseData, State>builder()
            .id(1L)
            .data(caseData)
            .build();

        underTest.aboutToSubmit(details, details);

        verifyNoMoreInteractions(generateConditionalOrderPronouncedDocument);
        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    public void shouldRegenerateCoDocWhenDocumentAlreadyExistsAndChangesToConditionalOrder() {
        final CaseData caseDataOld = caseData();
        setConditionalOrder(caseDataOld);

        final CaseData caseDataNew = caseData();
        setConditionalOrder(caseDataNew);
        caseDataNew.getConditionalOrder().setPronouncementJudge("NewJudge");

        final CaseDetails<CaseData, State> detailsOld = CaseDetails.<CaseData, State>builder()
            .id(1L)
            .data(caseDataOld)
            .build();

        final CaseDetails<CaseData, State> detailsNew = CaseDetails.<CaseData, State>builder()
            .id(1L)
            .data(caseDataNew)
            .build();

        when(removeExistingConditionalOrderPronouncedDocument.apply(detailsNew)).thenReturn(detailsNew);
        when(generateConditionalOrderPronouncedDocument.apply(detailsNew)).thenReturn(detailsNew);

        underTest.aboutToSubmit(detailsNew, detailsOld);

        verify(removeExistingConditionalOrderPronouncedDocument).apply(detailsNew);
        verify(generateConditionalOrderPronouncedDocument).apply(detailsNew);
        verifyNoInteractions(notificationDispatcher);
    }

    private void setConditionalOrder(final CaseData caseData) {
        ListValue<DivorceDocument> coDocumentListValue = ListValue
            .<DivorceDocument>builder()
            .id(CONDITIONAL_ORDER_GRANTED.getLabel())
            .value(DivorceDocument.builder()
                .documentType(CONDITIONAL_ORDER_GRANTED)
                .build())
            .build();

        List<ListValue<DivorceDocument>> docs = List.of(coDocumentListValue);

        caseData.getDocuments().setDocumentsGenerated(docs);

        caseData.setConditionalOrder(ConditionalOrder.builder()
            .pronouncementJudge("judgeName")
            .court(ConditionalOrderCourt.BIRMINGHAM)
            .dateAndTimeOfHearing(LocalDateTime.now())
            .build());
    }
}
