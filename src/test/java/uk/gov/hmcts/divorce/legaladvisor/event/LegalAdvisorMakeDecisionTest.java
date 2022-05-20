package uk.gov.hmcts.divorce.legaladvisor.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.ConfigBuilderImpl;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.Event;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.LegalAdvisorDecision;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ConditionalOrderRefusalContent;
import uk.gov.hmcts.divorce.legaladvisor.notification.LegalAdvisorMoreInfoDecisionNotification;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.ADMIN_ERROR;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.MORE_INFO;
import static uk.gov.hmcts.divorce.divorcecase.model.RefusalOption.REJECT;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAdminClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAmendedApplication;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingClarification;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingPronouncement;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REFUSAL_ORDER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.REFUSAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorMakeDecision.LEGAL_ADVISOR_MAKE_DECISION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class LegalAdvisorMakeDecisionTest {

    @Mock
    private LegalAdvisorMoreInfoDecisionNotification notification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private ConditionalOrderRefusalContent conditionalOrderRefusalContent;

    @Mock
    private Clock clock;

    @InjectMocks
    private LegalAdvisorMakeDecision legalAdvisorMakeDecision;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        legalAdvisorMakeDecision.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(LEGAL_ADVISOR_MAKE_DECISION);
    }

    @Test
    void shouldSetGrantedDateAndStateToAwaitingPronouncementIfConditionalOrderIsGranted() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder().granted(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getDecisionDate()).isEqualTo(getExpectedLocalDate());
        assertThat(response.getState()).isEqualTo(AwaitingPronouncement);
    }

    @Test
    void shouldSetStateToAwaitingClarificationIfConditionalOrderIsNotGrantedAndRefusalIsDueToMoreInformationRequired() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder().granted(NO).refusalDecision(MORE_INFO).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getDecisionDate()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingClarification);

    }

    @Test
    void shouldSetStateToAwaitingAdminClarificationIfConditionalOrderIsNotGrantedAndRefusalIsDueToAdminError() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder().granted(NO).refusalDecision(ADMIN_ERROR).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getDecisionDate()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingAdminClarification);
    }

    @Test
    void shouldSetStateToAwaitingAmendedApplicationIfConditionalOrderIsRejected() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder().granted(NO).refusalDecision(REJECT).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final Map<String, Object> templateContent = new HashMap<>();
        when(conditionalOrderRefusalContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        String documentUrl = "http://localhost:8080/4567";
        var refusalConditionalOrderDoc = new Document(
            documentUrl,
            REFUSAL_ORDER_DOCUMENT_NAME,
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                REFUSAL_ORDER_TEMPLATE_ID,
                ENGLISH,
                REFUSAL_ORDER_DOCUMENT_NAME
            ))
            .thenReturn(refusalConditionalOrderDoc);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, null);

        assertThat(response.getData().getConditionalOrder().getDecisionDate()).isNull();
        assertThat(response.getState()).isEqualTo(AwaitingAmendedApplication);
    }

    @Test
    void shouldSendEmailIfConditionalOrderIsRejectedForMoreInfoAndIsSolicitorApplication() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder().granted(NO).refusalDecision(MORE_INFO).build())
            .application(Application.builder().solSignStatementOfTruth(YES).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(12345L);

        legalAdvisorMakeDecision.aboutToSubmit(caseDetails, caseDetails);

        verify(notificationDispatcher).send(notification, caseData, 12345L);
    }

    @Test
    void shouldNotSendEmailIfConditionalOrderIsRejectedForMoreInfoAndIsNotSolicitorApplication() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(ConditionalOrder.builder().granted(NO).build())
            .application(Application.builder().solSignStatementOfTruth(NO).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final Map<String, Object> templateContent = new HashMap<>();
        when(conditionalOrderRefusalContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        String documentUrl = "http://localhost:8080/4567";
        var refusalConditionalOrderDoc = new Document(
            documentUrl,
            REFUSAL_ORDER_DOCUMENT_NAME,
            documentUrl + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                REFUSAL_ORDER_TEMPLATE_ID,
                ENGLISH,
                REFUSAL_ORDER_DOCUMENT_NAME
            ))
            .thenReturn(refusalConditionalOrderDoc);

        legalAdvisorMakeDecision.aboutToSubmit(caseDetails, caseDetails);

        verifyNoInteractions(notification);
    }

    @Test
    void shouldResetConditionalOrderRefusalFieldsWhenAboutToStartCallbackIsInvoked() {

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder
                    .builder()
                    .granted(NO)
                    .refusalDecision(MORE_INFO)
                    .refusalClarificationAdditionalInfo("some info")
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToStart(caseDetails);

        ConditionalOrder actualConditionalOrder = response.getData().getConditionalOrder();
        assertThat(actualConditionalOrder.getRefusalDecision()).isNull();
        assertThat(actualConditionalOrder.getRefusalClarificationAdditionalInfo()).isNull();
        assertThat(actualConditionalOrder.getGranted()).isNull();
    }

    @Test
    void shouldCreateNewClarificationResponsesSubmittedListIfNotExist() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder
                    .builder()
                    .granted(NO)
                    .refusalDecision(MORE_INFO)
                    .refusalClarificationAdditionalInfo("some info")
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getLegalAdvisorDecisions()).hasSize(1);
    }

    @Test
    void shouldAddClarificationResponseSubmittedToTopOfListIfExistsAlready() {

        setMockClock(clock);

        final ListValue<LegalAdvisorDecision> listValue =
            ListValue.<LegalAdvisorDecision>builder()
                .value(LegalAdvisorDecision.builder().build())
                .build();
        final List<ListValue<LegalAdvisorDecision>> legalAdvisorDecisions = new ArrayList<>();
        legalAdvisorDecisions.add(listValue);

        final CaseData caseData = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder
                    .builder()
                    .granted(NO)
                    .refusalDecision(MORE_INFO)
                    .refusalClarificationAdditionalInfo("some info")
                    .legalAdvisorDecisions(legalAdvisorDecisions)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            legalAdvisorMakeDecision.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData().getConditionalOrder().getLegalAdvisorDecisions()).hasSize(2);
    }
}
