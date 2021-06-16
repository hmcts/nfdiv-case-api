package uk.gov.hmcts.divorce.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.model.CaseData;
import uk.gov.hmcts.divorce.common.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;

import java.util.Map;
import java.util.function.Supplier;

import static uk.gov.hmcts.divorce.document.DocumentUtil.divorceDocumentFrom;

@Service
@Slf4j
public class CaseDataDocumentService {

    @Autowired
    private DocAssemblyService docAssemblyService;

    @Autowired
    private DocumentIdProvider documentIdProvider;

    public CaseData renderDocumentAndUpdateCaseData(final CaseData caseData,
                                                    final DocumentType documentType,
                                                    final Supplier<Map<String, Object>> templateContentSupplier,
                                                    final Long caseId,
                                                    final String authorisation,
                                                    final String templateId,
                                                    final String documentName,
                                                    final LanguagePreference languagePreference) {

        log.info("Rendering document request for templateId : {} case id: {}", templateId, caseId);

        final var documentInfo = docAssemblyService.renderDocument(
            templateContentSupplier,
            caseId,
            authorisation,
            templateId,
            documentName,
            languagePreference
        );

        log.info("Adding document to case data for templateId : {} case id: {}", templateId, caseId);

        caseData.addToDocumentsGenerated(
            ListValue.<DivorceDocument>builder()
                .id(documentIdProvider.documentId())
                .value(divorceDocumentFrom(documentInfo, documentType))
                .build());

        return caseData;
    }
}
