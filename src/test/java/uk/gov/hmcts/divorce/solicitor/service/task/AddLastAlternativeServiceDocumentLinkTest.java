package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeService;
import uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceOutcome;
import uk.gov.hmcts.divorce.divorcecase.model.Bailiff;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.NO;
import static uk.gov.hmcts.ccd.sdk.type.YesOrNo.YES;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.BAILIFF;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DEEMED;
import static uk.gov.hmcts.divorce.divorcecase.model.AlternativeServiceType.DISPENSED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.BAILIFF_CERTIFICATE_OF_SERVICE;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DEEMED_AS_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DISPENSE_WITH_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class AddLastAlternativeServiceDocumentLinkTest {

    @InjectMocks
    private AddLastAlternativeServiceDocumentLink addLastAlternativeServiceDocumentLink;

    @Test
    void shouldSetCertificateOfServiceDocumentIfBailiffAlternativeServiceIsSuccessfullyServed() {

        final Document documentLink = Document.builder()
            .filename("bailiffDocument.pdf")
            .build();

        final var caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .alternativeServiceType(BAILIFF)
                .bailiff(Bailiff.builder()
                    .certificateOfServiceDocument(DivorceDocument.builder()
                        .documentType(BAILIFF_CERTIFICATE_OF_SERVICE)
                        .documentLink(documentLink)
                        .build())
                    .successfulServedByBailiff(YES)
                    .build())
                .build())
            .build();

        caseData.setAlternativeServiceOutcomes(getAlternativeServiceOutcomes(caseData));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> updatedCaseDetails = addLastAlternativeServiceDocumentLink.apply(caseDetails);

        assertThat(updatedCaseDetails.getData().getConditionalOrder().getLastAlternativeServiceDocumentLink())
            .isEqualTo(documentLink);
    }

    @Test
    void shouldNotSetCertificateOfServiceDocumentIfBailiffAlternativeServiceIsNotSuccessfullyServed() {

        final Document documentLink = Document.builder()
            .filename("bailiffDocument.pdf")
            .build();

        final var caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .alternativeServiceType(BAILIFF)
                .bailiff(Bailiff.builder()
                    .certificateOfServiceDocument(DivorceDocument.builder()
                        .documentType(BAILIFF_CERTIFICATE_OF_SERVICE)
                        .documentLink(documentLink)
                        .build())
                    .successfulServedByBailiff(NO)
                    .build())
                .build())
            .build();

        caseData.setAlternativeServiceOutcomes(getAlternativeServiceOutcomes(caseData));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> updatedCaseDetails = addLastAlternativeServiceDocumentLink.apply(caseDetails);

        assertThat(updatedCaseDetails.getData().getConditionalOrder().getLastAlternativeServiceDocumentLink())
            .isNull();
    }

    @Test
    void shouldSetDeemedApplicationGrantedDocumentIfDeemedAlternativeServiceIsGranted() {

        final Document documentLink = Document.builder()
            .filename("deemedDocument.pdf")
            .build();

        final var caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .alternativeServiceType(DEEMED)
                .serviceApplicationGranted(YES)
                .build())
            .build();

        caseData.setAlternativeServiceOutcomes(getAlternativeServiceOutcomes(caseData));

        final ListValue<DivorceDocument> documentListValue = documentWithType(DEEMED_AS_SERVICE_GRANTED, documentLink);

        caseData.setDocuments(
            CaseDocuments.builder()
                .documentsGenerated(singletonList(documentListValue))
                .build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> updatedCaseDetails = addLastAlternativeServiceDocumentLink.apply(caseDetails);

        assertThat(updatedCaseDetails.getData().getConditionalOrder().getLastAlternativeServiceDocumentLink())
            .isEqualTo(documentLink);
    }

    @Test
    void shouldNoSetDeemedApplicationGrantedDocumentIfDeemedAlternativeServiceIsNotGranted() {

        final Document documentLink = Document.builder()
            .filename("deemedDocument.pdf")
            .build();

        final var caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .alternativeServiceType(DEEMED)
                .serviceApplicationGranted(NO)
                .build())
            .build();

        caseData.setAlternativeServiceOutcomes(getAlternativeServiceOutcomes(caseData));

        final ListValue<DivorceDocument> documentListValue = documentWithType(DEEMED_AS_SERVICE_GRANTED, documentLink);

        caseData.setDocuments(
            CaseDocuments.builder()
                .documentsGenerated(singletonList(documentListValue))
                .build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> updatedCaseDetails = addLastAlternativeServiceDocumentLink.apply(caseDetails);

        assertThat(updatedCaseDetails.getData().getConditionalOrder().getLastAlternativeServiceDocumentLink())
            .isNull();
    }

    @Test
    void shouldSetDispensedApplicationGrantedDocumentIfDispensedAlternativeServiceIsGranted() {

        final Document documentLink = Document.builder()
            .filename("dispensedDocument.pdf")
            .build();

        final var caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .alternativeServiceType(DISPENSED)
                .serviceApplicationGranted(YES)
                .build())
            .build();

        caseData.setAlternativeServiceOutcomes(getAlternativeServiceOutcomes(caseData));

        final ListValue<DivorceDocument> documentListValue = documentWithType(DISPENSE_WITH_SERVICE_GRANTED, documentLink);

        caseData.setDocuments(
            CaseDocuments.builder()
                .documentsGenerated(singletonList(documentListValue))
                .build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> updatedCaseDetails = addLastAlternativeServiceDocumentLink.apply(caseDetails);

        assertThat(updatedCaseDetails.getData().getConditionalOrder().getLastAlternativeServiceDocumentLink())
            .isEqualTo(documentLink);
    }

    @Test
    void shouldNotSetDispensedApplicationGrantedDocumentIfDispensedAlternativeServiceIsNotGranted() {

        final Document documentLink = Document.builder()
            .filename("dispensedDocument.pdf")
            .build();

        final var caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .alternativeServiceType(DISPENSED)
                .serviceApplicationGranted(NO)
                .build())
            .build();

        caseData.setAlternativeServiceOutcomes(getAlternativeServiceOutcomes(caseData));

        final ListValue<DivorceDocument> documentListValue = documentWithType(DISPENSE_WITH_SERVICE_GRANTED, documentLink);

        caseData.setDocuments(
            CaseDocuments.builder()
                .documentsGenerated(singletonList(documentListValue))
                .build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> updatedCaseDetails = addLastAlternativeServiceDocumentLink.apply(caseDetails);

        assertThat(updatedCaseDetails.getData().getConditionalOrder().getLastAlternativeServiceDocumentLink())
            .isNull();
    }

    @Test
    void shouldNotSetDocumentIfNoAlternativeServiceTypeSet() {

        final Document documentLink = Document.builder()
            .filename("dispensedDocument.pdf")
            .build();

        final var caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .serviceApplicationGranted(YES)
                .build())
            .build();

        caseData.setAlternativeServiceOutcomes(getAlternativeServiceOutcomes(caseData));

        final ListValue<DivorceDocument> documentListValue = documentWithType(DISPENSE_WITH_SERVICE_GRANTED, documentLink);

        caseData.setDocuments(
            CaseDocuments.builder()
                .documentsGenerated(singletonList(documentListValue))
                .build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> updatedCaseDetails = addLastAlternativeServiceDocumentLink.apply(caseDetails);

        assertThat(updatedCaseDetails.getData().getConditionalOrder().getLastAlternativeServiceDocumentLink())
            .isNull();
    }

    @Test
    void shouldNotSetDocumentIfNoAlternativeServiceOutcome() {

        final Document documentLink = Document.builder()
            .filename("dispensedDocument.pdf")
            .build();

        final var caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .serviceApplicationGranted(YES)
                .build())
            .build();

        final ListValue<DivorceDocument> documentListValue = documentWithType(DISPENSE_WITH_SERVICE_GRANTED, documentLink);

        caseData.setDocuments(
            CaseDocuments.builder()
                .documentsGenerated(singletonList(documentListValue))
                .build());

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> updatedCaseDetails = addLastAlternativeServiceDocumentLink.apply(caseDetails);

        assertThat(updatedCaseDetails.getData().getConditionalOrder().getLastAlternativeServiceDocumentLink())
            .isNull();
    }

    @Test
    void shouldNotSetDocumentIfNoGeneratedDocuments() {

        final var caseData = CaseData.builder()
            .alternativeService(AlternativeService.builder()
                .serviceApplicationGranted(YES)
                .build())
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> updatedCaseDetails = addLastAlternativeServiceDocumentLink.apply(caseDetails);

        assertThat(updatedCaseDetails.getData().getConditionalOrder().getLastAlternativeServiceDocumentLink())
            .isNull();
    }

    private List<ListValue<AlternativeServiceOutcome>> getAlternativeServiceOutcomes(final CaseData caseData) {
        final var alternativeServiceOutcomeListValue = ListValue.<AlternativeServiceOutcome>builder()
            .value(caseData.getAlternativeService().getOutcome())
            .build();

        final List<ListValue<AlternativeServiceOutcome>> alternativeServiceOutcomes = new ArrayList<>();
        alternativeServiceOutcomes.add(0, alternativeServiceOutcomeListValue);
        return alternativeServiceOutcomes;
    }

    private ListValue<DivorceDocument> documentWithType(final DocumentType documentType, final Document document) {

        return ListValue.<DivorceDocument>builder()
            .id(APPLICATION.getLabel())
            .value(DivorceDocument
                .builder()
                .documentLink(document)
                .documentFileName("test-draft-divorce-application-12345.pdf")
                .documentType(documentType)
                .build())
            .build();
    }
}