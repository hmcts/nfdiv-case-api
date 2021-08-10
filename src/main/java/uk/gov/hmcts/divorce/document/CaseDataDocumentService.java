package uk.gov.hmcts.divorce.document;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.divorcecase.model.CaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;
import uk.gov.hmcts.divorce.document.model.DocumentType;
import uk.gov.hmcts.divorce.idam.IdamService;

import java.util.Map;
import java.util.function.Supplier;

import static uk.gov.hmcts.divorce.document.DocumentUtil.divorceDocumentFrom;
import static uk.gov.hmcts.divorce.document.DocumentUtil.documentFrom;

@Service
@Slf4j
public class CaseDataDocumentService {

    @Autowired
    private DocAssemblyService docAssemblyService;

    @Autowired
    private DocumentIdProvider documentIdProvider;

    @Autowired
    private IdamService idamService;

    public void renderDocumentAndUpdateCaseData(final CaseData caseData,
                                                final DocumentType documentType,
                                                final Supplier<Map<String, Object>> templateContentSupplier,
                                                final Long caseId,
                                                final String templateId,
                                                final LanguagePreference languagePreference,
                                                final Supplier<String> filename) {

        log.info("Rendering document request for templateId : {} case id: {}", templateId, caseId);

        final String authorisation = idamService.retrieveSystemUpdateUserDetails().getAuthToken();

        final var documentInfo = docAssemblyService.renderDocument(
            templateContentSupplier,
            caseId,
            authorisation,
            templateId,
            languagePreference,
            filename
        );

        log.info("Adding document to case data for templateId : {} case id: {}", templateId, caseId);

        caseData.addToDocumentsGenerated(
            ListValue.<DivorceDocument>builder()
                .id(documentIdProvider.documentId())
                .value(divorceDocumentFrom(documentInfo, documentType))
                .build());
    }

    public Document renderGeneralOrderDocument(final Supplier<Map<String, Object>> templateContentSupplier,
                                               final Long caseId,
                                               final String templateId,
                                               final LanguagePreference languagePreference,
                                               final Supplier<String> filename) {

        log.info("Rendering document request for templateId : {} ", templateId);

        final String authorisation = idamService.retrieveSystemUpdateUserDetails().getAuthToken();

        final var documentInfo = docAssemblyService.renderDocument(
            templateContentSupplier,
            caseId,
            authorisation,
            templateId,
            languagePreference,
            filename
        );

        return documentFrom(documentInfo);
    }
}
