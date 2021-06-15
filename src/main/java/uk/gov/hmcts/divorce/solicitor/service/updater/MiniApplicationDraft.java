package uk.gov.hmcts.divorce.solicitor.service.updater;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.ccd.sdk.type.Document;
import uk.gov.hmcts.ccd.sdk.type.ListValue;
import uk.gov.hmcts.divorce.common.updater.CaseDataContext;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdater;
import uk.gov.hmcts.divorce.common.updater.CaseDataUpdaterChain;
import uk.gov.hmcts.divorce.document.DocAssemblyService;
import uk.gov.hmcts.divorce.document.DocumentIdProvider;
import uk.gov.hmcts.divorce.document.content.DocmosisTemplateProvider;
import uk.gov.hmcts.divorce.document.content.DraftApplicationTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_DRAFT_APPLICATION;
import static uk.gov.hmcts.divorce.document.DocumentConstants.DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.model.DocumentType.DIVORCE_APPLICATION;

@Component
@Slf4j
public class MiniApplicationDraft implements CaseDataUpdater {

    @Autowired
    private DocAssemblyService docAssemblyService;

    @Autowired
    private DocmosisTemplateProvider docmosisTemplateProvider;

    @Autowired
    private DocumentIdProvider documentIdProvider;

    @Autowired
    private DraftApplicationTemplateContent templateContent;

    @Override
    public CaseDataContext updateCaseData(final CaseDataContext caseDataContext,
                                          final CaseDataUpdaterChain caseDataUpdaterChain) {

        log.info("Executing handler for generating mini draft for case id {} ", caseDataContext.getCaseId());

        final var updatedCaseData = caseDataContext.copyOfCaseData();

        final var templateName = docmosisTemplateProvider.templateNameFor(
            DIVORCE_MINI_DRAFT_APPLICATION,
            updatedCaseData.getApplicant1().getLanguagePreference());

        final Map<String, Object> templateData = templateContent.apply(
            updatedCaseData,
            caseDataContext.getCaseId(),
            caseDataContext.getCreatedDate());

        final var documentInfo = docAssemblyService.renderDocument(
            templateData,
            caseDataContext.getCaseId(),
            caseDataContext.getUserAuthToken(),
            templateName,
            DIVORCE_MINI_DRAFT_APPLICATION_DOCUMENT_NAME
        );

        final var ccdDocument = new Document(
            documentInfo.getUrl(),
            documentInfo.getFilename(),
            documentInfo.getBinaryUrl()
        );

        final var divorceDocument = DivorceDocument
            .builder()
            .documentLink(ccdDocument)
            .documentFileName(documentInfo.getFilename())
            .documentType(DIVORCE_APPLICATION)
            .build();

        updatedCaseData.addToDocumentsGenerated(
            ListValue.<DivorceDocument>builder()
                .id(documentIdProvider.documentId())
                .value(divorceDocument)
                .build());

        return caseDataUpdaterChain.processNext(caseDataContext.handlerContextWith(updatedCaseData));
    }
}
