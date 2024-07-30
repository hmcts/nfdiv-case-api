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
import uk.gov.hmcts.divorce.common.notification.ServiceApplicationNotification;
import uk.gov.hmcts.divorce.common.service.HoldingPeriodService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceOutcome;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.ServiceOrderTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.JOINT_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.ApplicationType.SOLE_APPLICATION;
import static uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.divorce.divorcecase.model.ServiceApplicationRefusalReason.ADMIN_REFUSAL;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingAos;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingJsNullity;
import static uk.gov.hmcts.divorce.divorcecase.model.State.AwaitingServiceConsideration;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Holding;
import static uk.gov.hmcts.divorce.divorcecase.model.State.ServiceAdminRefusal;
import static uk.gov.hmcts.divorce.divorcecase.model.State.Submitted;
import static uk.gov.hmcts.divorce.divorcecase.model.SupplementaryCaseType.JUDICIAL_SEPARATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DEEMED_AS_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DEEMED_SERVICE_REFUSED_FILE_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DISPENSED_AS_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DISPENSED_WITH_SERVICE_REFUSED_FILE_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SERVICE_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.SERVICE_REFUSAL_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DISPENSE_WITH_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.legaladvisor.event.LegalAdvisorMakeServiceDecision.LEGAL_ADVISOR_SERVICE_DECISION;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.getExpectedLocalDate;
import static uk.gov.hmcts.divorce.testutil.ClockTestUtil.setMockClock;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class LegalAdvisorMakeServiceDecisionTest {

    @Mock
    private Clock clock;

    @Mock
    private CaseDataDocumentService caseDataDocumentService;

    @Mock
    private ServiceOrderTemplateContent serviceOrderTemplateContent;

    @Mock
    private ServiceApplicationNotification serviceApplicationNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private HoldingPeriodService holdingPeriodService;

    @InjectMocks
    private LegalAdvisorMakeServiceDecision makeServiceDecision;

    private static final String DOCUMENT_URL = "http://localhost:8080/4567";

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        makeServiceDecision.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(LEGAL_ADVISOR_SERVICE_DECISION);
    }

    @Test
    void shouldUpdateStateToHoldingAndSetDecisionDateAndGenerateOrderToDispenseDocIfApplicationIsGrantedAndTypeIsDispensed() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .alternativeService(
                AlternativeService
                    .builder()
                    .serviceApplicationGranted(YES)
                    .alternativeServiceType(DISPENSED)
                    .build()
            )
            .build();

        LocalDate issueDate = LocalDate.now();
        caseData.getApplication().setIssueDate(issueDate);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();
        when(serviceOrderTemplateContent.apply(any(CaseData.class), eq(TEST_CASE_ID))).thenReturn(templateContent);

        var orderToDispensedDoc = new Document(
            DOCUMENT_URL,
            DISPENSED_AS_SERVICE_GRANTED,
            DOCUMENT_URL + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                SERVICE_ORDER_TEMPLATE_ID,
                ENGLISH,
                DISPENSED_AS_SERVICE_GRANTED
            ))
            .thenReturn(orderToDispensedDoc);

        when(holdingPeriodService.getDueDateFor(issueDate)).thenReturn(issueDate.plusDays(141));

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeServiceDecision.aboutToSubmit(caseDetails, caseDetails);

        ListValue<AlternativeServiceOutcome> listValue = response.getData().getAlternativeServiceOutcomes().get(0);
        assertThat(listValue.getValue().getServiceApplicationDecisionDate())
            .isEqualTo(getExpectedLocalDate());

        assertThat(response.getState()).isEqualTo(Holding);
        assertThat(response.getData().getDueDate()).isEqualTo(issueDate.plusDays(141));

        var deemedOrDispensedDoc = DivorceDocument
            .builder()
            .documentLink(orderToDispensedDoc)
            .documentFileName(orderToDispensedDoc.getFilename())
            .documentType(DISPENSE_WITH_SERVICE_GRANTED)
            .build();

        assertThat(response.getData().getDocuments().getDocumentsGenerated())
            .extracting("value")
            .containsExactly(deemedOrDispensedDoc);

        verify(notificationDispatcher).send(serviceApplicationNotification, response.getData(), caseDetails.getId());
    }

    @Test
    void shouldUpdateStateToAwaitingJsNullityAndGenerateOrderToDispenseDocIfJSApplicationIsGrantedAndTypeIsDispensed() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .alternativeService(
                AlternativeService
                    .builder()
                    .serviceApplicationGranted(YES)
                    .alternativeServiceType(DISPENSED)
                    .build()
            )
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .build();

        LocalDate issueDate = LocalDate.now();
        caseData.getApplication().setIssueDate(issueDate);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();
        when(serviceOrderTemplateContent.apply(any(CaseData.class), eq(TEST_CASE_ID))).thenReturn(templateContent);

        var orderToDispensedDoc = new Document(
            DOCUMENT_URL,
            DISPENSED_AS_SERVICE_GRANTED,
            DOCUMENT_URL + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                SERVICE_ORDER_TEMPLATE_ID,
                ENGLISH,
                DISPENSED_AS_SERVICE_GRANTED
            ))
            .thenReturn(orderToDispensedDoc);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeServiceDecision.aboutToSubmit(caseDetails, caseDetails);

        ListValue<AlternativeServiceOutcome> listValue = response.getData().getAlternativeServiceOutcomes().get(0);
        assertThat(listValue.getValue().getServiceApplicationDecisionDate())
            .isEqualTo(getExpectedLocalDate());

        assertThat(response.getState()).isEqualTo(AwaitingJsNullity);

        var deemedOrDispensedDoc = DivorceDocument
            .builder()
            .documentLink(orderToDispensedDoc)
            .documentFileName(orderToDispensedDoc.getFilename())
            .documentType(DISPENSE_WITH_SERVICE_GRANTED)
            .build();

        assertThat(response.getData().getDocuments().getDocumentsGenerated())
            .extracting("value")
            .containsExactly(deemedOrDispensedDoc);

        verify(notificationDispatcher).send(serviceApplicationNotification, response.getData(), caseDetails.getId());
    }

    @Test
    void shouldUpdateStateToHoldingAndSetDecisionDateAndGenerateDeemedServiceOrderDocIfApplicationIsGrantedAndTypeIsDeemed() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .alternativeService(
                AlternativeService
                    .builder()
                    .deemedServiceDate(LocalDate.now(clock))
                    .serviceApplicationGranted(YES)
                    .alternativeServiceType(DEEMED)
                    .build()
            )
            .build();

        LocalDate issueDate = LocalDate.now();
        caseData.getApplication().setIssueDate(issueDate);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();
        when(serviceOrderTemplateContent.apply(any(CaseData.class), eq(TEST_CASE_ID))).thenReturn(templateContent);

        var orderToDispensedDoc = new Document(
            DOCUMENT_URL,
            DEEMED_AS_SERVICE_GRANTED,
            DOCUMENT_URL + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                SERVICE_ORDER_TEMPLATE_ID,
                ENGLISH,
                DEEMED_AS_SERVICE_GRANTED
            ))
            .thenReturn(orderToDispensedDoc);

        when(holdingPeriodService.getDueDateFor(issueDate)).thenReturn(issueDate.plusDays(141));

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeServiceDecision.aboutToSubmit(caseDetails, caseDetails);

        ListValue<AlternativeServiceOutcome> listValue = response.getData().getAlternativeServiceOutcomes().get(0);
        assertThat(listValue.getValue().getServiceApplicationDecisionDate())
            .isEqualTo(getExpectedLocalDate());

        assertThat(response.getState()).isEqualTo(Holding);
        assertThat(response.getData().getDueDate()).isEqualTo(issueDate.plusDays(141));

        var deemedOrDispensedDoc = DivorceDocument
            .builder()
            .documentLink(orderToDispensedDoc)
            .documentFileName(orderToDispensedDoc.getFilename())
            .documentType(DocumentType.DEEMED_AS_SERVICE_GRANTED)
            .build();

        assertThat(response.getData().getDocuments().getDocumentsGenerated())
            .extracting("value")
            .containsExactly(deemedOrDispensedDoc);

        verify(notificationDispatcher).send(serviceApplicationNotification, response.getData(), caseDetails.getId());
    }

    @Test
    void shouldUpdateStateToAwaitingJsNullityAndGenerateDeemedServiceOrderDocIfJSApplicationIsGrantedAndTypeIsDeemed() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .applicationType(SOLE_APPLICATION)
            .alternativeService(
                AlternativeService
                    .builder()
                    .deemedServiceDate(LocalDate.now(clock))
                    .serviceApplicationGranted(YES)
                    .alternativeServiceType(DEEMED)
                    .build()
            )
            .supplementaryCaseType(JUDICIAL_SEPARATION)
            .build();

        LocalDate issueDate = LocalDate.now();
        caseData.getApplication().setIssueDate(issueDate);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();
        when(serviceOrderTemplateContent.apply(any(CaseData.class), eq(TEST_CASE_ID))).thenReturn(templateContent);

        var orderToDispensedDoc = new Document(
            DOCUMENT_URL,
            DEEMED_AS_SERVICE_GRANTED,
            DOCUMENT_URL + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                SERVICE_ORDER_TEMPLATE_ID,
                ENGLISH,
                DEEMED_AS_SERVICE_GRANTED
            ))
            .thenReturn(orderToDispensedDoc);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeServiceDecision.aboutToSubmit(caseDetails, caseDetails);

        ListValue<AlternativeServiceOutcome> listValue = response.getData().getAlternativeServiceOutcomes().get(0);
        assertThat(listValue.getValue().getServiceApplicationDecisionDate())
            .isEqualTo(getExpectedLocalDate());

        assertThat(response.getState()).isEqualTo(AwaitingJsNullity);

        var deemedOrDispensedDoc = DivorceDocument
            .builder()
            .documentLink(orderToDispensedDoc)
            .documentFileName(orderToDispensedDoc.getFilename())
            .documentType(DocumentType.DEEMED_AS_SERVICE_GRANTED)
            .build();

        assertThat(response.getData().getDocuments().getDocumentsGenerated())
            .extracting("value")
            .containsExactly(deemedOrDispensedDoc);

        verify(notificationDispatcher).send(serviceApplicationNotification, response.getData(), caseDetails.getId());
    }

    @Test
    void shouldNotSetDueDateIfJointApplication() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .applicationType(JOINT_APPLICATION)
            .alternativeService(
                AlternativeService
                    .builder()
                    .serviceApplicationGranted(YES)
                    .alternativeServiceType(DISPENSED)
                    .build()
            )
            .build();

        LocalDate issueDate = LocalDate.now();
        caseData.getApplication().setIssueDate(issueDate);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();
        when(serviceOrderTemplateContent.apply(any(CaseData.class), eq(TEST_CASE_ID))).thenReturn(templateContent);

        var orderToDispensedDoc = new Document(
            DOCUMENT_URL,
            DISPENSED_AS_SERVICE_GRANTED,
            DOCUMENT_URL + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                SERVICE_ORDER_TEMPLATE_ID,
                ENGLISH,
                DISPENSED_AS_SERVICE_GRANTED
            ))
            .thenReturn(orderToDispensedDoc);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeServiceDecision.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(Holding);

        verifyNoInteractions(holdingPeriodService);

        assertThat(response.getData().getDueDate()).isNull();
    }

    @Test
    void shouldUpdateStateToSubmittedIfApplicationIsSuccessfulButTheCaseHasNotBeenIssued() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .deemedServiceDate(LocalDate.now(clock))
                    .serviceApplicationGranted(YES)
                    .alternativeServiceType(DEEMED)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();
        when(serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        var orderToDispensedDoc = new Document(
            DOCUMENT_URL,
            DEEMED_AS_SERVICE_GRANTED,
            DOCUMENT_URL + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                SERVICE_ORDER_TEMPLATE_ID,
                ENGLISH,
                DEEMED_AS_SERVICE_GRANTED
            ))
            .thenReturn(orderToDispensedDoc);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeServiceDecision.aboutToSubmit(caseDetails, caseDetails);

        ListValue<AlternativeServiceOutcome> listValue = response.getData().getAlternativeServiceOutcomes().get(0);
        assertThat(listValue.getValue().getServiceApplicationDecisionDate())
            .isEqualTo(getExpectedLocalDate());

        assertThat(response.getState()).isEqualTo(Submitted);

        var deemedOrDispensedDoc = DivorceDocument
            .builder()
            .documentLink(orderToDispensedDoc)
            .documentFileName(orderToDispensedDoc.getFilename())
            .documentType(DocumentType.DEEMED_AS_SERVICE_GRANTED)
            .build();


        assertThat(response.getData().getDocuments().getDocumentsGenerated())
            .extracting("value")
            .containsExactly(deemedOrDispensedDoc);

        verify(notificationDispatcher).send(serviceApplicationNotification, response.getData(), caseDetails.getId());
    }

    @Test
    void shouldUpdateServiceApplicationDecisionDateIfServiceApplicationIsNotGranted() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                        .builder()
                        .deemedServiceDate(LocalDate.now(clock))
                        .serviceApplicationGranted(NO)
                        .alternativeServiceType(DISPENSED)
                        .build()
        )
                .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setState(AwaitingServiceConsideration);
        caseDetails.setData(caseData);

        final Map<String, Object> templateContent = new HashMap<>();
        when(serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        var orderToDispensedDoc = new Document(
                DOCUMENT_URL,
                DISPENSED_WITH_SERVICE_REFUSED_FILE_NAME,
                DOCUMENT_URL + "/binary"
        );

        when(
                caseDataDocumentService.renderDocument(
                        templateContent,
                        TEST_CASE_ID,
                        SERVICE_REFUSAL_TEMPLATE_ID,
                        ENGLISH,
                        DISPENSED_WITH_SERVICE_REFUSED_FILE_NAME
                )).thenReturn(orderToDispensedDoc);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeServiceDecision.aboutToSubmit(caseDetails, null);

        assertThat(response.getState()).isEqualTo(ServiceAdminRefusal);

        ListValue<AlternativeServiceOutcome> listValue = response.getData().getAlternativeServiceOutcomes().get(0);
        assertThat(listValue.getValue().getServiceApplicationDecisionDate()).isEqualTo(getExpectedLocalDate());

        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldUpdateStateToAwaitingAosAndGenerateDispensedServiceRefusalOrderDocIfApplicationIsNotGrantedAndTypeIsDispensed() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .receivedServiceApplicationDate(LocalDate.now(clock))
                    .serviceApplicationGranted(NO)
                    .alternativeServiceType(DISPENSED)
                    .build()
            ).application(Application.builder().issueDate(LocalDate.now(clock)).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();
        when(serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        var dispenseWithServiceRefusedDoc = new Document(
            DOCUMENT_URL,
            DISPENSED_WITH_SERVICE_REFUSED_FILE_NAME,
            DOCUMENT_URL + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                SERVICE_REFUSAL_TEMPLATE_ID,
                ENGLISH,
                DISPENSED_WITH_SERVICE_REFUSED_FILE_NAME
            ))
            .thenReturn(dispenseWithServiceRefusedDoc);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeServiceDecision.aboutToSubmit(caseDetails, caseDetails);

        ListValue<AlternativeServiceOutcome> listValue = response.getData().getAlternativeServiceOutcomes().get(0);
        assertThat(listValue.getValue().getReceivedServiceApplicationDate())
            .isEqualTo(getExpectedLocalDate());

        assertThat(response.getState()).isEqualTo(AwaitingAos);

        var deemedOrDispensedDoc = DivorceDocument
            .builder()
            .documentLink(dispenseWithServiceRefusedDoc)
            .documentFileName(dispenseWithServiceRefusedDoc.getFilename())
            .documentType(DocumentType.DISPENSE_WITH_SERVICE_REFUSED)
            .build();

        assertThat(response.getData().getDocuments().getDocumentsGenerated())
            .extracting("value")
            .containsExactly(deemedOrDispensedDoc);

        verify(notificationDispatcher).send(serviceApplicationNotification, response.getData(), caseDetails.getId());
    }

    @Test
    void shouldUpdateStateToServiceAdminRefusalIfApplicationIsNotGrantedAndReasonIsAdminRefusal() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .receivedServiceApplicationDate(LocalDate.now(clock))
                    .serviceApplicationGranted(NO)
                    .refusalReason(ADMIN_REFUSAL)
                    .serviceApplicationRefusalReason("Reason order refused")
                    .alternativeServiceType(DISPENSED)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeServiceDecision.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getState()).isEqualTo(ServiceAdminRefusal);
        verifyNoInteractions(caseDataDocumentService);
        verifyNoInteractions(notificationDispatcher);
    }

    @Test
    void shouldUpdateStateToAwaitingAosAndGenerateDeemedServiceRefusalOrderDocIfApplicationIsNotGrantedAndTypeIsDeemed() {

        setMockClock(clock);

        final CaseData caseData = CaseData.builder()
            .alternativeService(
                AlternativeService
                    .builder()
                    .receivedServiceApplicationDate(LocalDate.now(clock))
                    .serviceApplicationGranted(NO)
                    .alternativeServiceType(DEEMED)
                    .build()
            ).application(Application.builder().issueDate(LocalDate.now(clock)).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final Map<String, Object> templateContent = new HashMap<>();
        when(serviceOrderTemplateContent.apply(caseData, TEST_CASE_ID)).thenReturn(templateContent);

        var deemedServiceRefusedDoc = new Document(
            DOCUMENT_URL,
            DEEMED_SERVICE_REFUSED_FILE_NAME,
            DOCUMENT_URL + "/binary"
        );

        when(
            caseDataDocumentService.renderDocument(
                templateContent,
                TEST_CASE_ID,
                SERVICE_REFUSAL_TEMPLATE_ID,
                ENGLISH,
                DEEMED_SERVICE_REFUSED_FILE_NAME
            ))
            .thenReturn(deemedServiceRefusedDoc);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            makeServiceDecision.aboutToSubmit(caseDetails, caseDetails);

        ListValue<AlternativeServiceOutcome> listValue = response.getData().getAlternativeServiceOutcomes().get(0);
        assertThat(listValue.getValue().getReceivedServiceApplicationDate())
            .isEqualTo(getExpectedLocalDate());

        assertThat(response.getState()).isEqualTo(AwaitingAos);

        var deemedOrDispensedDoc = DivorceDocument
            .builder()
            .documentLink(deemedServiceRefusedDoc)
            .documentFileName(deemedServiceRefusedDoc.getFilename())
            .documentType(DocumentType.DEEMED_SERVICE_REFUSED)
            .build();

        assertThat(response.getData().getDocuments().getDocumentsGenerated())
            .extracting("value")
            .containsExactly(deemedOrDispensedDoc);

        verify(notificationDispatcher).send(serviceApplicationNotification, response.getData(), caseDetails.getId());
    }
}
