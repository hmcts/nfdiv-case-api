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
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.caseworker.service.task.GenerateFinalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.Application;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ConditionalOrder;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.model.UserRole;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateCertificateOfEntitlement;
import uk.gov.hmcts.divorce.systemupdate.service.task.GenerateConditionalOrderPronouncedDocument;

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
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.getDivorceDocumentListValue;

@ExtendWith(MockitoExtension.class)
public class CaseworkerRegenerateCourtOrdersTest {

    @Mock
    private GenerateConditionalOrderPronouncedDocument generateConditionalOrderPronouncedDocument;

    @Mock
    private GenerateCertificateOfEntitlement generateCertificateOfEntitlement;

    @Mock
    private GenerateFinalOrder generateFinalOrder;

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
    void shouldNotRegenerateCourtOrdersForPaperCase() {
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();

        final CaseData caseData = CaseData
            .builder()
            .application(
                Application
                    .builder()
                    .newPaperCase(YesOrNo.YES)
                    .build()
            )
            .build();

        caseDetails.setId(1L);
        caseDetails.setData(caseData);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRegenerateCourtOrders.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);
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

        doNothing().when(generateConditionalOrderPronouncedDocument).removeExistingAndGenerateNewConditionalOrderGrantedDoc(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRegenerateCourtOrders.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(caseData);

        verify(generateConditionalOrderPronouncedDocument).removeExistingAndGenerateNewConditionalOrderGrantedDoc(caseDetails);
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
        doNothing().when(generateConditionalOrderPronouncedDocument).removeExistingAndGenerateNewConditionalOrderGrantedDoc(caseDetails);
        doNothing().when(generateFinalOrder).removeExistingAndGenerateNewFinalOrderGrantedDoc(caseDetails);

        final AboutToStartOrSubmitResponse<CaseData, State> response =
            caseworkerRegenerateCourtOrders.aboutToSubmit(caseDetails, caseDetails);

        assertThat(response.getData()).isEqualTo(updatedCaseData);

        verify(generateConditionalOrderPronouncedDocument).removeExistingAndGenerateNewConditionalOrderGrantedDoc(caseDetails);
        verify(generateFinalOrder).removeExistingAndGenerateNewFinalOrderGrantedDoc(caseDetails);
        verify(generateCertificateOfEntitlement).apply(caseDetails);
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
        final Document certificateOfEntitlementDocumentLink = Document.builder()
            .url("http://dm-store-aat.service.core-compute-aat.internal/documents/fa1c052a-20ed-4eb2-a2dd-01322553d5a3")
            .filename("certificateOfEntitlement-1641906321238843-2022-01-11:13:06.pdf")
            .binaryUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/fa1c052a-20ed-4eb2-a2dd-01322553d5a3/binary")
            .build();
        return certificateOfEntitlementDocumentLink;
    }
}
