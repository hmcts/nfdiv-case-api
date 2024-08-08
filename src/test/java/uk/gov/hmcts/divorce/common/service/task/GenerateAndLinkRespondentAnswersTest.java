package uk.gov.hmcts.divorce.common.service.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.ccd.sdk.type.YesOrNo;
import uk.gov.hmcts.divorce.divorcecase.model.AcknowledgementOfService;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.DocumentGenerator;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_ANSWERS_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.RESPONDENT_ANSWERS_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.RESPONDENT_ANSWERS;
import static uk.gov.hmcts.divorce.testutil.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.divorce.testutil.TestDataHelper.caseData;

@ExtendWith(MockitoExtension.class)
class GenerateAndLinkRespondentAnswersTest {

    @Mock
    private DocumentGenerator documentGenerator;

    @InjectMocks
    private GenerateAndLinkRespondentAnswers generateAndLinkRespondentAnswers;

    @Test
    void shouldSetRespondentAnswersLinkIfGenerateAnswersDocumentPresent() {

        final Document documentLink = new Document("url", "filename", "binary url");
        final ListValue<DivorceDocument> respondentAnswersListValue = ListValue.<DivorceDocument>builder()
            .value(DivorceDocument.builder()
                .documentType(RESPONDENT_ANSWERS)
                .documentLink(documentLink)
                .build())
            .build();

        final CaseData caseData = caseData();
        caseData.getDocuments().setDocumentsGenerated(singletonList(respondentAnswersListValue));

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> result = generateAndLinkRespondentAnswers.apply(caseDetails);

        verify(documentGenerator).generateAndStoreCaseDocument(RESPONDENT_ANSWERS,
            RESPONDENT_ANSWERS_TEMPLATE_ID,
            RESPONDENT_ANSWERS_DOCUMENT_NAME,
            caseData,
            TEST_CASE_ID);
        assertThat(result.getData().getConditionalOrder().getRespondentAnswersLink()).isSameAs(documentLink);
    }

    @Test
    void shouldNotSetMiniApplicationLinkIfNoDivorceApplicationDocumentPresent() {

        final CaseData caseData = caseData();
        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        final CaseDetails<CaseData, State> result = generateAndLinkRespondentAnswers.apply(caseDetails);

        verify(documentGenerator).generateAndStoreCaseDocument(RESPONDENT_ANSWERS,
            RESPONDENT_ANSWERS_TEMPLATE_ID,
            RESPONDENT_ANSWERS_DOCUMENT_NAME,
            caseData,
            TEST_CASE_ID);
        assertThat(result.getData().getConditionalOrder().getRespondentAnswersLink()).isNull();
    }

    @Test
    void shouldGenerateRespondentAnswersWhenOnlineAosIsDrafted() {

        final CaseData caseData = caseData();
        AcknowledgementOfService acknowledgementOfService = AcknowledgementOfService
                .builder()
                .aosIsDrafted(YesOrNo.YES)
                .build();

        caseData.setAcknowledgementOfService(acknowledgementOfService);

        final CaseDetails<CaseData, State> caseDetails = new CaseDetails<>();
        caseDetails.setData(caseData);
        caseDetails.setId(TEST_CASE_ID);

        generateAndLinkRespondentAnswers.apply(caseDetails);

        verify(documentGenerator).generateAndStoreCaseDocument(RESPONDENT_ANSWERS,
                RESPONDENT_ANSWERS_TEMPLATE_ID,
                RESPONDENT_ANSWERS_DOCUMENT_NAME,
                caseData,
                TEST_CASE_ID);
    }
}
