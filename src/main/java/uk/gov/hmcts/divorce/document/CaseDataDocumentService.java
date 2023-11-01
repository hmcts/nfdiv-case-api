package uk.gov.hmcts.divorce.document;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.model.DocumentInfo;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.idam.IdamService;

import java.util.Map;

import static uk.gov.hmcts.divorce.divorcecase.model.CaseDocuments.addDocumentToTop;
import static uk.gov.hmcts.divorce.document.DocumentUtil.divorceDocumentFrom;
import static uk.gov.hmcts.divorce.document.DocumentUtil.documentFrom;
import static uk.gov.hmcts.divorce.document.DocumentUtil.documentInfoFrom;
import static uk.gov.hmcts.divorce.document.DocumentUtil.getConfidentialDocumentType;
import static uk.gov.hmcts.divorce.document.DocumentUtil.isConfidential;

@Service
@RequiredArgsConstructor
@Slf4j
public class CaseDataDocumentService {

    private final DocAssemblyService docAssemblyService;
    private final DocumentIdProvider documentIdProvider;
    private final IdamService idamService;

    public void renderDocumentAndUpdateCaseData(final CaseData caseData,
                                                final DocumentType documentType,
                                                final Map<String, Object> templateContent,
                                                final Long caseId,
                                                final String templateId,
                                                final LanguagePreference languagePreference,
                                                final String filename) {

        log.info("Rendering document request for templateId : {} case id: {}", templateId, caseId);

        final String authorisation = idamService.retrieveSystemUpdateUserDetails().getAuthToken();

        final var documentInfo = docAssemblyService.renderDocument(
            templateContent,
            caseId,
            authorisation,
            templateId,
            languagePreference,
            filename
        );

        updateCaseData(caseData, documentType, documentInfo, caseId, templateId);
    }

    public Document renderDocument(final Map<String, Object> templateContent,
                                   final Long caseId,
                                   final String templateId,
                                   final LanguagePreference languagePreference,
                                   final String filename) {

        log.info("Rendering document request for templateId : {} ", templateId);

        final String authorisation = idamService.retrieveSystemUpdateUserDetails().getAuthToken();

        final var documentInfo = docAssemblyService.renderDocument(
            templateContent,
            caseId,
            authorisation,
            templateId,
            languagePreference,
            filename
        );

        return documentFrom(documentInfo);
    }

    private void updateCaseData(final CaseData caseData,
                                final DocumentType documentType,
                                final DocumentInfo documentInfo,
                                final Long caseId,
                                final String templateId) {

        if (isConfidential(caseData, documentType)) {

            log.info("Adding confidential document to case data for templateId : {} case id: {}", templateId, caseId);

            caseData.getDocuments().setConfidentialDocumentsGenerated(addDocumentToTop(
                caseData.getDocuments().getConfidentialDocumentsGenerated(),
                divorceDocumentFrom(documentInfo, getConfidentialDocumentType(documentType)),
                documentIdProvider.documentId())
            );

        } else {

            log.info("Adding document to case data for templateId : {} case id: {}", templateId, caseId);

            caseData.getDocuments().setDocumentsGenerated(addDocumentToTop(
                caseData.getDocuments().getDocumentsGenerated(),
                divorceDocumentFrom(documentInfo, documentType),
                documentIdProvider.documentId()
            ));
        }
    }

    public void updateCaseData(final CaseData caseData,
                               final DocumentType documentType,
                               final Document document,
                               final Long caseId,
                               final String templateId) {
        updateCaseData(caseData, documentType, documentInfoFrom(document), caseId, templateId);
    }
}
