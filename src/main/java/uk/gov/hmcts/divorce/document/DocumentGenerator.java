package uk.gov.hmcts.divorce.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.model.Letter;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.time.LocalDateTime.now;
import static org.springframework.util.CollectionUtils.firstElement;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getLettersBasedOnContactPrivacy;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentGenerator {

    private final List<TemplateContent> allTemplateContentHandlers;
    private final CaseDataDocumentService caseDataDocumentService;
    private final Clock clock;

    public List<Letter> generateDocuments(final CaseData caseData,
                                          final long caseId,
                                          final Applicant applicant,
                                          final DocumentPackInfo documentPackInfo) {
        log.info("About to start generating document pack for case {}", caseId);

        return documentPackInfo.documentPack().entrySet().stream()
            .map(entry -> toLetter(entry, caseData, caseId, applicant, documentPackInfo.templateInfo()))
            .flatMap(Optional::stream)
            .toList();
    }

    private Optional<Letter> toLetter(final Map.Entry<DocumentType, Optional<String>> entry,
                                      final CaseData caseData,
                                      final long caseId,
                                      final Applicant applicant,
                                      final Map<String, String> documentIdToDocumentNameMap) {

        Letter letter = entry.getValue()
            .map(templatedId -> {
                log.info("Generating document with id {} for case {}", templatedId, caseId);

                String documentName = documentIdToDocumentNameMap.get(templatedId);
                Document generatedDocument = generateDocument(
                    caseId,
                    applicant,
                    caseData,
                    entry.getKey(),
                    templatedId,
                    documentName);

                return new Letter(generatedDocument, 1);
            })
            .orElseGet(() -> firstElement(getLettersBasedOnContactPrivacy(caseData, entry.getKey())));

        log.info("Got the letter with type {} for case {}", entry.getKey(), caseId);

        return Optional.ofNullable(letter);
    }

    private Document generateDocument(final long caseId,
                                      final Applicant applicant,
                                      final CaseData caseData,
                                      final DocumentType documentType,
                                      final String templateId,
                                      final String docName) {

        List<TemplateContent> relevantTemplateContent = allTemplateContentHandlers.stream()
            .filter(handler -> handler.getSupportedTemplates().contains(templateId))
            .toList();

        if (relevantTemplateContent.size() != 1) {
            throw new IllegalStateException(
                String.format("Multiple template content providers found for given templateId %s for case %s", templateId, caseId)
            );
        }

        log.info("Got the relevant template content bean for doctype {} for case {}", documentType, caseId);

        Map<String, Object> templateContent = relevantTemplateContent.get(0).getTemplateContent(caseData, caseId, applicant);

        Document generatedDocument = caseDataDocumentService.renderDocument(templateContent,
            caseId,
            templateId,
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, docName, now(clock)));

        caseDataDocumentService.updateCaseData(caseData, documentType, generatedDocument, caseId, templateId);

        return generatedDocument;
    }

}
