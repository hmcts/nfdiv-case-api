package uk.gov.hmcts.divorce.common.event.page;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.api.callback.AboutToStartOrSubmitResponse;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class Applicant2SolAosOtherProceedingsTest {

    @InjectMocks
    private Applicant2SolAosOtherProceedings applicant2SolAosOtherProceedings;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnErrorWhenNoDocumentsAreUploaded() {
        CaseData caseData = new CaseData();
        Applicant applicant2 = new Applicant();
        applicant2.setLegalProceedingDocs(null);
        caseData.setApplicant2(applicant2);

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicant2SolAosOtherProceedings.midEvent(caseDetails, null);

        assertThat(response.getErrors()).containsExactly("Please upload a document in order to continue");
    }

    @Test
    void shouldReturnErrorWhenDocumentLinkIsMissing() {
        CaseData caseData = new CaseData();
        Applicant applicant2 = new Applicant();
        ListValue<DivorceDocument> document = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder().documentLink(null).build())
            .build();
        applicant2.setLegalProceedingDocs(singletonList(document));
        caseData.setApplicant2(applicant2);

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicant2SolAosOtherProceedings.midEvent(caseDetails, null);

        assertThat(response.getErrors()).containsExactly("No document attached to one or more uploads");
    }

    @Test
    void shouldReturnNoErrorsWhenDocumentsAreValid() {
        CaseData caseData = new CaseData();
        Applicant applicant2 = new Applicant();
        ListValue<DivorceDocument> document = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder().documentLink(Document.builder().build()).build())
            .build();
        applicant2.setLegalProceedingDocs(singletonList(document));
        caseData.setApplicant2(applicant2);

        CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);

        AboutToStartOrSubmitResponse<CaseData, State> response = applicant2SolAosOtherProceedings.midEvent(caseDetails, null);

        assertThat(response.getErrors()).isNull();
    }
}
