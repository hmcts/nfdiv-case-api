package uk.gov.hmcts.divorce.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.Applicant;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.State;
import uk.gov.hmcts.divorce.document.content.templatecontent.TemplateContent;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.document.print.documentpack.DocumentPackInfo;
import uk.gov.hmcts.divorce.document.print.model.Letter;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;
import static org.springframework.util.CollectionUtils.firstElement;
import static uk.gov.hmcts.divorce.caseworker.service.task.util.FileNameUtil.formatDocumentName;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_JUDICIAL_SEPARATION_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getLettersBasedOnContactPrivacy;
import static uk.gov.hmcts.divorce.document.model.DocumentType.CERTIFICATE_OF_ENTITLEMENT;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentGenerator {

    private final List<TemplateContent> allTemplateContentHandlers;
    private final CaseDataDocumentService caseDataDocumentService;
    private final Clock clock;

    public void generateAndStoreCaseDocument(DocumentType documentType,
                                             String templateId,
                                             String templateName,
                                             CaseData caseData,
                                             long caseId) {
        generateAndStoreCaseDocument(documentType, templateId, templateName, caseData, caseId, null);
    }

    public void generateAndStoreCaseDocument(DocumentType documentType,
                                             String templateId,
                                             String templateName,
                                             CaseData caseData,
                                             long caseId,
                                             Applicant applicant) {
        //this is a case document like FO Granted or CO Granted so is not specific to an applicant
        var templateContent = getTemplateContent(caseId, applicant, caseData, documentType, templateId);

        var generatedDocument = caseDataDocumentService.renderDocument(templateContent,
            caseId,
            templateId,
            applicant != null ? applicant.getLanguagePreference() : caseData.getApplicant1().getLanguagePreference(),
            formatDocumentName(caseId, templateName, now(clock)));

        caseDataDocumentService.updateCaseData(caseData, documentType, generatedDocument, caseId, templateId);
    }

    public List<Letter> generateDocuments(final CaseData caseData,
                                          final long caseId,
                                          final Applicant applicant,
                                          final DocumentPackInfo documentPackInfo) {
        log.info("About to start generating document pack for case {}", caseId);

        return documentPackInfo.documentPack().entrySet().stream()
            .map(entry -> toLetter(entry, caseData, caseId, applicant, documentPackInfo.templateInfo()))
            .flatMap(Optional::stream)
            .collect(Collectors.toList());
    }

    private Optional<Letter> toLetter(final Map.Entry<DocumentType, Optional<String>> entry,
                                      final CaseData caseData,
                                      final long caseId,
                                      final Applicant applicant,
                                      final Map<String, String> documentIdToDocumentNameMap) {

        var documentType = entry.getKey();
        var letter = entry.getValue()
            .map(templatedId -> {
                log.info("Generating document type {} from template id {} for case {}", documentType, templatedId, caseId);

                String documentName = documentIdToDocumentNameMap.get(templatedId);
                Document generatedDocument = generateDocument(
                    caseId,
                    applicant,
                    caseData,
                    documentType,
                    templatedId,
                    documentName);

                return new Letter(generatedDocument, 1);
            })
            .orElseGet(() -> {
                log.info("Fetching pre-generated document of type: {} for case {}", documentType, caseId);

                return firstElement(getLettersBasedOnContactPrivacy(caseData, documentType));
            });

        return Optional.ofNullable(letter);
    }

    private Document generateDocument(final long caseId,
                                      final Applicant applicant,
                                      final CaseData caseData,
                                      final DocumentType documentType,
                                      final String templateId,
                                      final String docName) {

        Map<String, Object> templateContent = getTemplateContent(caseId, applicant, caseData, documentType, templateId);

        Document generatedDocument = caseDataDocumentService.renderDocument(templateContent,
            caseId,
            templateId,
            applicant.getLanguagePreference(),
            formatDocumentName(caseId, docName, now(clock)));

        caseDataDocumentService.updateCaseData(caseData, documentType, generatedDocument, caseId, templateId);

        return generatedDocument;
    }


    private Map<String, Object> getTemplateContent(long caseId,
                                                   Applicant applicant,
                                                   CaseData caseData,
                                                   DocumentType documentType,
                                                   String templateId) {
        List<TemplateContent> relevantTemplateContent = allTemplateContentHandlers.stream()
                .filter(handler -> handler.getSupportedTemplates().contains(templateId))
                .toList();
        if (relevantTemplateContent.size() != 1) {
            throw new IllegalStateException(
                    String.format("Multiple template content providers found for given templateId %s for case %s", templateId, caseId)
            );
        }

        log.info("Got the relevant template content bean for doctype {} for case {}", documentType, caseId);

        return relevantTemplateContent.get(0).getTemplateContent(caseData, caseId, applicant);
    }

    public void generateCertificateOfEntitlement(CaseDetails<CaseData, State> details) {
        CaseData caseData = details.getData();

        generateAndStoreCaseDocument(
                CERTIFICATE_OF_ENTITLEMENT,
                caseData.isJudicialSeparationCase()
                        ? CERTIFICATE_OF_ENTITLEMENT_JUDICIAL_SEPARATION_TEMPLATE_ID
                        : CERTIFICATE_OF_ENTITLEMENT_TEMPLATE_ID,
                CERTIFICATE_OF_ENTITLEMENT_NAME,
                caseData,
                details.getId()
        );

        var regeneratedCertificateOfEntitlement =
                caseData.getDocuments().getDocumentsGenerated()
                        .stream()
                        .filter(doc -> doc.getValue().getDocumentType().equals(CERTIFICATE_OF_ENTITLEMENT))
                        .findFirst().orElseThrow();
        // Update the case data with the regenerated certificate of entitlement, copy from generated document
        caseData.getConditionalOrder().setCertificateOfEntitlementDocument(regeneratedCertificateOfEntitlement.getValue());
    }
}
