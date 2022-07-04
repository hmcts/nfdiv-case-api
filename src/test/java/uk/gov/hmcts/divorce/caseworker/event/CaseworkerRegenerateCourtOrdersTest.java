package uk.gov.hmcts.divorce.caseworker.event;

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
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateFinalOrder;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateFinalOrderCoverLetter;
import uk.gov.hmcts.divorce.common.notification.RegenerateCourtOrdersNotification;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlement;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedCoversheet;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedDocument;
import uk.gov.hmcts.divorce.systemupdate.service.task.RemoveExistingConditionalOrderPronouncedDocument;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRegenerateCourtOrders.CASEWORKER_REGENERATE_COURT_ORDERS;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;

@ExtendWith(MockitoExtension.class)
public class CaseworkerRegenerateCourtOrdersTest {

    @Mock
    private GenerateCertificateOfEntitlement generateCertificateOfEntitlement;

    @Mock
    private GenerateConditionalOrderPronouncedDocument generateConditionalOrderPronouncedDocument;

    @Mock
    private GenerateConditionalOrderPronouncedCoversheet generateConditionalOrderPronouncedCoversheetDocument;

    @Mock
    private GenerateFinalOrderCoverLetter generateFinalOrderCoverLetter;

    @Mock
    private GenerateFinalOrder generateFinalOrder;

    @Mock
    private RegenerateCourtOrdersNotification regenerateCourtOrdersNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private RemoveExistingConditionalOrderPronouncedDocument removeExistingConditionalOrderPronouncedDocument;

    @InjectMocks
    private CaseworkerRegenerateCourtOrders caseworkerRegenerateCourtOrders;

    @Test
    void shouldAddConfigurationToConfigBuilder() {
        final ConfigBuilderImpl<CaseData, State, UserRole> configBuilder = createCaseDataConfigBuilder();

        caseworkerRegenerateCourtOrders.configure(configBuilder);

        assertThat(getEventsFrom(configBuilder).values())
            .extracting(Event::getId)
            .contains(CASEWORKER_REGENERATE_COURT_ORDERS);
    }

    @Test
    void shouldNotRegenerateCourtOrdersForDigitalCaseWhenThereAreNoExistingCOEAndCOGrantedAndFOGrantedDocumentsForDigitalCase() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final CaseData caseData = CaseData.builder().build();
        caseDetails.setId(1L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRegenerateCourtOrders.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldOnlyRegenerateCOEDocumentWhenCOEExistsAndCOGrantedAndFOGrantedDoesNotExistsForDigitalCase() {
        final CaseData caseData = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .dateAndTimeOfHearing(LocalDateTime.now())
                    .certificateOfEntitlementDocument(
                        divorceDocumentWithFileName("certificateOfEntitlement-1641906321238843-2022-01-11:13:06.pdf")
                    )
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final CaseData updatedCaseData = CaseData.builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .dateAndTimeOfHearing(LocalDateTime.now())
                    .certificateOfEntitlementDocument(
                        divorceDocumentWithFileName("certificateOfEntitlement-1641906321238843-2022-02-22:16:06.pdf")
                    )
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(updatedCaseData);

        when(generateCertificateOfEntitlement.apply(caseDetails)).thenReturn(updatedCaseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRegenerateCourtOrders.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(updatedCaseData);

        verify(generateCertificateOfEntitlement).removeExistingAndGenerateNewCertificateOfEntitlementCoverLetters(caseDetails);
        verify(generateCertificateOfEntitlement).apply(caseDetails);
    }

    @Test
    void shouldOnlyRegenerateCOGrantedDocumentWhenCOGrantedDocExistsAndCOEAndFOGrantedDoesNotExistsForDigitalCase() {
        final CaseData caseData = CaseData
            .builder()
            .documents(
                CaseDocuments
                    .builder()
                    .documentsGenerated(
                        List.of(getDivorceDocumentListValue(
                                "http://localhost:4200/assets/8c75732c-d640-43bf-a0e9-f33452243696",
                                "co_granted.pdf",
                                CONDITIONAL_ORDER_GRANTED
                            )
                        )
                    ).build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        when(removeExistingConditionalOrderPronouncedDocument.apply(caseDetails)).thenReturn(caseDetails);
        when(generateConditionalOrderPronouncedDocument.apply(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRegenerateCourtOrders.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);

        verify(generateConditionalOrderPronouncedCoversheetDocument)
            .removeExistingAndGenerateConditionalOrderPronouncedCoversheet(caseDetails);
        verify(removeExistingConditionalOrderPronouncedDocument).apply(caseDetails);
        verify(generateConditionalOrderPronouncedDocument).apply(caseDetails);
    }

    @Test
    void shouldOnlyRegenerateFOGrantedDocumentWhenCOGrantedDocExistsAndCOEAndCOGrantedDoesNotExistsForDigitalCase() {
        final CaseData caseData = CaseData
            .builder()
            .documents(
                CaseDocuments
                    .builder()
                    .documentsGenerated(
                        List.of(getDivorceDocumentListValue(
                                "http://localhost:4200/assets/8c75732c-d640-43bf-a0e9-f33452243696",
                                "fo_granted.pdf",
                                FINAL_ORDER_GRANTED
                            )
                        )
                    ).build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        doNothing().when(generateFinalOrder).removeExistingAndGenerateNewFinalOrderGrantedDoc(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRegenerateCourtOrders.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);

        verify(generateFinalOrderCoverLetter).removeExistingAndGenerateNewFinalOrderGrantedCoverLetters(caseDetails);
        verify(generateFinalOrder).removeExistingAndGenerateNewFinalOrderGrantedDoc(caseDetails);
    }

    @Test
    void shouldRegenerateCOGrantedDocumentAndFOGrantedAndCOEWhenAllDocsExistForDigitalCase() {
        final CaseData caseData = CaseData
            .builder()
            .documents(
                CaseDocuments
                    .builder()
                    .documentsGenerated(
                        List.of(getDivorceDocumentListValue(
                                "http://localhost:4200/assets/8c75732c-d640-43bf-a0e9-f33452243696",
                                "co_granted.pdf",
                                CONDITIONAL_ORDER_GRANTED
                            ),
                            getDivorceDocumentListValue(
                                "http://localhost:4200/assets/8c75732c-d640-43bf-a0e9-f33452243696",
                                "fo_granted.pdf",
                                FINAL_ORDER_GRANTED
                            )
                        )
                    ).build()
            )
            .conditionalOrder(
                ConditionalOrder.builder()
                    .dateAndTimeOfHearing(LocalDateTime.now())
                    .certificateOfEntitlementDocument(
                        divorceDocumentWithFileName("certificateOfEntitlement-1641906321238843-2022-01-11:13:06.pdf")
                    )
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        final ListValue<DivorceDocument> regeneratedCODoc =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "co_granted.pdf", CONDITIONAL_ORDER_GRANTED);
        final ListValue<DivorceDocument> regeneratedFODoc =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003", "fo_granted.pdf", FINAL_ORDER_GRANTED);

        List<ListValue<DivorceDocument>> documentsGenerated = new ArrayList<>();
        documentsGenerated.add(regeneratedCODoc);
        documentsGenerated.add(regeneratedFODoc);

        final CaseData updatedCaseData = CaseData
            .builder()
            .conditionalOrder(
                ConditionalOrder.builder()
                    .dateAndTimeOfHearing(LocalDateTime.now())
                    .certificateOfEntitlementDocument(
                        divorceDocumentWithFileName("certificateOfEntitlement-1641906321238843-2022-02-22:16:06.pdf")
                    )
                    .build()
            )
            .documents(
                CaseDocuments
                    .builder()
                    .documentsGenerated(documentsGenerated)
                    .build()
            )
            .build();

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(updatedCaseData);

        when(generateCertificateOfEntitlement.apply(caseDetails)).thenReturn(updatedCaseDetails);
        when(removeExistingConditionalOrderPronouncedDocument.apply(caseDetails)).thenReturn(caseDetails);
        when(generateConditionalOrderPronouncedDocument.apply(caseDetails)).thenReturn(caseDetails);
        doNothing().when(generateFinalOrder).removeExistingAndGenerateNewFinalOrderGrantedDoc(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRegenerateCourtOrders.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(updatedCaseData);

        verify(generateConditionalOrderPronouncedCoversheetDocument)
            .removeExistingAndGenerateConditionalOrderPronouncedCoversheet(caseDetails);
        verify(generateFinalOrderCoverLetter).removeExistingAndGenerateNewFinalOrderGrantedCoverLetters(caseDetails);
        verify(removeExistingConditionalOrderPronouncedDocument).apply(caseDetails);
        verify(generateConditionalOrderPronouncedDocument).apply(caseDetails);
        verify(generateCertificateOfEntitlement).apply(caseDetails);
        verify(generateFinalOrder).removeExistingAndGenerateNewFinalOrderGrantedDoc(caseDetails);
        verify(generateCertificateOfEntitlement).removeExistingAndGenerateNewCertificateOfEntitlementCoverLetters(caseDetails);
    }

    @Test
    void shouldTriggerNotificationsInSubmittedCallback() {
        final CaseData caseData = new CaseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        caseworkerRegenerateCourtOrders.submitted(caseDetails, null);

        verify(notificationDispatcher).send(regenerateCourtOrdersNotification, caseData, TEST_CASE_ID);
    }

    private DivorceDocument divorceDocumentWithFileName(String fileName) {
        return DivorceDocument
            .builder()
            .documentLink(certificateOfEntitlementDocumentLink())
            .documentType(CERTIFICATE_OF_ENTITLEMENT)
            .documentFileName(fileName)
            .build();
    }

    private Document certificateOfEntitlementDocumentLink() {
        return Document.builder()
            .url("http://dm-store-aat.service.core-compute-aat.internal/documents/fa1c052a-20ed-4eb2-a2dd-01322553d5a3")
            .filename("certificateOfEntitlement-1641906321238843-2022-01-11:13:06.pdf")
            .binaryUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/fa1c052a-20ed-4eb2-a2dd-01322553d5a3/binary")
            .build();
    }
}
