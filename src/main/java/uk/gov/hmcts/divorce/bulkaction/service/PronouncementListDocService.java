package uk.gov.hmcts.divorce.bulkaction.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.ccd.sdk.api.CaseDetails;
import uk.gov.hmcts.divorce.bulkaction.ccd.BulkActionState;
import uk.gov.hmcts.divorce.bulkaction.data.BulkActionCaseData;
import uk.gov.hmcts.divorce.divorcecase.model.LanguagePreference;
import uk.gov.hmcts.divorce.document.CaseDataDocumentService;
import uk.gov.hmcts.divorce.document.content.PronouncementListTemplateContent;
import uk.gov.hmcts.divorce.document.model.DivorceDocument;

import java.util.Map;

import static uk.gov.hmcts.divorce.document.DocumentConstants.PRONOUNCEMENT_LIST_DOCUMENT_NAME;
import static uk.gov.hmcts.divorce.document.DocumentConstants.PRONOUNCEMENT_LIST_TEMPLATE_ID;
import static uk.gov.hmcts.divorce.document.model.DocumentType.PRONOUNCEMENT_LIST;

@Service
@Slf4j
public class PronouncementListDocService {

    @Autowired
    private CaseDataDocumentService caseDataDocumentService;

    @Autowired
    private PronouncementListTemplateContent pronouncementListTemplateContent;

    public void generateDocument(CaseDetails<BulkActionCaseData, BulkActionState> bulkCaseDetails) {

        final Long caseId = bulkCaseDetails.getId();

        log.info("Executing handler for generating Bulk Case List for Bulk Case id {} ", caseId);

        final Map<String, Object> templateContent;
        templateContent = pronouncementListTemplateContent.apply(bulkCaseDetails.getData(), caseId);

        var pronouncementListDoc = caseDataDocumentService.renderDocument(
            templateContent,
            caseId,
            PRONOUNCEMENT_LIST_TEMPLATE_ID,
            LanguagePreference.ENGLISH,
            PRONOUNCEMENT_LIST_DOCUMENT_NAME
        );

        var pronouncementListDocument = DivorceDocument
            .builder()
            .documentLink(pronouncementListDoc)
            .documentFileName(pronouncementListDoc.getFilename())
            .documentType(PRONOUNCEMENT_LIST)
            .build();

        bulkCaseDetails.getData().setPronouncementListDocument(pronouncementListDocument);
    }
}
