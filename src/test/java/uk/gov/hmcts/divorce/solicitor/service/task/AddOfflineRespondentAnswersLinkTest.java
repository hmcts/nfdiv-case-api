package uk.gov.hmcts.divorce.solicitor.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.divorce.document.model.DocumentType.APPLICATION;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DEEMED_AS_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DISPENSE_WITH_SERVICE_GRANTED;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_ANSWERS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;

@ExtendWith(MockitoExtension.class)
class AddOfflineRespondentAnswersLinkTest {

    @InjectMocks
    private AddOfflineRespondentAnswersLink addOfflineRespondentAnswersLink;

    @Test
    void shouldSetRespondentAnswersLinkToUploadedRespondentAnswersDocument() {

        final Document documentLink1 = Document.builder()
            .filename("dispensedDocument1.pdf")
            .build();
        final Document documentLink2 = Document.builder()
            .filename("deemedDocument1.pdf")
            .build();
        final Document documentLink3 = Document.builder()
            .filename("respondentAnswers.pdf")
            .build();
        final Document documentLink4 = Document.builder()
            .filename("deemedDocument2.pdf")
            .build();

        final ListValue<DivorceDocument> documentListValue1 = documentWithType(DISPENSE_WITH_SERVICE_GRANTED, documentLink1);
        final ListValue<DivorceDocument> documentListValue2 = documentWithType(DEEMED_AS_SERVICE_GRANTED, documentLink2);
        final ListValue<DivorceDocument> documentListValue3 = documentWithType(RESPONDENT_ANSWERS, documentLink3);
        final ListValue<DivorceDocument> documentListValue4 = documentWithType(DEEMED_AS_SERVICE_GRANTED, documentLink4);

        final CaseDocuments caseDocuments = CaseDocuments.builder()
            .documentsUploaded(List.of(documentListValue4, documentListValue2, documentListValue3, documentListValue1))
            .build();

        final var caseData = CaseData.builder()
            .documents(caseDocuments)
            .build();

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> response = addOfflineRespondentAnswersLink.apply(caseDetails);

        assertThat(response.getData().getConditionalOrder().getRespondentAnswersLink()).isEqualTo(documentLink3);
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