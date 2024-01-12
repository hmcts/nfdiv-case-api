package uk.gov.hmcts.divorce.caseworker.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetter;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralLetterDetails;
import uk.gov.hmcts.divorce.divorcecase.model.GeneralParties;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.divorcecase.task.CaseTask;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.content.GeneralLetterTemplateContent;
import uk.gov.hmcts.divorce.document.model.ConfidentialDivorceDocument;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static java.util.stream.Stream.ofNullable;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_LETTER_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.GENERAL_LETTER_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getConfidentialDocumentType;
import static uk.gov.hmcts.divorce.document.DocumentUtil.isConfidential;
import static uk.gov.hmcts.divorce.document.model.DocumentType.GENERAL_LETTER;

@Component
@Slf4j
public class GenerateGeneralLetter implements CaseTask {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private GeneralLetterTemplateContent templateContent;

    @Autowired
    private Clock clock;

    @Autowired
    private DocumentIdProvider documentIdProvider;

    @Override
    public CaseDetails<CaseData, State> apply(final CaseDetails<CaseData, State> caseDetails) {

        final Long caseId = caseDetails.getId();
        final CaseData caseData = caseDetails.getData();

        log.info("Generating general letter for case id: {} ", caseId);

        LanguagePreference languagePreference =
            GeneralParties.RESPONDENT.equals(caseData.getGeneralLetter().getGeneralLetterParties())
                ? caseData.getApplicant2().getLanguagePreference()
                : caseData.getApplicant1().getLanguagePreference();

        caseDataDocumentService.renderDocumentAndUpdateCaseData(
            caseData,
            GENERAL_LETTER,
            templateContent.apply(caseData, caseId, languagePreference),
            caseId,
            GENERAL_LETTER_TEMPLATE_ID,
            languagePreference,
            formatDocumentName(GENERAL_LETTER_DOCUMENT_NAME, now(clock))
        );

        updateGeneralLetters(caseData);

        return caseDetails;
    }

    private void updateGeneralLetters(CaseData caseData) {
        Optional<Document> generalLetterDocument;
        if (isConfidential(caseData, GENERAL_LETTER)) {
            generalLetterDocument =
                ofNullable(caseData.getDocuments().getConfidentialDocumentsGenerated())
                    .flatMap(Collection::stream)
                    .map(ListValue::getValue)
                    .filter(document -> getConfidentialDocumentType(GENERAL_LETTER).equals(document.getConfidentialDocumentsReceived()))
                    .findFirst()
                    .map(ConfidentialDivorceDocument::getDocumentLink);
        } else {
            generalLetterDocument =
                ofNullable(caseData.getDocuments().getDocumentsGenerated())
                    .flatMap(Collection::stream)
                    .map(ListValue::getValue)
                    .filter(document -> GENERAL_LETTER.equals(document.getDocumentType()))
                    .findFirst()
                    .map(DivorceDocument::getDocumentLink);
        }

        generalLetterDocument.ifPresent(document -> caseData.setGeneralLetters(addDocumentToTop(
            caseData.getGeneralLetters(),
            mapToGeneralLetterDetails(caseData.getGeneralLetter(), document),
            documentIdProvider.documentId()
        )));
    }

    private GeneralLetterDetails mapToGeneralLetterDetails(GeneralLetter generalLetter, Document generalLetterDocument) {

        List<ListValue<Document>> attachments = ofNullable(generalLetter.getGeneralLetterAttachments())
            .flatMap(Collection::stream)
            .map(divorceDocument -> ListValue.<Document>builder()
                .id(documentIdProvider.documentId())
                .value(divorceDocument.getValue().getDocumentLink()).build())
            .toList();

        return GeneralLetterDetails.builder()
            .generalLetterLink(generalLetterDocument)
            .generalLetterAttachmentLinks(attachments)
            .generalLetterDateTime(now(clock))
            .generalLetterParties(generalLetter.getGeneralLetterParties())
            .build();
    }
}
