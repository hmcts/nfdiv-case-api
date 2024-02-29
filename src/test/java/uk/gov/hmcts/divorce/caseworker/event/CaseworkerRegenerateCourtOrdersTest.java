package uk.gov.hmcts.divorce.caseworker.event;

import com.google.common.collect.Lists;
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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.common.notification.RegenerateCourtOrdersNotification;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.print.documentpack.CertificateOfEntitlementDocumentPack;
import uk.gov.hmcts.divorce.notification.NotificationDispatcher;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedCoversheet;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedDocument;
import uk.gov.hmcts.divorce.systemupdate.service.task.RemoveExistingConditionalOrderPronouncedDocument;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.divorce.caseworker.event.CaseworkerRegenerateCourtOrders.CASEWORKER_REGENERATE_COURT_ORDERS;
import static uk.gov.hmcts.divorce.document.DocumentConstants.APPLICANT1;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.FINAL_ORDER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CONDITIONAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.FINAL_ORDER_GRANTED;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.createCaseDataConfigBuilder;
import static uk.gov.hmcts.divorce.testutil.ConfigTestUtil.getEventsFrom;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;

@ExtendWith(MockitoExtension.class)
class CaseworkerRegenerateCourtOrdersTest {

    @Mock
    private GenerateConditionalOrderPronouncedDocument generateConditionalOrderPronouncedDocument;

    @Mock
    private GenerateConditionalOrderPronouncedCoversheet generateConditionalOrderPronouncedCoversheetDocument;

    @Mock
    private RegenerateCourtOrdersNotification regenerateCourtOrdersNotification;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Mock
    private RemoveExistingConditionalOrderPronouncedDocument removeExistingConditionalOrderPronouncedDocument;

    @Mock
    private DocumentGenerator documentGenerator;

    @Mock
    private CertificateOfEntitlementDocumentPack certificateOfEntitlementDocumentPack;

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
        caseDetails.setId(TEST_CASE_ID);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRegenerateCourtOrders.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
    }

    @Test
    void shouldOnlyRegenerateCOEDocumentWhenCOEExistsAndCOGrantedAndFOGrantedDoesNotExistsForDigitalCase() {

        final var certificateOfEntitlementDocuments = buildCertificateOfEntitlementDocuments();

        final CaseData caseData = CaseData.builder()
            .applicant1(
                    Applicant
                            .builder()
                            .firstName(APPLICANT1)
                            .build())
            .conditionalOrder(
                ConditionalOrder.builder()
                    .dateAndTimeOfHearing(LocalDateTime.now())
                    .certificateOfEntitlementDocument(
                        divorceDocumentWithFileName("certificateOfEntitlement-1641906321238843-2022-01-11:13:06.pdf")
                    )
                    .build()
            ).documents(CaseDocuments.builder().documentsGenerated(certificateOfEntitlementDocuments).build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRegenerateCourtOrders.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
    }

    private static List<ListValue<DivorceDocument>> buildCertificateOfEntitlementDocuments() {
        return Lists.newArrayList(
                ListValue.<DivorceDocument>builder()
                        .id("1")
                        .value(DivorceDocument.builder()
                                .documentType(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP1)
                                .build())
                        .build(),
                ListValue.<DivorceDocument>builder()
                        .id("2")
                        .value(DivorceDocument.builder()
                                .documentType(CERTIFICATE_OF_ENTITLEMENT_COVER_LETTER_APP2)
                                .build())
                        .build(),
                ListValue.<DivorceDocument>builder()
                        .id("3")
                        .value(DivorceDocument.builder()
                                .documentType(CERTIFICATE_OF_ENTITLEMENT)
                                .build()).build()
        );
    }

    @Test
    void shouldOnlyRegenerateCOGrantedDocumentWhenCOGrantedDocExistsAndCOEAndFOGrantedDoesNotExistsForDigitalCase() {

        final var generatedDocuments = buildCertificateOfEntitlementDocuments();
        generatedDocuments.add(getDivorceDocumentListValue(
                "http://localhost:4200/assets/8c75732c-d640-43bf-a0e9-f33452243696",
                "co_granted.pdf",
                CONDITIONAL_ORDER_GRANTED
        ));

        final CaseData caseData = CaseData
            .builder()
            .documents(
                CaseDocuments
                    .builder()
                    .documentsGenerated(generatedDocuments
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

        verify(removeExistingConditionalOrderPronouncedDocument).apply(caseDetails);
        verify(generateConditionalOrderPronouncedDocument).apply(caseDetails);
        verify(notificationDispatcher).send(regenerateCourtOrdersNotification, caseData, caseDetails.getId());
    }

    @Test
    void shouldOnlyRegenerateFOGrantedDocumentWhenCOGrantedDocExistsAndCOEAndCOGrantedDoesNotExistsForDigitalCase() {

        final var generatedDocuments = buildCertificateOfEntitlementDocuments();
        generatedDocuments.add(getDivorceDocumentListValue(
                "http://localhost:4200/assets/8c75732c-d640-43bf-a0e9-f33452243696",
                "fo_granted.pdf",
                FINAL_ORDER_GRANTED
        ));

        final CaseData caseData = CaseData
            .builder()
            .documents(
                CaseDocuments
                    .builder()
                    .documentsGenerated(
                        generatedDocuments
                    ).build()
            )
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseData.getApplicant1().setFirstName("Harry");
        caseData.getApplicant1().setOffline(YesOrNo.YES);
        caseData.getApplicant2().setFirstName("Sally");
        caseData.getApplicant2().setOffline(YesOrNo.YES);
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRegenerateCourtOrders.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);

        verify(documentGenerator).generateAndStoreCaseDocument(FINAL_ORDER_GRANTED,
            FINAL_ORDER_TEMPLATE_ID,
            FINAL_ORDER_DOCUMENT_NAME,
            caseData,
            caseDetails.getId());
        verify(notificationDispatcher).send(regenerateCourtOrdersNotification, caseData, TEST_CASE_ID);
    }

    @Test
    void shouldRegenerateCOGrantedDocumentAndFOGrantedAndCOEWhenAllDocsExistForDigitalCase() {
        final CaseData caseData = CaseData
            .builder()
            .applicant1(
                    Applicant
                            .builder()
                            .firstName(APPLICANT1)
                            .build())
            .documents(
                CaseDocuments
                    .builder()
                    .documentsGenerated(
                        new ArrayList<>(List.of(getDivorceDocumentListValue(
                                "http://localhost:4200/assets/8c75732c-d640-43bf-a0e9-f33452243696",
                                "co_granted.pdf",
                                CONDITIONAL_ORDER_GRANTED
                            ),
                            getDivorceDocumentListValue(
                                "http://localhost:4200/assets/8c75732c-d640-43bf-a0e9-f33452243696",
                                "fo_granted.pdf",
                                FINAL_ORDER_GRANTED
                            )
                        ))
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
        caseDetails.setId(TEST_CASE_ID);

        final ListValue<DivorceDocument> regeneratedCODoc =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
                    "co_granted.pdf", CONDITIONAL_ORDER_GRANTED);

        final ListValue<DivorceDocument> regeneratedFODoc =
            getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
                    "fo_granted.pdf", FINAL_ORDER_GRANTED);

        final ListValue<DivorceDocument> certificateOfEntitlement =
                getDivorceDocumentListValue("http://localhost:4200/assets/59a54ccc-979f-11eb-a8b3-0242ac130003",
                        "certificate_of_entitlement.pdf", CERTIFICATE_OF_ENTITLEMENT);

        List<ListValue<DivorceDocument>> documentsGenerated = new ArrayList<>();
        documentsGenerated.add(regeneratedCODoc);
        documentsGenerated.add(regeneratedFODoc);
        documentsGenerated.add(certificateOfEntitlement);

        caseData.getDocuments().setDocumentsGenerated(documentsGenerated);

        final CaseDetails<CaseData, State> updatedCaseDetails = new CaseDetails<>();
        updatedCaseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        when(removeExistingConditionalOrderPronouncedDocument.apply(caseDetails)).thenReturn(caseDetails);
        when(generateConditionalOrderPronouncedDocument.apply(caseDetails)).thenReturn(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRegenerateCourtOrders.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);

        verify(removeExistingConditionalOrderPronouncedDocument).apply(caseDetails);
        verify(generateConditionalOrderPronouncedDocument).apply(caseDetails);
        verify(documentGenerator).generateAndStoreCaseDocument(FINAL_ORDER_GRANTED, FINAL_ORDER_TEMPLATE_ID, FINAL_ORDER_DOCUMENT_NAME,
            caseData, caseDetails.getId());
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
