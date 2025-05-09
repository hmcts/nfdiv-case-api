package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.ContactDetailsType;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class SaveLegalProceedingDocumentsToCaseDocumentsTest {

    @InjectMocks
    private SaveLegalProceedingDocumentsToCaseDocuments saveLegalProceedingDocumentsToCaseDocuments;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldSaveDocumentsToConfidentialDocumentsUploadedWhenContactDetailsAreConfidential() {
        // Arrange
        CaseData caseData = new CaseData();
        Applicant applicant2 = new Applicant();
        applicant2.setContactDetailsType(ContactDetailsType.PRIVATE);

        ListValue<DivorceDocument> document = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder().documentFileName("test-doc.pdf").build())
            .build();

        applicant2.setLpDocuments(Collections.singletonList(document));
        caseData.setApplicant2(applicant2);
        caseData.setDocuments(new CaseDocuments());

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        saveLegalProceedingDocumentsToCaseDocuments.apply(caseDetails);

        assertThat(caseData.getDocuments().getConfidentialDocumentsUploaded()).isNotEmpty();
        assertThat(caseData.getDocuments().getConfidentialDocumentsUploaded().get(0).getValue().getDocumentFileName())
            .isEqualTo("test-doc.pdf");
        assertThat(caseData.getApplicant2().getLpDocuments()).isNull();
    }

    @Test
    void shouldSaveDocumentsToApplicant2DocumentsUploadedWhenContactDetailsAreNotConfidential() {
        CaseData caseData = new CaseData();
        Applicant applicant2 = new Applicant();
        applicant2.setContactDetailsType(ContactDetailsType.PUBLIC);

        ListValue<DivorceDocument> document = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder().documentFileName("test-doc.pdf").build())
            .build();

        applicant2.setLpDocuments(Collections.singletonList(document));
        caseData.setApplicant2(applicant2);
        caseData.setDocuments(new CaseDocuments());

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        saveLegalProceedingDocumentsToCaseDocuments.apply(caseDetails);

        assertThat(caseData.getDocuments().getApplicant2DocumentsUploaded()).isNotEmpty();
        assertThat(caseData.getDocuments().getApplicant2DocumentsUploaded().get(0).getValue().getDocumentFileName())
            .isEqualTo("test-doc.pdf");
        assertThat(caseData.getApplicant2().getLpDocuments()).isNull();
    }

    @Test
    void shouldDoNothingWhenDocumentsListIsEmpty() {
        CaseData caseData = new CaseData();
        Applicant applicant2 = new Applicant();
        applicant2.setLpDocuments(Collections.emptyList());
        caseData.setApplicant2(applicant2);
        caseData.setDocuments(new CaseDocuments());

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        saveLegalProceedingDocumentsToCaseDocuments.apply(caseDetails);

        assertThat(caseData.getDocuments().getConfidentialDocumentsUploaded()).isNull();
        assertThat(caseData.getDocuments().getApplicant2DocumentsUploaded()).isNull();
    }
}
